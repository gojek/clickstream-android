package clickstream.internal.eventscheduler

import clickstream.api.CSInfo
import clickstream.config.CSEventSchedulerConfig
import clickstream.extension.eventName
import clickstream.extension.isValidMessage
import clickstream.extension.protoName
import clickstream.extension.toFlatMap
import clickstream.health.constant.CSEventNamesConstant
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.identity.CSGuIdGenerator
import clickstream.health.intermediate.CSEventHealthListener
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.model.CSHealthEventDTO
import clickstream.health.time.CSTimeStampGenerator
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.internal.utils.flowableTicker
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.listener.CSEventListener
import clickstream.listener.CSEventModel
import clickstream.logger.CSLogger
import clickstream.model.CSEvent
import com.google.protobuf.MessageLite
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

/**
 * The EventScheduler acts an bridge between the EventProcessor & NetworkManager
 *
 * It caches the events delivered to it. Based on the provided configuration, it processes
 * the events into batch at regular intervals
 *
 * @param eventRepository - To cache the data
 * @param networkManager - to send the analytic data to server
 * @param config - config how the scheduler should process the events
 * @param dispatcher - CoroutineDispatcher on which the events are observed
 * @param logger - To create logs
 * @param healthEventRepository - Used for logging health events
 * @param guIdGenerator - Used for generating a random ID
 * @param timeStampGenerator - Used for generating current time stamp
 * @param batteryStatusObserver - observes the battery status
 */
