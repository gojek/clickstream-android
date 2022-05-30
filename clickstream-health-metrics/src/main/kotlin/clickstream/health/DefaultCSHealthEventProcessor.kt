package clickstream.health

import clickstream.lifecycle.CSAppLifeCycle
import clickstream.lifecycle.CSLifeCycleManager
import clickstream.logger.CSLogger
import clickstream.util.CSAppVersionSharedPref
import com.gojek.clickstream.internal.Health
import com.gojek.clickstream.internal.HealthDetails
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val ONE_SECOND = 1000
private const val THREE_SECOND = 3000
private const val FIVE_SECOND = 5000
private const val TEN_SECOND = 10000
private const val TWENTY_SECOND = 20000
private const val TEN_KB = 10240
private const val TWENTY_KB = 20480
private const val FIFTY_KB = 51200
private const val ONE_MS_TO_NANO = 1000000
private const val MAX_BATCH_THRESHOLD = 13

/**
 * This is being used to denote different connectivity types
 *
 */
internal enum class CSNetworkType {
    WIFI,
    MOBILE_4G,
    MOBILE_3G,
    MOBILE_2G,
    UNKNOWN
}

/**
 * The HealthEventProcessor is responsible for aggregating, sending and clearing health events for the sdk
 */
public open class DefaultCSHealthEventProcessor(
    appLifeCycleObserver: CSAppLifeCycle,
    private val healthEventRepository: CSHealthEventRepository,
    private val dispatcher: CoroutineDispatcher,
    private val healthEventConfig: CSHealthEventConfig,
    private val info: CSInfo,
    private val logger: CSLogger,
    private val healthEventLogger: CSHealthEventLogger,
    private val healthEventFactory: CSHealthEventFactory,
    private val appVersion: String,
    private val appVersionPreference: CSAppVersionSharedPref
) : CSLifeCycleManager(appLifeCycleObserver), CSHealthEventProcessor {

    private var job: Job = SupervisorJob()
    private var coroutineScope: CoroutineScope = CoroutineScope(job + dispatcher)

    init {
        logger.debug { "CSHealthEventProcessor#init" }
        addObserver()
        coroutineScope.launch {
            if (!appVersionPreference.isAppVersionEqual(appVersion) && isActive) {
                healthEventRepository.deleteHealthEvents(
                    healthEventRepository.getAggregateEvents()
                        .filter { healthEventConfig.isTrackedViaClickstream(it.eventName) })
            }
        }
    }

    override fun onStart() {
        logger.debug { "CSHealthEventProcessor#onStart" }

        if (coroutineScope.isActive) {
            coroutineScope.cancel()
        }
    }

    override fun onStop() {
        logger.debug { "CSHealthEventProcessor#onStop" }

        if (coroutineScope.isActive) {
            coroutineScope.cancel()
        }
        job = SupervisorJob()
        coroutineScope = CoroutineScope(job + dispatcher)
        sendEvents()
    }

    /**
     * Returns list of health protos after aggregation
     */
    override suspend fun getAggregateEventsBasedOnEventName(): List<Health> {
        logger.debug { "CSHealthEventProcessor#getAggregateEventsBasedOnEventName" }

        if (!healthEventConfig.destination.contains(CS_DESTINATION) || (isHealthEventEnabled().not())) {
            return emptyList()
        }
        val healthEvents = mutableListOf<Health>()
        healthEventRepository.getAggregateEvents()
            .also { list ->
                list.groupBy { it.eventName }
                    .forEach { entry ->
                        if (healthEventConfig.isTrackedViaClickstream(entry.key)) {
                            val health = createHealthProto(entry.key, entry.value)
                            healthEvents += healthEventFactory.create(health)
                            logger.debug { "CSHealthEventProcessor#getAggregateEventsBasedOnEventName - Health events: $health" }
                        }
                    }
            }.also { list -> healthEventRepository.deleteHealthEvents(list) }
        return healthEvents
    }

    /**
     * Sends all existing events in the database to server
     */
    private fun sendEvents() {
        logger.debug { "CSHealthEventProcessor#sendEvents" }

        coroutineScope.launch {
            logger.debug { "CSHealthEventProcessor#sendEvents - isCoroutineActive $isActive" }

            if (isActive.not()) {
                logger.debug { "CSHealthEventProcessor#sendEvents - coroutineScope is not longer active" }
                return@launch
            }
            if (isHealthEventEnabled().not() || healthEventConfig.destination.isEmpty()) {
                logger.debug { "CSHealthEventProcessor#sendEvents - Health Event condition is not satisfied for this user" }
                return@launch
            }
            if (healthEventConfig.destination.contains(CT_DESTINATION)) {
                logger.debug { "CSHealthEventProcessor#sendEvents - sendEventsToCleverTap" }
                sendEventsToCleverTap()
            }
        }
    }

    private fun isHealthEventEnabled(): Boolean {
        return healthEventConfig.isEnabled(info.appInfo.appVersion, info.userInfo.identity)
    }

    private suspend fun sendEventsToCleverTap() {
        sendInstantEvents()
        sendAggregateEvents()
        sendBucketEvents()
    }

    private suspend fun sendInstantEvents() {
        logger.debug { "CSHealthEventProcessor#sendInstantEvents" }

        val instantEvents = healthEventRepository.getInstantEvents()
        pushEvents(instantEvents)
        healthEventRepository.deleteHealthEvents(instantEvents)
    }

    private suspend fun sendAggregateEvents() {
        logger.debug { "CSHealthEventProcessor#sendAggregateEvents" }

        val aggregateEvents = healthEventRepository.getAggregateEvents()
        aggregateEvents
            .groupBy { it.eventName }
            .forEach { entry ->
                val errorEvents = entry.value.filter { it.error.isNotBlank() }.groupBy { it.error }
                if (errorEvents.isEmpty()) {
                    sendAggregateEventsBasedOnEventName(entry.value)
                } else {
                    sendAggregateEventsBasedOnError(errorEvents)
                }
            }
    }

    private suspend fun sendAggregateEventsBasedOnEventName(events: List<clickstream.health.CSHealthEvent>) {
        logger.debug { "CSHealthEventProcessor#sendAggregateEventsBasedOnEventName" }

        val batchSize = if (events.joinToString("") { it.eventId }.isNotBlank() ||
            events.joinToString("") { it.eventBatchId }.isNotBlank()
        ) MAX_BATCH_THRESHOLD else events.size

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
                pushEvents(listOf(healthEvent))
            }
    }

    private suspend fun sendAggregateEventsBasedOnError(events: Map<String, List<clickstream.health.CSHealthEvent>>) {
        logger.debug { "CSHealthEventProcessor#sendAggregateEventsBasedOnError" }

        events.forEach { entry ->
            val batch = entry.value
            val healthEvent = batch[0].copy(
                eventId = batch.filter { it.eventId.isNotBlank() }.joinToString { it.eventId },
                eventBatchId = batch.filter { it.eventBatchId.isNotBlank() }
                    .joinToString { it.eventBatchId },
                timestamp = batch.filter { it.timestamp.isNotBlank() }
                    .joinToString { it.timestamp },
                count = batch.size
            )
            pushEvents(listOf(healthEvent))
        }
    }

    private suspend fun sendBucketEvents() {
        logger.debug { "CSHealthEventProcessor#sendBucketEvents" }

        val bucketEvents = healthEventRepository.getBucketEvents()
        val events = bucketEvents.map { event ->
            val eventName = event.eventName
            val bucketType = when {
                eventName == CSEventNames.ClickStreamEventBatchLatency.value &&
                        event.stopTime > event.startTime -> {
                    getBucketTypeForBatchLatency(
                        event.startTime,
                        event.stopTime,
                        event.networkType
                    )
                }
                eventName == CSEventNames.ClickStreamEventWaitTime.value &&
                        event.stopTime > event.startTime -> {
                    getBucketTypeForWaitTime(event.startTime, event.stopTime)
                }
                eventName == CSEventNames.ClickStreamEventBatchWaitTime.value &&
                        event.stopTime > event.startTime ->
                    getBucketTypeForWaitTime(event.startTime, event.stopTime)
                eventName == CSEventNames.ClickStreamBatchSize.value ->
                    getBucketTypeForBatchSize(event.batchSize)
                else -> event.bucketType
            }
            event.copy(bucketType = bucketType)
        }.toList()
        aggregateBuckets(events)
        healthEventRepository.deleteHealthEvents(bucketEvents)
    }

    private fun getBucketTypeForBatchLatency(
        startTime: Long,
        stopTime: Long,
        networkType: String
    ): String {
        logger.debug { "CSHealthEventProcessor#getBucketTypeForBatchLatency" }

        val latencyInMs = (stopTime - startTime) / ONE_MS_TO_NANO
        return when {
            networkType == CSNetworkType.WIFI.name && latencyInMs <= ONE_SECOND -> CSBucketTypes.LT_1sec_WIFI
            networkType == CSNetworkType.WIFI.name && latencyInMs <= THREE_SECOND -> CSBucketTypes.MT_1sec_WIFI
            networkType == CSNetworkType.WIFI.name && latencyInMs > THREE_SECOND -> CSBucketTypes.MT_3sec_WIFI
            networkType == CSNetworkType.MOBILE_4G.name && latencyInMs <= ONE_SECOND -> CSBucketTypes.LT_1sec_4G
            networkType == CSNetworkType.MOBILE_4G.name && latencyInMs <= THREE_SECOND -> CSBucketTypes.MT_1sec_4G
            networkType == CSNetworkType.MOBILE_4G.name && latencyInMs > THREE_SECOND -> CSBucketTypes.MT_3sec_4G
            networkType == CSNetworkType.MOBILE_3G.name && latencyInMs <= ONE_SECOND -> CSBucketTypes.LT_1sec_3G
            networkType == CSNetworkType.MOBILE_3G.name && latencyInMs <= THREE_SECOND -> CSBucketTypes.MT_1sec_3G
            networkType == CSNetworkType.MOBILE_3G.name && latencyInMs > THREE_SECOND -> CSBucketTypes.MT_3sec_3G
            networkType == CSNetworkType.MOBILE_2G.name && latencyInMs <= ONE_SECOND -> CSBucketTypes.LT_1sec_2G
            networkType == CSNetworkType.MOBILE_2G.name && latencyInMs <= THREE_SECOND -> CSBucketTypes.MT_1sec_2G
            networkType == CSNetworkType.MOBILE_2G.name && latencyInMs > THREE_SECOND -> CSBucketTypes.MT_3sec_2G
            else -> ""
        }
    }

    private fun getBucketTypeForWaitTime(
        startTime: Long,
        stopTime: Long
    ): String {
        logger.debug { "CSHealthEventProcessor#getBucketTypeForWaitTime" }

        val latencyInMs = (stopTime - startTime) / ONE_MS_TO_NANO
        return when {
            latencyInMs <= FIVE_SECOND -> CSBucketTypes.LT_5sec
            latencyInMs <= TEN_SECOND -> CSBucketTypes.LT_10sec
            latencyInMs <= TWENTY_SECOND -> CSBucketTypes.MT_10sec
            else -> CSBucketTypes.MT_20sec
        }
    }

    private fun getBucketTypeForBatchSize(batchSize: Long): String {
        logger.debug { "CSHealthEventProcessor#getBucketTypeForBatchSize" }

        return when {
            batchSize <= TEN_KB -> CSBucketTypes.LT_10KB
            batchSize <= TWENTY_KB -> CSBucketTypes.MT_10KB
            batchSize <= FIFTY_KB -> CSBucketTypes.MT_20KB
            else -> CSBucketTypes.MT_50KB
        }
    }

    private suspend fun aggregateBuckets(bucketEvents: List<clickstream.health.CSHealthEvent>) {
        logger.debug { "CSHealthEventProcessor#aggregateBuckets" }

        val groupByEventNames = bucketEvents.groupBy { it.eventName }
        groupByEventNames.forEach { entry ->
            val groupByBucketType = entry.value.groupBy { it.bucketType }
            groupByBucketType.forEach {
                val batches = it.value.chunked(MAX_BATCH_THRESHOLD)
                batches.forEach { batch ->
                    val healthEvent = batch[0].copy(
                        eventId = batch.filter { it.eventId.isNotBlank() }
                            .joinToString { it.eventId },
                        eventBatchId = batch.filter { it.eventBatchId.isNotBlank() }
                            .joinToString { it.eventBatchId },
                        timestamp = batch.filter { it.timestamp.isNotBlank() }
                            .joinToString { it.timestamp },
                        count = batch.size
                    )
                    pushEvents(listOf(healthEvent))
                }
            }
        }
    }

    private suspend fun pushEvents(events: List<clickstream.health.CSHealthEvent>) {
        logger.debug { "CSHealthEventProcessor#pushEvents" }

        events.forEach {
            if (!healthEventConfig.isTrackedViaClickstream(it.eventName)) {
                healthEventLogger.logEvent(eventName = it.eventName, eventData = it.eventData())
                healthEventRepository.deleteHealthEvents(events)
            }
        }
    }

    private fun createHealthProto(
        eventName: String,
        events: List<clickstream.health.CSHealthEvent>
    ): Health {
        logger.debug { "CSHealthEventProcessor#createHealthProto" }

        val eventGuids = mutableListOf<String>()
        events.forEach { event ->
            val eventIdArray = event.eventId.split(",").map { it.trim() }
            eventGuids += eventIdArray
        }

        val eventBatchGuids =
            events.filter { it.eventBatchId.isNotBlank() }.map { it.eventBatchId }

        logger.debug { "CSHealthEventProcessor#createHealthProto - eventGuids $eventGuids" }
        logger.debug { "CSHealthEventProcessor#createHealthProto - eventBatchGuids $eventBatchGuids" }

        return Health.newBuilder().apply {
            this.eventName = eventName
            numberOfEvents = eventGuids.size.toLong()
            numberOfBatches = eventBatchGuids.size.toLong()

            logger.debug { "CSHealthEventProcessor#createHealthProto# - isVerboseLoggingEnabled ${healthEventConfig.isVerboseLoggingEnabled()}" }
            logger.debug {
                "CSHealthEventProcessor#createHealthProto# - isExemptedFromVerbosityCheck ${
                    isExemptedFromVerbosityCheck(
                        eventName
                    )
                }"
            }

            if (healthEventConfig.isVerboseLoggingEnabled() || isExemptedFromVerbosityCheck(
                    eventName
                )
            ) {
                healthDetails = HealthDetails.newBuilder().apply {
                    addAllEventGuids(eventGuids)
                    addAllEventBatchGuids(eventBatchGuids)
                }.build()

                logger.debug { "CSHealthEventProcessor#createHealthProto - HealthDetails $healthDetails" }
            }
        }.build()
    }

    private fun isExemptedFromVerbosityCheck(eventName: String): Boolean {
        logger.debug { "CSHealthEventProcessor#isExemptedFromVerbosityCheck" }

        return eventName == CSEventNames.ClickStreamEventReceived.value
    }
}
