package clickstream.internal.eventscheduler

import clickstream.CSEvent
import clickstream.api.CSInfo
import clickstream.config.CSEventSchedulerConfig
import clickstream.config.CSRemoteConfig
import clickstream.health.CSHealthGateway
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.constant.CSHealthEventName
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.health.model.CSHealthEvent
import clickstream.internal.db.CSBatchSizeSharedPref
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.networklayer.socket.CSSocketConnectionManager
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSGuIdGenerator
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.internal.utils.CSTimeStampGenerator
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.listener.CSEventListener
import clickstream.logger.CSLogger
import clickstream.report.CSReportDataTracker
import com.gojek.clickstream.internal.Health
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

private const val TIMEOUT: Int = 10000
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
    logger: CSLogger,
    guIdGenerator: CSGuIdGenerator,
    timeStampGenerator: CSTimeStampGenerator,
    batteryStatusObserver: CSBatteryStatusObserver,
    networkStatusObserver: CSNetworkStatusObserver,
    private val info: CSInfo,
    eventListeners: List<CSEventListener>,
    errorListener: CSEventSchedulerErrorListener,
    csReportDataTracker: CSReportDataTracker?,
    batchSizeRegulator: CSEventBatchSizeStrategy,
    csHealthGateway: CSHealthGateway,
    private val csSocketConnectionManager: CSSocketConnectionManager,
    private val remoteConfig: CSRemoteConfig,
    private val batchSizeSharedPref: CSBatchSizeSharedPref,
    private val healthProcessor: CSHealthEventProcessor?
) : CSEventScheduler(
    appLifeCycleObserver,
    networkManager,
    dispatcher,
    config,
    eventRepository,
    healthProcessor,
    logger,
    guIdGenerator,
    timeStampGenerator,
    batteryStatusObserver,
    networkStatusObserver,
    info,
    eventListeners,
    errorListener,
    csReportDataTracker,
    batchSizeRegulator,
    csSocketConnectionManager,
    remoteConfig,
    csHealthGateway
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
        if (!waitForNetwork()) {
            pushEventsToUpstream()
            return false
        }
        flushAllEvents()
        waitForAck()
        flushHealthEvents()
        pushEventsToUpstream()
        return eventRepository.getEventCount() == 0
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

    private suspend fun logFlushHealthEvent(reqId: String?, events: List<CSEventData>) {
        logger.debug { "$tag#logFlushHealthEvent $reqId" }
        if (reqId != null && healthProcessor != null) {
            val healthEvent = CSHealthEvent(
                eventName = CSHealthEventName.ClickStreamFlushOnBackground.value,
                eventType = CSEventTypesConstant.AGGREGATE,
                appVersion = info.appInfo.appVersion,
                error = if (remoteConfig.batchFlushedEvents) "Flushed ${events.size} events with batching." else "Flushed ${events.size} events.",
                count = events.size
            )
            healthProcessor.insertBatchEvent(
                healthEvent,
                events.map { it.toCSEventForHealth(reqId) })
        }
    }

    private suspend fun flushHealthEvents() {
        healthProcessor?.getHealthEventFlow(CSEventTypesConstant.AGGREGATE, false)?.collect {
            val healthMappedEvent = it.map { health ->
                CSEvent(
                    guid = health.healthMeta.eventGuid,
                    timestamp = health.eventTimestamp,
                    message = health
                )
            }.map { CSEventData.create(it) }
            logger.debug { "$tag#flushHealthEvents - healthEvents size ${healthMappedEvent.size}" }
            if (healthMappedEvent.isNotEmpty()) {
                forwardEvents(healthMappedEvent, forFlushing = true)
            }
        }
    }

    private suspend fun pushEventsToUpstream() {
        healthProcessor?.pushEventToUpstream(CSEventTypesConstant.AGGREGATE, true)
    }

    private suspend fun waitForAck() {
        logger.debug { "$tag#waitForAck" }

        var timeElapsed = 0
        while (eventRepository.getEventCount() != 0 && timeElapsed <= TIMEOUT) {
            delay(ONE_SEC)
            timeElapsed += ONE_SEC.toInt()
        }
    }

    private suspend fun waitForNetwork(): Boolean {
        logger.debug { "$tag#waitForNetwork" }

        var timeout = TIMEOUT
        while (!networkManager.isSocketAvailable() && timeout > 0) {
            logger.debug { "$tag#waitForNetwork - Waiting for socket to be open" }

            delay(ONE_SEC)
            timeout -= ONE_SEC.toInt()
        }

        return networkManager.isSocketAvailable()
    }
}