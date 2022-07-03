package clickstream.internal.eventscheduler

import clickstream.api.CSInfo
import clickstream.health.constant.CSEventNamesConstant
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.identity.CSGuIdGenerator
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.model.CSHealthEventDTO
import clickstream.health.time.CSTimeStampGenerator
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.lifecycle.CSBackgroundLifecycleManager
import clickstream.listener.CSEventListener
import clickstream.logger.CSLogger
import clickstream.model.CSEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay

private const val TIMEOUT: Int = 5000
private const val ONE_SEC: Long = 1000

@ExperimentalCoroutinesApi
internal class CSWorkManagerEventScheduler(
    appLifeCycle: CSAppLifeCycle,
    guIdGenerator: CSGuIdGenerator,
    timeStampGenerator: CSTimeStampGenerator,
    batteryStatusObserver: CSBatteryStatusObserver,
    networkStatusObserver: CSNetworkStatusObserver,
    eventListeners: List<CSEventListener>,
    dispatcher: CoroutineDispatcher,
    private val healthEventProcessor: CSHealthEventProcessor,
    private val backgroundLifecycleManager: CSBackgroundLifecycleManager,
    private val info: CSInfo,
    private val eventRepository: CSEventRepository,
    private val healthEventRepository: CSHealthEventRepository,
    private val logger: CSLogger,
    private val networkManager: CSNetworkManager
) : CSBaseEventScheduler(
    appLifeCycle,
    dispatcher,
    networkManager,
    eventRepository,
    healthEventRepository,
    logger,
    guIdGenerator,
    timeStampGenerator,
    batteryStatusObserver,
    networkStatusObserver,
    info,
    eventListeners
) {

    init {
        logger.debug { "CSWorkManagerEventScheduler#init" }
    }

    override fun onStart() {
        logger.debug { "CSWorkManagerEventScheduler#onStart" }
    }

    override fun onStop() {
        logger.debug { "CSWorkManagerEventScheduler#onStop" }
    }

    suspend fun sendEvents(): Boolean {
        logger.debug { "CSWorkManagerEventScheduler#sendEvents" }

        backgroundLifecycleManager.onStart()
        runEventGuidCollector()
        waitForNetwork()
        flushAllEvents()
        waitForAck()
        flushHealthEvents()
        waitForAck()
        backgroundLifecycleManager.onStop()
        return eventRepository.getAllEvents().isEmpty()
    }

    // we sending to backend
    // if we get ack success, we remove the event from the EventData table
    private suspend fun flushAllEvents() {
        logger.debug { "CSWorkManagerEventScheduler#flushAllEvents" }

        val events = eventRepository.getAllEvents()
        if (events.isEmpty()) return

        forwardEvents(batch = events)
        CSHealthEventDTO(
            eventName = CSEventNamesConstant.AggregatedAndFlushed.ClickStreamFlushOnBackground.value,
            eventType = CSEventTypesConstant.AGGREGATE,
            eventGuid = events.joinToString { event -> event.eventGuid },
            appVersion = info.appInfo.appVersion
        ).let { healthEventRepository.insertHealthEvent(it) }
    }

    // we sending to backend
    // if we get ack success, we remove the event from the EventData table
    private suspend fun flushHealthEvents() {
        logger.debug { "CSWorkManagerEventScheduler#flushHealthEvents" }

        val aggregateEvents = healthEventProcessor.getAggregateEvents()
        val instantEvents = healthEventProcessor.getInstantEvents()
        val healthEvents = (aggregateEvents + instantEvents).map { health ->
            CSEvent(
                guid = health.healthMeta.eventGuid,
                timestamp = health.eventTimestamp,
                message = health
            )
        }.map { CSEventData.create(it).first }

        logger.debug { "CSWorkManagerEventScheduler#flushHealthEvents - healthEvents size ${healthEvents.size}" }
        if (healthEvents.isNotEmpty()) {
            forwardEvents(healthEvents)
        }
    }

    private suspend fun waitForAck() {
        logger.debug { "CSWorkManagerEventScheduler#waitForAck" }

        var timeElapsed = 0
        while (eventRepository.getAllEvents().isNotEmpty() && timeElapsed <= TIMEOUT) {
            delay(ONE_SEC)
            timeElapsed += ONE_SEC.toInt()
        }
    }

    private suspend fun waitForNetwork() {
        logger.debug { "CSWorkManagerEventScheduler#waitForNetwork" }

        var timeout = TIMEOUT
        while (!networkManager.isSocketConnected() && timeout > 0) {
            logger.debug { "CSWorkManagerEventScheduler#waitForNetwork - Waiting for socket to be open" }

            delay(ONE_SEC)
            timeout -= ONE_SEC.toInt()
        }
    }
}
