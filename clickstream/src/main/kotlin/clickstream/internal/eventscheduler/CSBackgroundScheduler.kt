package clickstream.internal.eventscheduler

import clickstream.CSEvent
import clickstream.CSInfo
import clickstream.config.CSEventSchedulerConfig
import clickstream.internal.analytics.CSEventNames
import clickstream.internal.analytics.CSHealthEvent
import clickstream.internal.analytics.CSHealthEventRepository
import clickstream.internal.analytics.EventTypes
import clickstream.analytics.event.CSEventHealthListener
import clickstream.internal.di.CSServiceLocator
import clickstream.internal.lifecycle.CSAppLifeCycle
import clickstream.internal.lifecycle.CSBackgroundLifecycleManager
import clickstream.internal.networklayer.CSBackgroundNetworkManager
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSGuIdGenerator
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.internal.utils.CSTimeStampGenerator
import clickstream.logger.CSLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay

private const val TIMEOUT: Int = 5000
private const val ONE_SEC: Long = 1000

/**
 * The BackgroundEventScheduler acts an bridge between the BackgroundWorker & NetworkManager
 *
 * It flushes all the events present in database
 */
@ExperimentalCoroutinesApi
internal class CSBackgroundScheduler(
    appLifeCycleObserver: CSAppLifeCycle,
    networkManager: CSBackgroundNetworkManager,
    dispatcher: CoroutineDispatcher,
    config: CSEventSchedulerConfig,
    eventRepository: CSEventRepository,
    healthEventRepository: CSHealthEventRepository,
    logger: CSLogger,
    guIdGenerator: CSGuIdGenerator,
    timeStampGenerator: CSTimeStampGenerator,
    batteryStatusObserver: CSBatteryStatusObserver,
    networkStatusObserver: CSNetworkStatusObserver,
    private val backgroundLifecycleManager: CSBackgroundLifecycleManager,
    private val info: CSInfo,
    eventHealthListener: CSEventHealthListener
) : CSEventScheduler(
    appLifeCycleObserver,
    networkManager,
    dispatcher,
    config,
    eventRepository,
    healthEventRepository,
    logger,
    guIdGenerator,
    timeStampGenerator,
    batteryStatusObserver,
    networkStatusObserver,
    info,
    eventHealthListener
) {

    override fun onStart() {
        logger.debug { "CSBackgroundScheduler#onStart" }
    }

    override fun onStop() {
        logger.debug { "CSBackgroundScheduler#onStop - backgroundTaskEnabled ${config.backgroundTaskEnabled}" }
        if (config.backgroundTaskEnabled) {
            job = SupervisorJob()
            coroutineScope = CoroutineScope(job + dispatcher)
            backgroundLifecycleManager.onStart()
            setupObservers()
        }
    }

    /**
     * Flushes all the events present in database and
     * waits for ack
     */
    suspend fun sendEvents(): Boolean {
        logger.debug { "CSBackgroundScheduler#sendEvents" }

        backgroundLifecycleManager.onStart()
        waitForNetwork()
        flushAllEvents()
        waitForAck()
        flushHealthEvents()
        waitForAck()
        return eventRepository.getAllEvents().isEmpty()
    }

    /**
     * Terminates lifecycle, hence closes socket connection
     */
    fun terminate() {
        logger.debug { "CSBackgroundScheduler#terminate" }

        coroutineScope.cancel()
        backgroundLifecycleManager.onStop()
    }

    private suspend fun flushAllEvents() {
        logger.debug { "CSBackgroundScheduler#flushAllEvents" }

        val events = eventRepository.getAllEvents()
        if (events.isEmpty()) return
        val reqId = forwardEvents(batch = events)
        reqId?.let {
            CSHealthEvent(
                eventName = CSEventNames.ClickStreamFlushOnBackground.value,
                eventType = EventTypes.AGGREGATE,
                eventBatchId = it,
                eventId = events.joinToString { event -> event.eventGuid },
                appVersion = info.appInfo.appVersion
            )
        }?.let { healthEventRepository.insertHealthEvent(it) }
    }

    private suspend fun flushHealthEvents() {
        logger.debug { "CSBackgroundScheduler#flushHealthEvents" }

        val healthEvents =
            CSServiceLocator.getInstance().healthEventProcessor.getAggregateEventsBasedOnEventName()
                .map { health ->
                    CSEvent(
                        guid = health.healthMeta.eventGuid,
                        timestamp = health.eventTimestamp,
                        message = health
                    )
                }
                .map { CSEventData.create(it).first }

        logger.debug { "CSBackgroundScheduler#flushHealthEvents - healthEvents size ${healthEvents.size}" }
        logger.debug { "CSBackgroundScheduler#flushHealthEvents - healthEventsGuid ${healthEvents.map { it.eventGuid }}" }

        if (healthEvents.isNotEmpty()) {
            forwardEvents(healthEvents)
        }
    }

    private suspend fun waitForAck() {
        logger.debug { "CSBackgroundScheduler#waitForAck" }

        var timeElapsed = 0
        while (eventRepository.getAllEvents().isNotEmpty() && timeElapsed <= TIMEOUT) {
            delay(ONE_SEC)
            timeElapsed += ONE_SEC.toInt()
        }
    }

    private suspend fun waitForNetwork() {
        logger.debug { "CSBackgroundScheduler#waitForNetwork" }

        var timeout = TIMEOUT
        while (!networkManager.isAvailable() && timeout > 0) {
            logger.debug { "CSBackgroundScheduler#waitForNetwork - Waiting for socket to be open" }

            delay(ONE_SEC)
            timeout -= ONE_SEC.toInt()
        }
    }
}
