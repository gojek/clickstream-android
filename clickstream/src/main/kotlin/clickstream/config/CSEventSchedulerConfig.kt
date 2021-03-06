package clickstream.config

import java.util.concurrent.TimeUnit.SECONDS

/**
 * EventSchedulerConfig holds the configuration properties
 * for the EventScheduler to process the event data
 *
 * @param eventsPerBatch Number of events to combine in a single request
 * @param batchPeriod Delay between batches
 * @param flushOnBackground Flag for enabling forced flushing of events
 * @param connectionTerminationTimerWaitTimeInMillis Wait time after which socket gets disconnected
 * @param backgroundTaskEnabled Flag for enabling flushing of events by background task
 * @param workRequestDelayInHr Initial delay for background task
 */
public data class CSEventSchedulerConfig(
    val eventsPerBatch: Int,
    val batchPeriod: Long,
    val flushOnBackground: Boolean,
    val connectionTerminationTimerWaitTimeInMillis: Long,
    val backgroundTaskEnabled: Boolean,
    val workRequestDelayInHr: Long,
    val utf8ValidatorEnabled: Boolean
) {

    public companion object {

        private const val DEFAULT_EVENTS_PER_BATCH: Int = 20
        private const val DEFAULT_BATCH_PERIOD: Long = 10000
        private const val FORCED_FLUSH: Boolean = false
        private const val BACKGROUND_TASK_ENABLED: Boolean = false
        private const val CONNECTION_TERMINATION_TIMER_WAIT_TIME_IN_S: Long = 5
        private const val WORK_REQUEST_DELAY_TIME_IN_HR: Long = 1
        private const val UTF8_VALIDATOR_ENABLED: Boolean = true

        /**
         * Returns the config with default values
         */
        public fun default(): CSEventSchedulerConfig = CSEventSchedulerConfig(
            DEFAULT_EVENTS_PER_BATCH, DEFAULT_BATCH_PERIOD,
            FORCED_FLUSH, SECONDS.toMillis(CONNECTION_TERMINATION_TIMER_WAIT_TIME_IN_S),
            BACKGROUND_TASK_ENABLED,
            WORK_REQUEST_DELAY_TIME_IN_HR,
            UTF8_VALIDATOR_ENABLED
        )
    }
}
