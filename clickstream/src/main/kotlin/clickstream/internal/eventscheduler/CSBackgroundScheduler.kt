package clickstream.internal.eventscheduler

import clickstream.CSEvent
import clickstream.api.CSInfo
import clickstream.config.CSEventSchedulerConfig
import clickstream.config.CSRemoteConfig
import clickstream.health.constant.CSEventNamesConstant
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.intermediate.CSEventHealthListener
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.model.CSHealthEventDTO
import clickstream.internal.db.CSBatchSizeSharedPref
import clickstream.internal.di.CSServiceLocator
import clickstream.internal.lifecycle.CSAppLifeCycle
import clickstream.internal.lifecycle.CSSocketConnectionManager
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSGuIdGenerator
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.internal.utils.CSTimeStampGenerator
import clickstream.listener.CSEventListener
import clickstream.logger.CSLogger
import clickstream.report.CSReportDataTracker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay

private const val TIMEOUT: Int = 100000000
private const val ONE_SEC: Long = 1000

/**
 * The BackgroundEventScheduler acts an bridge between the BackgroundWorker & NetworkManager
 *
 * It flushes all the events present in database
 */
@ExperimentalCoroutinesApi
internal open class CSBackgroundScheduler(
    appLifeCycleObserver: CSAppLifeCycle,
    networkManager: CSNetworkManager,
    dispatcher: CoroutineDispatcher,
    config: CSEventSchedulerConfig,
    eventRepository: CSEventRepository,
    healthEventRepository: CSHealthEventRepository,
    logger: CSLogger,
    guIdGenerator: CSGuIdGenerator,
    timeStampGenerator: CSTimeStampGenerator,
    batteryStatusObserver: CSBatteryStatusObserver,
    networkStatusObserver: CSNetworkStatusObserver,
    private val info: CSInfo,
    eventHealthListener: CSEventHealthListener,
    eventListeners: List<CSEventListener>,
    errorListener: CSEventSchedulerErrorListener,
    csReportDataTracker: CSReportDataTracker?,
    batchSizeRegulator: CSEventBatchSizeStrategy,
    private val csSocketConnectionManager: CSSocketConnectionManager,
    private val remoteConfig: CSRemoteConfig,
    private val batchSizeSharedPref: CSBatchSizeSharedPref
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
    eventHealthListener,
    eventListeners,
    errorListener,
    csReportDataTracker,
    batchSizeRegulator,
    csSocketConnectionManager,
    remoteConfig
) {

    override val tag: String
        get() = "CSBackgroundScheduler"

    override fun onStart() {
        logger.debug { "$tag#onStart" }
    }

    override fun onStop() {
        logger.debug { "$tag#onStop - backgroundTaskEnabled ${config.backgroundTaskEnabled}" }
        job = SupervisorJob()
        coroutineScope = CoroutineScope(job + dispatcher)
        setupObservers()
    }

    /**
     * Flushes all the events present in database and
     * waits for ack
     */
    suspend fun sendEvents(): Boolean {
        logger.debug { "$tag#sendEvents" }
        csSocketConnectionManager.connect()
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
        logger.debug { "$tag#terminate" }
        coroutineScope.cancel()
        csSocketConnectionManager.disconnect()
    }

    private suspend fun flushAllEvents() {
        logger.debug { "$tag#flushAllEvents" }

        if (remoteConfig.batchFlushedEvents) {
            while (eventRepository.getAllUnprocessedEventsCount() != 0) {
                val events =
                    eventRepository.getUnprocessedEventsWithLimit(batchSizeSharedPref.getSavedBatchSize())
                logger.debug { "$tag#Batched flushing batch size:  ${events.size} " }
                val reqId = forwardEvents(batch = events, forFlushing = true)
                logFlushHealthEvent(reqId, events)
            }
        } else {
            val events = eventRepository.getAllUnprocessedEvents()
            if (events.isEmpty()) return
            val reqId = forwardEvents(batch = events, forFlushing = true)
            logFlushHealthEvent(reqId, events)
        }
    }

    private suspend fun logFlushHealthEvent(
        reqId: String?,
        events: List<CSEventData>
    ) {
        logger.debug { "$tag#logFlushHealthEvent $reqId" }
        reqId?.let {
            CSHealthEventDTO(
                eventName = CSEventNamesConstant.AggregatedAndFlushed.ClickStreamFlushOnBackground.value,
                eventType = CSEventTypesConstant.AGGREGATE,
                eventBatchGuid = it,
                eventGuid = events.joinToString { event -> event.eventGuid },
                appVersion = info.appInfo.appVersion,
                error = if (remoteConfig.batchFlushedEvents) "Flushed ${events.size} events with batching." else "Flushed ${events.size} events.",
                count = events.size
            )
        }?.let { healthEventRepository.insertHealthEvent(it) }
    }

    private suspend fun flushHealthEvents() {
        logger.debug { "$tag#flushHealthEvents" }

        val healthEvents =
            CSServiceLocator.getInstance().healthEventProcessor.getAggregateEvents()
                .map { health ->
                    CSEvent(
                        guid = health.healthMeta.eventGuid,
                        timestamp = health.eventTimestamp,
                        message = health
                    )
                }
                .map { CSEventData.create(it).first }

        logger.debug { "$tag#flushHealthEvents - healthEvents size ${healthEvents.size}" }
        logger.debug { "$tag#flushHealthEvents - healthEventsGuid ${healthEvents.map { it.eventGuid }}" }

        if (healthEvents.isNotEmpty()) {
            forwardEvents(healthEvents, forFlushing = true)
        }
    }

    private suspend fun waitForAck() {
        logger.debug { "$tag#waitForAck" }

        var timeElapsed = 0
        while (eventRepository.getAllEvents().isNotEmpty() && timeElapsed <= TIMEOUT) {
            delay(ONE_SEC)
            timeElapsed += ONE_SEC.toInt()
        }
    }

    private suspend fun waitForNetwork() {
        logger.debug { "$tag#waitForNetwork" }

        var timeout = TIMEOUT
        while (!networkManager.isSocketAvailable() && timeout > 0) {
            logger.debug { "$tag#waitForNetwork - Waiting for socket to be open" }

            delay(ONE_SEC)
            timeout -= ONE_SEC.toInt()
        }
    }
}
