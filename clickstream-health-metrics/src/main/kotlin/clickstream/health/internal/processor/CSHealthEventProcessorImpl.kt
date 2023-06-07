package clickstream.health.internal.processor

import clickstream.api.CSInfo
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.intermediate.CSHealthEventLoggerListener
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.health.intermediate.CSMemoryStatusProvider
import clickstream.health.internal.factory.CSHealthEventFactory
import clickstream.health.internal.repository.CSHealthEventRepository
import clickstream.health.model.CSEventForHealth
import clickstream.health.model.CSHealthEvent
import clickstream.health.model.CSHealthEventConfig
import clickstream.health.model.EXTERNAL
import clickstream.health.model.INTERNAL
import clickstream.logger.CSLogger
import clickstream.util.CSAppVersionSharedPref
import com.gojek.clickstream.internal.Health
import com.gojek.clickstream.internal.HealthDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

private const val MAX_CHUNK_THRESHOLD = 13

/**
 * The HealthEventProcessor is responsible for aggregating, sending and clearing health events for the sdk.
 *
 */
internal open class CSHealthEventProcessorImpl(
    private val healthEventRepository: CSHealthEventRepository,
    private val healthEventConfig: CSHealthEventConfig,
    private val info: CSInfo,
    private val logger: CSLogger,
    private val healthEventFactory: CSHealthEventFactory,
    private val memoryStatusProvider: CSMemoryStatusProvider,
    private val csHealthEventLogger: CSHealthEventLoggerListener,
) : CSHealthEventProcessor {

    private val tag: String
        get() = "CSHealthEventProcessor"

    override suspend fun insertNonBatchEvent(csEvent: CSHealthEvent): Boolean {
        return doSuspendedIfHealthEnabled {
            healthEventRepository.insertHealthEvent(csEvent)
        }
    }

    override suspend fun insertBatchEvent(
        csEvent: CSHealthEvent, list: List<CSEventForHealth>
    ): Boolean {
        return doSuspendedIfHealthEnabled {

            // Set event guids only if verbosity is enabled
            var eventGuids = ""
            doIfVerbosityEnabled {
                eventGuids = list.joinToString(",") { it.eventGuid }
            }

            val eventCount = list.size.toLong()

            val batchId = if (list.isNotEmpty()) list[0].batchGuid ?: "" else ""

            healthEventRepository.insertHealthEvent(
                csEvent.copy(
                    eventGuid = eventGuids, batchSize = eventCount, eventBatchGuid = batchId
                )
            )
        }
    }

    override suspend fun insertBatchEvent(csEvent: CSHealthEvent, eventCount: Long): Boolean {
        return doSuspendedIfHealthEnabled {
            healthEventRepository.insertHealthEvent(
                csEvent.copy(
                    batchSize = eventCount
                )
            )
        }
    }

    /**
     * Events are controlled via [CSHealthEventConfig.destination]. If destination contains [INTERNAL]
     * then events are pushed to flow.
     *
     * */
    override fun getHealthEventFlow(type: String, deleteEvents: Boolean): Flow<List<Health>> =
        getHealthEventFlowInternal(type, deleteEvents).map {
            if (healthEventConfig.destination.contains(INTERNAL)) {
                getProtoListForInternalTracking(it)
            } else emptyList()
        }

    /**
     * If it contains [EXTERNAL] they are pushed to upstream.
     * */
    override suspend fun pushEventToUpstream(type: String, deleteEvents: Boolean) {
        doSuspendedIfHealthEnabled {
            getHealthEventFlowInternal(type, deleteEvents).collect {
                if (healthEventConfig.destination.contains(EXTERNAL)) {
                    pushEventToUpstream(it)
                }
            }
        }
    }

    private fun getHealthEventFlowInternal(type: String, deleteEvents: Boolean) = flow {
        logger.debug { "$tag#getAggregateEventsBasedOnEventName" }
        var totalEventCount = healthEventRepository.getEventCount(type)
        if (!doSuspendedIfHealthEnabled {
                while (totalEventCount <= healthEventRepository.getEventCount(type)) {
                    val batch = healthEventRepository.getEventsByTypeAndLimit(type, 30)
                    emit(batch)
                    totalEventCount += batch.size
                    if (deleteEvents) {
                        healthEventRepository.deleteHealthEvents(batch)
                    }
                }
            }) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.Default)


    private fun pushEventToUpstream(list: List<CSHealthEvent>) {
        list.filter { CSHealthEventConfig.isTrackedViaExternal(it.eventName) }
            .groupBy { it.eventName }.forEach { entry ->
                val errorEvents = entry.value.filter { it.error.isNotBlank() }.groupBy { it.error }
                if (errorEvents.isNotEmpty()) {
                    pushEventsBasedOnError(errorEvents)
                } else {
                    pushEventsBasedOnEventName(entry.value)
                }
            }
    }

    private fun pushEventToUpstream(eventName: String, mapData: HashMap<String, Any>) {
        csHealthEventLogger.logEvent(eventName, mapData)
    }

    private fun pushEventsBasedOnError(events: Map<String, List<CSHealthEvent>>) {
        logger.debug { "$tag#sendAggregateEventsBasedOnError" }
        events.forEach { entry ->
            chunkEventsAndPushToUpstream(entry.value)
        }
    }

    private fun pushEventsBasedOnEventName(events: List<CSHealthEvent>) {
        logger.debug { "$tag#sendAggregateEventsBasedOnEventName" }
        chunkEventsAndPushToUpstream(events)
    }

    private fun chunkEventsAndPushToUpstream(events: List<CSHealthEvent>) {
        var eventId = ""
        var batchId = ""

        if (events.isEmpty()) {
            return
        }

        val batchSize = if (events.size > MAX_CHUNK_THRESHOLD) MAX_CHUNK_THRESHOLD else events.size

        events.chunked(batchSize).forEach { list ->
            if (list.isNotEmpty()) {

                doIfVerbosityEnabled {
                    eventId =
                        events.filter { it.eventGuid.isNotBlank() }.joinToString { it.eventGuid }
                    batchId = events.filter { it.eventBatchGuid.isNotBlank() }
                        .joinToString { it.eventBatchGuid }
                }

                val data = events[0].copy(
                    eventGuid = eventId, eventBatchGuid = batchId, count = list.size
                ).eventData()

                pushEventToUpstream(events[0].eventName, data)
            }
        }
    }

    private suspend fun clearHealthEvents(list: List<CSHealthEvent>) {
        doSuspendedIfHealthEnabled {
            healthEventRepository.deleteHealthEvents(list)
        }
    }

    private suspend fun getProtoListForInternalTracking(batch: List<CSHealthEvent>): List<Health> {
        val healthEvents = mutableListOf<Health>()

        val batchGroupOnEventName =
            batch.filter { CSHealthEventConfig.isTrackedViaInternal(it.eventName) }
                .groupBy { it.eventName }

        batchGroupOnEventName.forEach { entry ->
            val health = createHealthProto(entry.key, entry.value)
            healthEvents += healthEventFactory.create(health)
            logger.debug { "$tag#getAggregateEventsBasedOnEventName - Health events: $health" }
        }
        return healthEvents
    }

    private fun createHealthProto(eventName: String, events: List<CSHealthEvent>): Health {
        logger.debug { "$tag#createHealthProto" }

        // Calculating total events
        var eventGuidCount = 0L
        events.forEach { eventGuidCount += it.batchSize }

        // Calculating batch size
        val batchSize = events.filter { it.eventBatchGuid.isNotEmpty() }.size.toLong()

        logger.debug { "$tag#createHealthProto - eventGuids $eventGuidCount" }
        logger.debug { "$tag#createHealthProto - eventBatchGuids $batchSize" }

        return Health.newBuilder().apply {
            this.eventName = eventName
            numberOfEvents = eventGuidCount
            numberOfBatches = batchSize

            // Only fill health details if verbosity is maximum
            doIfVerbosityEnabled {

                val eventGuids = mutableListOf<String>()

                // List of event guids
                events.forEach { event ->
                    val eventIdArray = event.eventGuid.split(",").map { it.trim() }
                    if (eventIdArray.isNotEmpty()) {
                        eventGuids += eventIdArray
                    }
                }

                // List of batch ids
                val eventBatchGuids =
                    events.filter { it.eventBatchGuid.isNotBlank() }.map { it.eventBatchGuid }

                healthDetails = HealthDetails.newBuilder().apply {
                    addAllEventGuids(eventGuids)
                    addAllEventBatchGuids(eventBatchGuids)
                }.build()
                logger.debug { "$tag#createHealthProto - HealthDetails $healthDetails" }
            }
        }.build()
    }

    private fun isHealthEventEnabled(): Boolean {
        return isHealthEventEnabled(memoryStatusProvider, healthEventConfig, info)
    }

    private inline fun <T> doIfVerbosityEnabled(crossinline executable: () -> T) {
        logger.debug { "$tag#createHealthProto# - isVerboseLoggingEnabled ${healthEventConfig.isVerboseLoggingEnabled()}" }
        if (healthEventConfig.isVerboseLoggingEnabled()) {
            executable()
        }
    }

    private suspend inline fun doSuspendedIfHealthEnabled(crossinline executable: suspend () -> Unit): Boolean {
        return if (isHealthEventEnabled()) {
            executable()
            true
        } else {
            false
        }
    }

    private inline fun doIfHealthEnabled(crossinline executable: () -> Unit): Boolean {
        return if (isHealthEventEnabled()) {
            executable()
            true
        } else {
            false
        }
    }

    companion object {

        internal fun isHealthEventEnabled(
            memoryStatusProvider: CSMemoryStatusProvider,
            healthEventConfig: CSHealthEventConfig,
            info: CSInfo,
        ): Boolean {
            return !memoryStatusProvider.isLowMemory() && healthEventConfig.isEnabled(
                info.appInfo.appVersion, info.userInfo.identity
            )
        }

        suspend fun clearHealthEventsForVersionChange(
            appSharedPref: CSAppVersionSharedPref,
            currentAppVersion: String,
            healthEventRepository: CSHealthEventRepository,
            logger: CSLogger
        ) {
            if (!appSharedPref.isAppVersionEqual(currentAppVersion)) {
                healthEventRepository.deleteHealthEventsByType(CSEventTypesConstant.AGGREGATE)
                logger.debug { "CSHealthEventProcessorImpl#Deleted events on version change" }
            }
        }
    }
}