package clickstream.internal.eventscheduler.impl

import clickstream.config.CSEventSchedulerConfig
import clickstream.internal.CSEventInternal
import clickstream.internal.db.CSBatchSizeSharedPref
import clickstream.internal.eventscheduler.CSEventBatchSizeStrategy
import clickstream.internal.eventscheduler.DEFAULT_MIN_EVENT_COUNT
import clickstream.logger.CSLogger
import java.util.concurrent.atomic.AtomicInteger

/**
 * Provides the regulated number of events for batch.
 */
internal class EventByteSizeBasedBatchStrategy(
    private val logger: CSLogger,
    private val csBatchSizeSharedPref: CSBatchSizeSharedPref
) : CSEventBatchSizeStrategy {

    private var totalDataFlow = AtomicInteger(0)
    private var totalEventCount = AtomicInteger(0)

    /**
     *
     * Calculates the count of events required to make the payload size of [CSEventSchedulerConfig.eventsPerBatch]
     * based on average size of events flowing in to the SDK.
     *
     */
    override suspend fun regulatedCountOfEventsPerBatch(expectedBatchSize: Int): Int {
        var regulatedCount: Int
        if (totalDataFlow.get() == 0 || totalEventCount.get() == 0) {
            regulatedCount = DEFAULT_MIN_EVENT_COUNT
        } else {
            val avgSizeOfEvents = totalDataFlow.get() / totalEventCount.get()
            regulatedCount = expectedBatchSize / avgSizeOfEvents
            // Rare case if average byte size of events is more than expected batch size.
            if (regulatedCount < 1) {
                regulatedCount = DEFAULT_MIN_EVENT_COUNT
            }
            logger.debug {
                """EventByteSizeBasedBatchStrategy#regulatedCountOfEventsPerBatch 
            totalDataFlow: $totalDataFlow
            totalEventCount: $totalEventCount
            average Size of Events: $avgSizeOfEvents
            regulated count for $expectedBatchSize: $regulatedCount
        """.trimMargin()
            }
        }
        csBatchSizeSharedPref.saveBatchSize(regulatedCount)
        return regulatedCount
    }

    /**
     * Provides the tracked event to calculate regulated number of events per batch
     */
    override fun logEvent(event: CSEventInternal) {
        val dataSize = when (event) {
            is CSEventInternal.CSBytesEvent -> event.eventData.size
            is CSEventInternal.CSEvent -> event.message.serializedSize
        }
        totalDataFlow.getAndAdd(dataSize)
        totalEventCount.getAndIncrement()
    }
}