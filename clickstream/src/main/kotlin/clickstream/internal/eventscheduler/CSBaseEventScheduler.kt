package clickstream.internal.eventscheduler

import clickstream.api.CSInfo
import clickstream.extension.eventName
import clickstream.extension.protoName
import clickstream.health.constant.CSEventNamesConstant
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.identity.CSGuIdGenerator
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.model.CSHealthEventDTO
import clickstream.health.proto.Health
import clickstream.health.time.CSTimeStampGenerator
import clickstream.internal.analytics.CSErrorReasons
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.networklayer.proto.raccoon.SendEventRequest
import clickstream.internal.utils.CSBatteryLevel
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.internal.utils.CSResult
import clickstream.internal.utils.CSTimeStampMessageBuilder
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.lifecycle.CSLifeCycleManager
import clickstream.listener.CSEventListener
import clickstream.listener.CSEventModel
import clickstream.logger.CSLogger
import com.google.protobuf.MessageLite
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
internal open class CSBaseEventScheduler(
    appLifeCycle: CSAppLifeCycle,
    dispatcher: CoroutineDispatcher,
    private val networkManager: CSNetworkManager,
    private val eventRepository: CSEventRepository,
    private val healthEventRepository: CSHealthEventRepository,
    private val logger: CSLogger,
    private val guIdGenerator: CSGuIdGenerator,
    private val timeStampGenerator: CSTimeStampGenerator,
    private val batteryStatusObserver: CSBatteryStatusObserver,
    private val networkStatusObserver: CSNetworkStatusObserver,
    private val info: CSInfo,
    private val eventListeners: List<CSEventListener>
) : CSLifeCycleManager(appLifeCycle) {

    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
    @Volatile
    protected var eventData: List<CSEventData> = CopyOnWriteArrayList()
    private val coroutineExceptionHandler: CoroutineExceptionHandler by lazy {
        CoroutineExceptionHandler { _, throwable ->
            logger.error {
                "================== CRASH IS HAPPENING ================== \n" +
                "= In : CSBackgroundEventScheduler                      = \n" +
                "= Due : ${throwable.message}                           = \n" +
                "==================== END OF CRASH ====================== \n"
            }
        }
    }

    init {
        logger.debug { "CSBaseEventScheduler#init" }
    }

    override fun onStart() {
        logger.debug { "CSBaseEventScheduler#onStart" }
    }

    override fun onStop() {
        logger.debug { "CSBaseEventScheduler#onStop" }
    }

    /**
     * forwardEventsFromBackground not sending transformed event to the backend
     * we only save the the transformed events to EventData table.
     *
     * The transformed events that we save to EventData table, will be get collect and send
     * to the backend whenever app goes to foreground.
     */
    protected suspend fun forwardEventsFromBackground(batch: List<CSEventData>): String? {
        logger.debug { "CSBaseEventScheduler#forwardEventsFromBackground" }

        dispatchToEventListener {
            batch.map { csEvent ->
                CSEventModel.Event.Dispatched(
                    eventId = csEvent.eventGuid,
                    eventName = getEventOrProtoName(csEvent.event()),
                    productName = csEvent.event().protoName(),
                    timeStamp = csEvent.eventTimeStamp
                )
            }
        }

        val eventRequest = transformToEventRequest(eventData = batch)
        recordEventBatchCreated(batch, eventRequest)

        logger.debug { "CSBaseEventScheduler#forwardEventsFromBackground : event size before inserted ${eventRepository.getAllEvents().size}" }
        updateEventsGuidAndInsertToDb(eventRequest, batch)
        logger.debug { "CSBaseEventScheduler#forwardEventsFromBackground : event size after inserted ${eventRepository.getAllEvents().size}" }
        return eventRequest.reqGuid
    }

    /**
     * Processes the batch and converts into EventRequest and then
     * forwards it to the NetworkManager
     */
    protected suspend fun forwardEvents(batch: List<CSEventData>): String? {
        logger.debug { "CSBaseEventScheduler#forwardEvents" }

        if (batteryStatusObserver.getBatteryStatus() == CSBatteryLevel.LOW_BATTERY) {
            logger.debug { "CSBaseEventScheduler#forwardEvents : battery is low" }
            return isBatteryLow()
        }
        if (networkStatusObserver.isNetworkAvailable().not()) {
            logger.debug { "CSBaseEventScheduler#forwardEvents : network is not available" }
            return isNetworkAvailable()
        }
        if (networkManager.isSocketConnected().not()) {
            logger.debug { "CSBaseEventScheduler#forwardEvents : socket is not connected" }
            return isSocketConnected()
        }
        dispatchToEventListener {
            batch.map { csEvent ->
                CSEventModel.Event.Dispatched(
                    eventId = csEvent.eventGuid,
                    eventName = getEventOrProtoName(csEvent.event()),
                    productName = csEvent.event().protoName(),
                    timeStamp = csEvent.eventTimeStamp
                )
            }
        }

        val eventRequest = transformToEventRequest(eventData = batch)
        val eventGuids = recordEventBatchCreated(batch, eventRequest)
        networkManager.processEvent(eventRequest = eventRequest, eventGuids = eventGuids)
        updateEventsGuidAndInsertToDb(eventRequest, batch)
        return eventRequest.reqGuid
    }

    protected suspend fun runEventGuidCollector() {
        logger.debug { "CSBaseEventScheduler#runEventGuidCollector" }

        if (coroutineContext.isActive.not()) {
            logger.debug { "CSBaseEventScheduler#runEventGuidCollector : coroutine is not active" }
            return
        }

        suspend fun onCallAckEventListener(requestId: String) {
            if (eventListeners.isEmpty()) return

            val currentRequestIdEvents = eventRepository.getEventsOnGuId(requestId)
            dispatchToEventListener {
                currentRequestIdEvents.map { csEvent ->
                    CSEventModel.Event.Acknowledged(
                        eventId = csEvent.eventGuid,
                        eventName = getEventOrProtoName(csEvent.event()),
                        productName = csEvent.event().protoName(),
                        timeStamp = csEvent.eventTimeStamp
                    )
                }
            }
        }

        coroutineScope {
            launch {
                eventRepository.getEventDataList().collect {
                    logger.debug { "CSBaseEventScheduler#runEventGuidCollector : getEventDataList().collect" }
                    eventData = it
                }
            }

            launch {
                networkManager.eventGuidFlow.collect {
                    logger.debug { "CSBaseEventScheduler#runEventGuidCollector : eventGuidFlow.collect" }

                    when (it) {
                        is CSResult.Success -> {
                            onCallAckEventListener(it.value)
                            eventRepository.deleteEventDataByGuId(it.value)
                            logger.debug { "CSBaseEventScheduler#runEventGuidCollector : Event Request sent successfully and deleted from DB: ${it.value}" }
                        }
                        is CSResult.Failure -> {
                            eventRepository.resetOnGoingForGuid(it.value)
                            logger.debug { "CSBaseEventScheduler#runEventGuidCollector : Event Request failed due to: ${it.exception.message}" }
                        }
                    }
                }
            }
        }
    }

    protected fun dispatchToEventListener(evaluator: () -> List<CSEventModel>) {
        if (eventListeners.isEmpty()) return
        logger.debug { "CSBaseEventScheduler#dispatchToEventListener" }

        coroutineScope.launch(coroutineExceptionHandler) {
            eventListeners.forEach {
                it.onCall(evaluator())
            }
        }
    }

    protected suspend fun recordHealthEvent(event: CSHealthEventDTO) {
        logger.debug { "CSBaseEventScheduler#logHealthEvent : ${event.eventName}" }

        healthEventRepository.insertHealthEvent(event)
    }

    protected fun transformToEventRequest(eventData: List<CSEventData>): SendEventRequest {
        logger.debug { "CSBaseEventScheduler#transformToEventRequest" }

        return SendEventRequest.newBuilder().apply {
            reqGuid = guIdGenerator.getId()
            sentTime = CSTimeStampMessageBuilder.build(timeStampGenerator.getTimeStamp())
            addAllEvents(eventData.map { it.event() })
        }.build()
    }

    protected fun getEventOrProtoName(message: MessageLite): String {
        return message.eventName() ?: message.protoName()
    }

    private suspend fun updateEventsGuidAndInsertToDb(
        eventRequest: SendEventRequest,
        eventData: List<CSEventData>
    ) {
        logger.debug { "CSBaseEventScheduler#updateEventsGuidAndInsertToDb" }

        val updatedList: List<CSEventData> =
            eventData.map { it.copy(eventRequestGuid = eventRequest.reqGuid, isOnGoing = true) }
        eventRepository.insertEventDataList(updatedList)
    }

    private suspend fun isBatteryLow(): String? {
        recordHealthEvent(
            CSHealthEventDTO(
                eventName = CSEventNamesConstant.Instant.ClickStreamEventBatchTriggerFailed.value,
                eventType = CSEventTypesConstant.INSTANT,
                error = CSErrorReasons.LOW_BATTERY,
                appVersion = info.appInfo.appVersion
            )
        )
        return null
    }

    private suspend fun isNetworkAvailable(): String? {
        recordHealthEvent(
            CSHealthEventDTO(
                eventName = CSEventNamesConstant.Instant.ClickStreamEventBatchTriggerFailed.value,
                eventType = CSEventTypesConstant.INSTANT,
                error = CSErrorReasons.NETWORK_UNAVAILABLE,
                appVersion = info.appInfo.appVersion
            )
        )
        return null
    }

    private suspend fun isSocketConnected(): String? {
        recordHealthEvent(
            CSHealthEventDTO(
                eventName = CSEventNamesConstant.Instant.ClickStreamEventBatchTriggerFailed.value,
                eventType = CSEventTypesConstant.INSTANT,
                error = CSErrorReasons.SOCKET_NOT_OPEN,
                appVersion = info.appInfo.appVersion
            )
        )
        return null
    }

    private suspend fun recordEventBatchCreated(
        batch: List<CSEventData>,
        eventRequest: SendEventRequest
    ): String {
        var eventGuids = ""
        if (batch.isNotEmpty() && batch[0].messageName != Health::class.qualifiedName.orEmpty()) {
            eventGuids = batch.joinToString { it.eventGuid }
            logger.debug { "CSBaseEventScheduler#recordEventBatchCreated#batch eventBatchId : ${eventRequest.reqGuid} eventId : $eventGuids" }
            logger.debug { "CSBaseEventScheduler#recordEventBatchCreated#batch : messageName : ${batch[0].messageName}" }
            recordHealthEvent(
                CSHealthEventDTO(
                    eventName = CSEventNamesConstant.AggregatedAndFlushed.ClickStreamEventBatchCreated.value,
                    eventType = CSEventTypesConstant.AGGREGATE,
                    eventBatchGuid = eventRequest.reqGuid,
                    eventGuid = eventGuids,
                    appVersion = info.appInfo.appVersion
                )
            )
        }

        return eventGuids
    }

    protected fun dispatchConnectionEventToEventListener(isConnected: Boolean) {
        if (eventListeners.isNotEmpty()) {
            eventListeners.forEach {
                it.onCall(listOf(CSEventModel.Connection(isConnected)))
            }
        }
    }
}