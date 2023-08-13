package clickstream.internal.di

import androidx.annotation.GuardedBy
import clickstream.config.CSEventSchedulerConfig
import clickstream.health.intermediate.CSEventHealthListener
import clickstream.health.intermediate.CSHealthEventFactory
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.internal.eventprocessor.CSEventProcessor
import clickstream.internal.eventscheduler.CSBackgroundEventScheduler
import clickstream.internal.eventscheduler.CSForegroundEventScheduler
import clickstream.internal.eventscheduler.CSWorkManagerEventScheduler
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.workmanager.CSEventFlushOneTimeWorkManager
import clickstream.internal.workmanager.CSEventFlushPeriodicWorkManager
import clickstream.internal.workmanager.CSWorkManager
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.lifecycle.CSBackgroundLifecycleManager
import clickstream.listener.CSEventListener
import clickstream.logger.CSLogLevel
import clickstream.logger.CSLogger
import com.tinder.scarlet.lifecycle.LifecycleRegistry
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
                            "CSServiceLocator is not initialized yet, " +
                            "please call setServiceLocator#release first. " +
                            "See CSServiceLocator#setServiceLocator(CSServiceLocator) or " +
                            "the class level. " +
                            "KotlinDoc for more information."
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

        /**
         * Dispose services
         */
        fun release() {
            sInstance?.foregroundLifecycleRegistry?.onComplete()
            sInstance?.backgroundLifecycleManager?.lifecycleRegistry?.onComplete()
            sInstance?.foregroundEventScheduler?.cancelJob()
            sInstance?.backgroundEventScheduler?.cancelJob()
            sInstance?.foregroundNetworkManager?.cancelJob()

            // cancel enqueue work
            sInstance?.workManager?.context?.run {
                CSEventFlushPeriodicWorkManager.cancelWork(this)
                CSEventFlushOneTimeWorkManager.cancelWork(this)
            }
            sInstance = null
        }
    }

    /**
     * The Coroutine dispatcher which is used for the scope
     */
    val dispatcher: CoroutineDispatcher

    /**
     * Network Manage which communicates with the backend
     */
    val foregroundNetworkManager: CSNetworkManager

    /**
     * Event scheduler which schedules and dispatches events to the backend
     */
    val foregroundEventScheduler: CSForegroundEventScheduler

    /**
     * Event scheduler which schedules and dispatches events to the backend
     */
    val backgroundEventScheduler: CSBackgroundEventScheduler

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
    val workManagerEventScheduler: CSWorkManagerEventScheduler

    /**
     * Configuration reference to hold metadata
     */
    val eventSchedulerConfig: CSEventSchedulerConfig

    /**
     * Types for LogLevel config during development or production.
     */
    val logLevel: CSLogLevel

    val foregroundLifecycleRegistry: LifecycleRegistry

    val backgroundLifecycleManager: CSBackgroundLifecycleManager

    /**
     * Internal Logger
     */
    val logger: CSLogger

    /**
     * [CSEventHealthListener] Essentially an optional listener which being used for
     * perform an analytic metrics to check every event size. We're exposed listener
     * so that if the host app wants to check each event size they can simply add the listener.
     *
     * Proto `MessageLite` provide an API that we're able to use to check the byte size which is
     * [messageSerializedSizeInBytes].
     *
     * **Example:**
     * ```kotlin
     * private fun applyEventHealthMetrics(config: CSClickStreamConfig): CSEventHealthListener {
     *     if (config.isEventHealthListenerEnabled.not()) return NoOpCSEventHealthListener()
     *
     *     return object : CSEventHealthListener {
     *         override fun onEventCreated(healthEvent: CSEventHealth) {
     *             executor.execute {
     *                 val trace = FirebasePerformance.getInstance().newTrace("CS_Event_Health_Metrics")
     *                 trace.start()
     *                 trace.putMetric(
     *                     healthEvent.messageName,
     *                     healthEvent.messageSerializedSizeInBytes.toLong()
     *                 )
     *                 trace.stop()
     *             }
     *        }
     *    }
     * }
     * ```
     */
    val eventHealthListener: CSEventHealthListener

    /**
     * [CSHealthEventRepository] Act as repository pattern where internally it doing DAO operation
     * to insert, delete, and read the [CSHealthEvent]'s.
     *
     * If you're using `com.gojek.clickstream:clickstream-health-metrics-noop`, the
     * [CSHealthEventRepository] internally will doing nothing.
     *
     * Do consider to use `com.gojek.clickstream:clickstream-health-metrics`, to operate
     * [CSHealthEventRepository] as expected. Whenever you opt in the `com.gojek.clickstream:clickstream-health-metrics`,
     * you should never touch the [DefaultCSHealthEventRepository] explicitly. All the wiring
     * is happening through [DefaultCSHealthGateway.factory(/*args*/)]
     */
    val healthEventRepository: CSHealthEventRepository

    /**
     * [CSHealthEventProcessor] is the Heart of the Clickstream Library. The [CSHealthEventProcessor]
     * is only for pushing events to the backend. [CSHealthEventProcessor] is respect to the
     * Application lifecycle where on the active state, we have a ticker that will collect events from database
     * and the send that to the backend. The ticker will run on every 10seconds and will be stopped
     * whenever application on the inactive state.
     *
     * On the inactive state we will running flush for both Events and HealthEvents, where
     * it would be transformed and send to the backend.
     */
    val healthEventProcessor: CSHealthEventProcessor

    /**
     * [CSHealthEventFactory] is act as proxy that would mutate the meta of any incoming
     * [Health] events. The mutation is needed in order to set value in the meta that would being
     * used by the internal metrics.
     */
    val healthEventFactory: CSHealthEventFactory

    /**
     * [CSAppLifeCycle] is an interface which provides onStart and onStop lifecycle based
     * on the concrete implementation, as for now we have 2 implementation, such as:
     * - [DefaultCSAppLifeCycleObserver] which respect to the Application Lifecycle
     * - [DefaultCSActivityLifeCycleObserver] which respect to the Activities Lifecycle
     */
    val appLifeCycle: CSAppLifeCycle

    val eventListener: List<CSEventListener>
}
