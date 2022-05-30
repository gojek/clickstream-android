package clickstream.internal.di

import androidx.annotation.GuardedBy
import clickstream.analytics.event.CSEventHealthListener
import clickstream.config.CSEventSchedulerConfig
import clickstream.health.CSHealthEventFactory
import clickstream.health.CSHealthEventProcessor
import clickstream.health.CSHealthEventRepository
import clickstream.internal.eventprocessor.CSEventProcessor
import clickstream.internal.eventscheduler.CSBackgroundScheduler
import clickstream.internal.eventscheduler.CSEventScheduler
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.workmanager.CSWorkManager
import clickstream.logger.CSLogLevel
import clickstream.logger.CSLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * The DI pattern which will be implemented to create dependencies.
 */
@ExperimentalCoroutinesApi
internal interface CSServiceLocator {

    companion object {

        /**
         * Default instance of the ServiceLocator which can be used across the SDK if required
         */
        @Volatile
        @GuardedBy("lock")
        private var sInstance: CSServiceLocator? = null
        private val lock = Any()

        /**
         * Returns a singleton instance of [CSServiceLocator]
         */
        fun getInstance(): CSServiceLocator {
            if (sInstance == null) {
                synchronized(lock) {
                    if (sInstance == null) {
                        requireNotNull(sInstance) {
                            "Service Locator should be created and set by using " +
                                    "[setServiceLocator] function."
                        }
                    }
                }
            }
            return sInstance!!
        }

        /**
         * Sets the current instance of the service locator
         */
        fun setServiceLocator(serviceLocator: CSServiceLocator) {
            sInstance = serviceLocator
        }
    }

    /**
     * The Coroutine dispatcher which is used for the scope
     */
    val dispatcher: CoroutineDispatcher

    /**
     * Network Manage which communicates with the backend
     */
    val networkManager: CSNetworkManager

    /**
     * Event scheduler which schedules and dispatches events to the backend
     */
    val eventScheduler: CSEventScheduler

    /**
     * EventProcessor which processes and dispatches to scheduler
     */
    val eventProcessor: CSEventProcessor

    /**
     * The background work manager which flushes the event
     */
    val workManager: CSWorkManager

    /**
     * The Background scheduler which flushes the event
     */
    val backgroundScheduler: CSBackgroundScheduler

    /**
     * Configuration reference to hold metadata
     */
    val eventSchedulerConfig: CSEventSchedulerConfig

    /**
     * Types for LogLevel config during development or production.
     */
    val logLevel: CSLogLevel

    /**
     * Internal Logger
     */
    val logger: CSLogger

    val eventHealthListener: CSEventHealthListener
    val healthEventRepository: CSHealthEventRepository
    val healthEventProcessor: CSHealthEventProcessor
    val healthEventFactory: CSHealthEventFactory
}
