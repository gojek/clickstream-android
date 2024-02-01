package clickstream.health.internal

import androidx.annotation.RestrictTo
import clickstream.api.CSInfo
import clickstream.health.intermediate.CSHealthEventFactory
import clickstream.health.intermediate.CSHealthEventLoggerListener
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.internal.CSHealthEventEntity.Companion.dtosMapTo
import clickstream.health.model.CSHealthEventConfig
import clickstream.health.model.CSHealthEventDTO
import clickstream.health.proto.Health
import clickstream.health.proto.HealthDetails
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.lifecycle.CSLifeCycleManager
import clickstream.logger.CSLogger
import clickstream.util.CSAppVersionSharedPref
import com.google.protobuf.Timestamp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.collections.Map.Entry

private const val MAX_BATCH_THRESHOLD = 13

/**
 * [CSHealthEventProcessor] is the Heart of the Clickstream Library. The [CSHealthEventProcessor]
 * is only for pushing events to the backend. [CSHealthEventProcessor] is respect to the
 * Application lifecycle where on the active state, we have a ticker that will collect events from database
 * and the send that to the backend. The ticker will run on every 10seconds and will be stopped
 * whenever application on the inactive state.
 *
 * On the inactive state we will running flush for both Events and HealthEvents, where
 * it would be transformed and send to the backend.
 *
 * **Sequence Diagram**
 * ```
 *            App                               Clickstream
 * +---+---+---+---+---+---+           +---+---+---+---+---+---+
 * |     Sending Events    | --------> |  Received the Events  |
 * +---+---+---+---+---+---+           +---+---+---+---+---+---+
 *                                                 |
 *                                                 |
 *                                                 |                         +---+---+---+---+---+---+---+---+----+
 *                                         if app on active state ---------> |   - run the ticker with 10s delay  |                |
 *                                                 |                         |   - collect events from db         |
 *                                                 |                         |   - transform and send to backend  |
 *                                                 |                         +---+---+---+---+---+---+---+---+----+
 *                                                 |
 *                                                 |                         +---+---+---+---+---+---+---+---+---+---+----+
 *                                         else if app on inactive state --> |   - run flushEvents and flushHealthMetrics |
 *                                                                           |   - transform and send to backend          |
 *                                                                           +---+---+---+---+---+---+---+---+---+----+---+
 *```
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class DefaultCSHealthEventProcessor(
    appLifeCycleObserver: CSAppLifeCycle,
    private val healthEventRepository: CSHealthEventRepository,
    private val dispatcher: CoroutineDispatcher,
    private val healthEventConfig: CSHealthEventConfig,
    private val info: CSInfo,
    private val logger: CSLogger,
    private val healthEventLoggerListener: CSHealthEventLoggerListener,
    private val healthEventFactory: CSHealthEventFactory,
    private val appVersion: String,
    private val appVersionPreference: CSAppVersionSharedPref
) : CSLifeCycleManager(appLifeCycleObserver), CSHealthEventProcessor {

    private var scope: CoroutineScope? = CoroutineScope(SupervisorJob() + dispatcher)
    private val scopeForAppUpgrading = CoroutineScope(SupervisorJob() + dispatcher)

    init {
        logger.debug { "DefaultCSHealthEventProcessor#init" }
        addObserver()
        flushOnAppUpgrade()
    }

    override fun onStart() {
        logger.debug { "DefaultCSHealthEventProcessor#onStart" }

        scope?.cancel()
        scope = null
    }

    override fun onStop() {
        logger.debug { "DefaultCSHealthEventProcessor#onStop" }

        scope = CoroutineScope(SupervisorJob() + dispatcher)
        trySendEventsToAnalyticsUpstream()
    }

    override suspend fun getInstantEvents(): List<Health> {
        logger.debug { "DefaultCSHealthEventProcessor#getInstantEvents" }

        if (isTrackedViaBothOrInternal().not()) {
            logger.debug { "DefaultCSHealthEventProcessor#getInstantEvents : Operation is not allowed" }
            return emptyList()
        }

        val instantEvents = healthEventRepository.getInstantEvents()
        val transformedEvents = instantEvents.map { event ->
            val health = Health.newBuilder()
                .setEventName(event.eventName)
                .setNumberOfEvents(1) // Since instant events are fired one at a time
                //.setNumberOfBatches() no need to set setNumberOfBatches for instant event
                //.setHealthMeta() will be override through healthEventFactory.create below
                .setHealthDetails(
                    HealthDetails.newBuilder()
                        .addEventGuids(event.eventGuid)
                        .addEventBatchGuids(event.eventBatchGuid)
                        .build()
                )
                .build()
            logger.debug { "DefaultCSHealthEventProcessor#getInstantEvents : Health Events $health" }
            healthEventFactory.create(health)
        }

        // Deleted collected aggregate events from health db,
        // since we're going to insert transformed events to event db anyway
        // so deleting health events here is expected
        healthEventRepository.deleteHealthEvents(instantEvents)
        return transformedEvents
    }

    override suspend fun getAggregateEvents(): List<Health> {
        logger.debug { "DefaultCSHealthEventProcessor#getAggregateEvents" }

        if (isTrackedViaBothOrInternal().not()) {
            logger.debug { "DefaultCSHealthEventProcessor#getInstantEvents : Operation is not allowed" }
            return emptyList()
        }

        val aggregateEvents = healthEventRepository.getAggregateEvents()
        val transformedEvents = aggregateEvents.groupBy { it.eventName }
            .map { entry: Entry<String, List<CSHealthEventDTO>> ->
                val events = entry.value.dtosMapTo()
                val eventGuids = mutableListOf<String>()
                events.forEach { event ->
                    val eventIdArray = event.eventId.split(",").map { it.trim() }
                    eventGuids += eventIdArray
                }
                val eventBatchGuids =
                    events.filter { it.eventBatchId.isNotBlank() }.map { it.eventBatchId }
                val health = Health.newBuilder()
                    .setEventName(entry.key)
                    .setNumberOfEvents(eventGuids.size.toLong())
                    .setNumberOfBatches(eventBatchGuids.size.toLong())
                    //.setHealthMeta() will be override through healthEventFactory.create below
                    .setEventTimestamp(Timestamp.getDefaultInstance())
                    .setDeviceTimestamp(Timestamp.getDefaultInstance())
                    .setHealthDetails(
                        HealthDetails.newBuilder()
                            .addAllEventGuids(eventGuids)
                            .addAllEventBatchGuids(eventBatchGuids)
                            .build()
                    )
                    // This not necessary at the moment
                    // As in RFC see: https://github.com/gojek/clickstream-android/discussions/18
                    // state that, the error along with timeToConnected only for instant event.
                    //.setTraceDetails()
                    .build()

                logger.debug { "DefaultCSHealthEventProcessor#getAggregateEvents : Health Events $health" }
                healthEventFactory.create(health)
            }

        // Deleted collected aggregate events from health db,
        // since we're going to insert transformed events to event db anyway
        // so deleting health events here is expected
        healthEventRepository.deleteHealthEvents(aggregateEvents)
        return transformedEvents
    }

    private fun trySendEventsToAnalyticsUpstream() {
        logger.debug { "CSHealthEventProcessor#sendEvents" }

        scope!!.launch {
            logger.debug { "CSHealthEventProcessor#sendEvents : isCoroutineActive $isActive" }

            if (isActive.not()) {
                logger.debug { "CSHealthEventProcessor#sendEvents : coroutineScope is not longer active" }
                return@launch
            }
            if (isHealthEventEnabled().not()) {
                logger.debug { "CSHealthEventProcessor#sendEvents : Health Event condition is not satisfied for this user" }
                return@launch
            }
            if (healthEventConfig.isTrackedForBoth()) {
                logger.debug { "CSHealthEventProcessor#sendEvents : sendEventsToAnalytics" }
                trySendUpstream()
            }
        }
    }

    // TrySendUpstream will be send events to the upstream via listener.
    private suspend fun trySendUpstream() {
        trySendUpstreamInstantEvents()
        trySendUpstreamAggregateEvents()
    }

    private suspend fun trySendUpstreamInstantEvents() {
        val instantEvents: List<CSHealthEventDTO> = healthEventRepository.getInstantEvents()
        trySendToUpstream(instantEvents.dtosMapTo())

        // Delete Events only if TrackedVia is for External or Internal
        if (healthEventConfig.isTrackedForInternal() || healthEventConfig.isTrackedForExternal()) {
            healthEventRepository.deleteHealthEvents(instantEvents)
        }
    }

    private suspend fun trySendUpstreamAggregateEvents() {
        val aggregateEvents: List<CSHealthEventDTO> = healthEventRepository.getAggregateEvents()
        aggregateEvents
            .groupBy { it.eventName }
            .forEach { entry: Entry<String, List<CSHealthEventDTO>> ->
                val errorEvents: Map<String, List<CSHealthEventDTO>> =
                    entry.value.filter { it.error.isNotBlank() }.groupBy { it.error }
                if (errorEvents.isEmpty()) {
                    sendAggregateEventsBasedOnEventName(entry.value.dtosMapTo())
                } else {
                    sendAggregateEventsBasedOnError(errorEvents)
                }
            }

        // Delete Events only if TrackedVia is for External or Internal
        if (healthEventConfig.isTrackedForInternal() || healthEventConfig.isTrackedForExternal()) {
            healthEventRepository.deleteHealthEvents(aggregateEvents)
        }
    }

    private fun sendAggregateEventsBasedOnEventName(events: List<CSHealthEventEntity>) {
        fun eventIdOrEventBatchIdNotBlank(events: List<CSHealthEventEntity>): Boolean {
            return events.joinToString("") { it.eventId }.isNotBlank() ||
                    events.joinToString("") { it.eventBatchId }.isNotBlank()
        }

        val batchSize =
            if (eventIdOrEventBatchIdNotBlank(events)) MAX_BATCH_THRESHOLD else events.size

        events.chunked(batchSize)
            .forEach { batch ->
                val healthEvent = events[0].copy(
                    eventId = batch.filter { it.eventId.isNotBlank() }.joinToString { it.eventId },
                    eventBatchId = batch.filter { it.eventBatchId.isNotBlank() }
                        .joinToString { it.eventBatchId },
                    timestamp = batch.filter { it.timestamp.isNotBlank() }
                        .joinToString { it.timestamp },
                    count = batch.size
                )
                trySendToUpstream(listOf(healthEvent))
            }
    }

    private fun sendAggregateEventsBasedOnError(events: Map<String, List<CSHealthEventDTO>>) {
        events.forEach { entry: Entry<String, List<CSHealthEventDTO>> ->
            val batch: List<CSHealthEventEntity> = entry.value.dtosMapTo()
            val healthEvent = batch[0].copy(
                eventId = batch.filter { it.eventId.isNotBlank() }.joinToString { it.eventId },
                eventBatchId = batch.filter { it.eventBatchId.isNotBlank() }
                    .joinToString { it.eventBatchId },
                timestamp = batch.filter { it.timestamp.isNotBlank() }
                    .joinToString { it.timestamp },
                count = batch.size
            )
            trySendToUpstream(listOf(healthEvent))
        }
    }

    private fun isHealthEventEnabled(): Boolean {
        return healthEventConfig.isEnabled(info.appInfo.appVersion, info.userInfo.identity)
    }

    private fun trySendToUpstream(events: List<CSHealthEventEntity>) {
        events.forEach {
            if (healthEventConfig.isTrackedForBoth()) {
                healthEventLoggerListener.logEvent(
                    eventName = it.eventName,
                    healthEvent = it.mapToHealthEventDTO()
                )
            }
        }
    }

    // Flushes health events, being tracked via Clickstream, in case of an app upgrade.
    private fun flushOnAppUpgrade() {
        scopeForAppUpgrading.launch {
            val isAppVersionEqual = appVersionPreference.isAppVersionEqual(appVersion)
            if (isAppVersionEqual.not() && isActive) {
                healthEventRepository.deleteHealthEvents(healthEventRepository.getAggregateEvents())
            }
        }
    }

    private fun isTrackedViaBothOrInternal(): Boolean {
        return healthEventConfig.isTrackedForInternal() || healthEventConfig.isTrackedForBoth()
    }
}