@ExperimentalCoroutinesApi
internal class CSForegroundEventScheduler(
    appLifeCycle: CSAppLifeCycle,
    private val networkManager: CSNetworkManager,
    private val dispatcher: CoroutineDispatcher,
    private val config: CSEventSchedulerConfig,
    private val eventRepository: CSEventRepository,
    private val healthEventRepository: CSHealthEventRepository,
    private val logger: CSLogger,
    private val guIdGenerator: CSGuIdGenerator,
    private val timeStampGenerator: CSTimeStampGenerator,
    private val batteryStatusObserver: CSBatteryStatusObserver,
    networkStatusObserver: CSNetworkStatusObserver,
    private val info: CSInfo,
    private val eventHealthListener: CSEventHealthListener,
    eventListeners: List<CSEventListener>
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

    private val job = SupervisorJob()
    private val coroutineScope: CoroutineScope = CoroutineScope(job + dispatcher)
    private val coroutineExceptionHandler: CoroutineExceptionHandler by lazy {
        CoroutineExceptionHandler { _, throwable ->
            logger.error {
                "================== CRASH IS HAPPENING ================== \n" +
                "= In : CSForegroundEventScheduler                      = \n" +
                "= Due : ${throwable.message}                           = \n" +
                "==================== END OF CRASH ====================== \n"
            }
        }
    }

    init {
        logger.debug { "CSForegroundEventScheduler#init" }
        addObserver()
    }

    override fun onStart() {
        logger.debug { "CSForegroundEventScheduler#onStart" }
        coroutineScope.launch(coroutineExceptionHandler) {
            launch { runEventGuidCollector() }
            launch { runTicker() }
            launch { runResetOnGoingEvents() }
        }
    }

    override fun onStop() {
        logger.debug { "CSForegroundEventScheduler#onStop" }
        cancelJob()
    }

    fun cancelJob() {
        logger.debug { "CSForegroundEventScheduler#cancelJob" }
        job.cancelChildren()
    }

    suspend fun scheduleEvent(event: CSEvent) {
        logger.debug { "CSForegroundEventScheduler#scheduleEvent ${event.message.eventName()} ${event.message.protoName()}" }

        if (coroutineContext.isActive.not()) {
            logger.debug { "CSForegroundEventScheduler#scheduleEvent : coroutine is not active" }
            return
        }
        if (validateMessage(event.message).not()) {
            logger.debug { "CSForegroundEventScheduler#scheduleEvent : message with name ${event.message.eventName()} is not valid" }
            return
        }

        val (eventData, eventHealthData) = CSEventData.create(event)
        eventHealthListener.onEventCreated(eventHealthData)
        eventRepository.insertEventData(eventData)
        dispatchToEventListener {
            listOf(
                CSEventModel.Event.Scheduled(
                    eventId = eventData.eventGuid,
                    eventName = getEventOrProtoName(event.message),
                    productName = event.message.protoName(),
                    properties = event.message.toFlatMap(),
                    timeStamp = eventData.eventTimeStamp,
                )
            )
        }

        recordHealthEvent(
            CSHealthEventDTO(
                eventName = CSEventNamesConstant.AggregatedAndFlushed.ClickStreamEventCached.value,
                eventType = CSEventTypesConstant.AGGREGATE,
                eventGuid = eventData.eventGuid,
                appVersion = info.appInfo.appVersion
            )
        )
    }

    suspend fun sendInstantEvent(event: CSEvent) {
        logger.debug { "CSForegroundEventScheduler#sendInstantEvent ${event.message.eventName()} ${event.message.protoName()}" }

        if (coroutineContext.isActive.not()) {
            logger.debug { "CSForegroundEventScheduler#sendInstantEvent : coroutine is not active" }
            return
        }
        if (validateMessage(event.message).not()) {
            logger.debug { "CSForegroundEventScheduler#sendInstantEvent : message not valid ${event.message.eventName()}" }
            return
        }
        val (eventData, eventHealthData) = CSEventData.create(event)
        eventHealthListener.onEventCreated(eventHealthData)
        dispatchToEventListener {
            listOf(
                CSEventModel.Event.Instant(
                    eventId = eventData.eventGuid,
                    eventName = getEventOrProtoName(event.message),
                    productName = event.message.protoName(),
                    properties = event.message.toFlatMap(),
                    timeStamp = eventData.eventTimeStamp,
                )
            )
        }
        val eventRequest = transformToEventRequest(eventData = listOf(eventData))
        networkManager.processInstantEvent(eventRequest)
    }

    private suspend fun runTicker() {
        logger.debug { "CSForegroundEventScheduler#runTicker" }

        if (coroutineContext.isActive.not()) {
            logger.debug { "CSForegroundEventScheduler#setupTicker : coroutine is not active" }
            return
        }

        flowableTicker(initialDelay = 10, delayMillis = config.batchPeriod)
            .onEach {
                logger.debug { "CSForegroundEventScheduler#setupTicker : tick" }
                dispatchConnectionEventToEventListener(networkManager.isSocketConnected())
            }
            .catch {
                logger.error { "CSForegroundEventScheduler#setupTicker : catch ${it.message}" }
            }
            .collect {
                if (eventData.isEmpty()) {
                    return@collect
                }
                val batch = when {
                    eventData.isEmpty() -> emptyList()
                    isEventLessThanBatchCount() -> eventData
                    else -> eventData.subList(0, config.eventsPerBatch)
                }
                logger.debug { "CSForegroundEventScheduler#runTicker#collect : event batch size ${batch.size}" }

                forwardEvents(batch)
            }
    }

    private suspend fun runResetOnGoingEvents() {
        logger.debug { "CSForegroundEventScheduler#resetOnGoingData" }

        if (coroutineContext.isActive.not()) {
            logger.debug { "CSForegroundEventScheduler#resetOnGoingData : coroutine is not active" }
            return
        }

        val onGoingEvents = eventRepository.getOnGoingEvents()
        val data: List<CSEventData> = onGoingEvents.map { it.copy(isOnGoing = false) }
        if (data.isNotEmpty()) {
            eventRepository.insertEventDataList(data)
        }
    }

    private fun isEventLessThanBatchCount(): Boolean {
        return config.eventsPerBatch > eventData.size
    }

    private fun validateMessage(message: MessageLite): Boolean {
        logger.debug { "CSForegroundEventScheduler#validateMessage : ${message.eventName()} ${message.protoName()}" }

        return config.utf8ValidatorEnabled && message.isValidMessage()
    }
}
