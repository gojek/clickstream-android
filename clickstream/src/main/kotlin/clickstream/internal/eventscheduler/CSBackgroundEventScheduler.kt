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
import clickstream.listener.CSEventListener
import clickstream.logger.CSLogger
import clickstream.model.CSEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

/**
 * The [CSBackgroundEventScheduler] only mean to save flushed events for both health and non-health
 * events to the EventData table whenever app goes to background.
 *
 * **Sequence Diagram**
 * ```
 * ┌────────────────┐      ┌────────────────┐   ┌─────────────────────────────┐    ┌─────────────────────────     ┌───────────────────────┐     ┌──────────────────────────┐
 * │  Application   │      │  Clickstream   │   │  CSBackgroundEventScheduler │    │  CSBaseEventScheduler   │    │  CSEventRepository    │     │  CSHealthEventRepository │
 * └───────┬────────┘      └───────┬────────┘   └─────────────┬───────────────┘    └───────────┬─────────────┘    └──────────┬────────────┘     └───────────┬──────────────┘
 *         │                       │                          │                                │                             │                              │
 *        │ │    App goes          │                          │                                │                             │                              │
 *        │ │  to Background       │                          │                                │                             │                              │
 *        │ │ ------------------> │ │ onStop()                │                                │                             │                              │
 *         │                      │ │ ---------------------> │ │ flushAllEvents()              │                             │                              │
 *         │                       │                         │ │ -------------------------------------------------------->  │ │ getAllEvents() -            │
 *         │                       │                         │ │                               │                            │ │                │            │
 *         │                       │                         │ │ <--------------------------------------------------------  │ │ <--------------             │
 *         │                       │                         │ │ if notEmpty                   │                             │                              │
 *         │                       │                         │ │ forwardEventsFromBackground()│ │ insertEventDataList ----> │ │ inserted()                  │
 *         │                       │                         │ │ else                          │                             │                              │
 *         │                       │                         │ │ doNothing                     │                             │                              │
 *         │                       │                          │                                │                             │                              │
 *         │                       │                         │ │ flushHealthEvents()           │                             │                              │
 *         │                       │                         │ │ ----------------------------------------------------------------------------------------> │ │ getAggregateEvents() + getInstantEvents() -
 *         │                       │                         │ │                               │                             │                             │ │                                           │
 *         │                       │                         │ │ <---------------------------------------------------------------------------------------- │ │ <-----------------------------------------
 *         │                       │                         │ │ if notEmpty                   │                             │                              │
 *         │                       │                         │ │ forwardEventsFromBackground()│ │ insertHealthEvent() -----------------------------------> │ │ inserted()
 *         │                       │                         │ │ else                          │                             │                              │
 *         │                       │                         │ │ doNothing                     │                             │                              │
 *         │                       │                          │                                │                             │                              │
 *```
 */
@ExperimentalCoroutinesApi
internal class CSBackgroundEventScheduler(
    appLifeCycle: CSAppLifeCycle,
    guIdGenerator: CSGuIdGenerator,
    timeStampGenerator: CSTimeStampGenerator,
    batteryStatusObserver: CSBatteryStatusObserver,
    networkStatusObserver: CSNetworkStatusObserver,
    eventListeners: List<CSEventListener>,
    networkManager: CSNetworkManager,
    private val healthEventProcessor: CSHealthEventProcessor,
    private val info: CSInfo,
    private val eventRepository: CSEventRepository,
    private val healthEventRepository: CSHealthEventRepository,
    private val logger: CSLogger,
    dispatcher: CoroutineDispatcher
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
    private val coroutineScope =  CoroutineScope(SupervisorJob() + dispatcher)
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
        logger.debug { "CSBackgroundEventScheduler#init" }
        addObserver()
    }

    override fun onStart() {
        logger.debug { "CSBackgroundEventScheduler#onStart" }
        cancelJob()
    }

    override fun onStop() {
        logger.debug { "CSBackgroundEventScheduler#onStop" }
        coroutineScope.launch(coroutineExceptionHandler) {
            flushEvents()
        }
    }

    fun cancelJob() {
        logger.debug { "CSBackgroundEventScheduler#cancelJob" }
        job.cancelChildren()
    }

    private suspend fun flushEvents() {
        logger.debug { "CSBackgroundEventScheduler#flushEvents" }

        flushAllEvents()
        flushHealthEvents()
    }

    private suspend fun flushAllEvents() {
        logger.debug { "CSBackgroundEventScheduler#flushAllEvents" }

        val events = eventRepository.getAllEvents()
        if (events.isEmpty()) return

        forwardEventsFromBackground(batch = events)
        CSHealthEventDTO(
            eventName = CSEventNamesConstant.AggregatedAndFlushed.ClickStreamFlushOnBackground.value,
            eventType = CSEventTypesConstant.AGGREGATE,
            eventGuid = events.joinToString { event -> event.eventGuid },
            appVersion = info.appInfo.appVersion
        ).let { healthEventRepository.insertHealthEvent(it) }
    }

    private suspend fun flushHealthEvents() {
        logger.debug { "CSBackgroundEventScheduler#flushHealthEvents" }

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

        if (healthEvents.isEmpty()) return
        forwardEventsFromBackground(healthEvents)
    }
}
