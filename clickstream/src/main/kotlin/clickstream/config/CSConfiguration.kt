package clickstream.config

import android.content.Context
import clickstream.api.CSInfo
import clickstream.config.timestamp.CSEventGeneratedTimestampListener
import clickstream.config.timestamp.DefaultCSEventGeneratedTimestampListener
import clickstream.connection.CSSocketConnectionListener
import clickstream.connection.NoOpCSConnectionListener
import clickstream.health.CSHealthGateway
import clickstream.health.NoOpCSHealthGateway
import clickstream.health.intermediate.CSEventHealthListener
import clickstream.health.intermediate.CSHealthEventLoggerListener
import clickstream.internal.di.CSServiceLocator
import clickstream.internal.eventscheduler.CSEventSchedulerErrorListener
import clickstream.internal.eventscheduler.impl.NoOpEventSchedulerErrorListener
import clickstream.internal.health.NoOpCSHealthEventListener
import clickstream.internal.health.NoOpCSHealthEventLoggerListener
import clickstream.listener.CSEventListener
import clickstream.logger.CSLogLevel
import clickstream.report.CSReportDataListener
import clickstream.report.CSReportDataTracker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * The [CSConfiguration] object used to customize [ClickStream] upon initialization.
 * [CSConfiguration] contains various parameters used to setup [ClickStream].
 * For example, it is possible to customize the [CoroutineScope] used by [CSServiceLocator]s here.
 *
 * @param context A [Context] object for configuration purposes. Internally, this class
 *        will call [Context#getApplicationContext()], so you may safely pass in
 *        any Context without risking a memory leak.
 * @param dispatcher A [CoroutineDispatcher] object for threading related work.
 * @param info An object that wraps [CSAppInfo], [CSLocationInfo], [CSUserInfo], [CSSessionInfo], [CSDeviceInfo]
 * @param config An object which holds the configuration for processor, scheduler & network manager
 * @param healthLogger An object which hold the configuration for Health Metrics.
 * @param logLevel ClickStream Loglevel for debugging purposes.
 * @param eventGeneratedTimeStamp An object which provide a plugin for exposes a timestamp where call side able to use
 *        for provides NTP timestamp
 * @param socketConnectionListener An interface that provides onEventConnectionChange.
 *
 * To set a custom Configuration for [ClickStream], **See** [ClickStream#initialize(Context, Configuration)].
 */
public class CSConfiguration private constructor(
    internal val context: Context,
    internal val dispatcher: CoroutineDispatcher,
    internal val info: CSInfo,
    internal val config: CSConfig,
    internal val healthLogger: CSHealthEventLoggerListener,
    internal val logLevel: CSLogLevel,
    internal val eventGeneratedTimeStamp: CSEventGeneratedTimestampListener,
    internal val socketConnectionListener: CSSocketConnectionListener,
    internal val remoteConfig: CSRemoteConfig,
    internal val eventHealthListener: CSEventHealthListener,
    internal val healthGateway: CSHealthGateway,
    internal val eventListeners: List<CSEventListener> = listOf(),
    internal val eventSchedulerErrorListener: CSEventSchedulerErrorListener,
    internal val csReportDataTracker: CSReportDataTracker?

) {
    /**
     * A Builder for [CSConfiguration]'s.
     */
    public class Builder(
        /**
         * A [Context] object for configuration purposes. Internally, this class will call
         * [Context#getApplicationContext()], so you may safely pass in any Context without
         * risking a memory leak.
         */
        private val context: Context,

        /**
         * Specifies a [CSInfo] which will be used by [ClickStream] for all its
         * internal meta information, such as.
         *  - AppInfo
         *  - CustomerInfo
         *  - SessionInfo
         *  - LocationInfo
         *  - DeviceInfo
         */
        private val info: CSInfo,

        /**
         * Specifies a [CSConfig] which will be used by [ClickStream] for all its
         * internal meta information, such as.
         *  - EventProcessor, to define Instant and Realtime events.
         *  - EventScheduler, for worker related meta.
         *  - NetworkConfig, to define endpoint and timout related things.
         *  - HealthConfig, to define verbosity and health related things.
         */
        private val config: CSConfig
    ) {
        private lateinit var dispatcher: CoroutineDispatcher
        private lateinit var eventGeneratedListener: CSEventGeneratedTimestampListener
        private lateinit var socketConnectionListener: CSSocketConnectionListener
        private lateinit var eventHealthListener: CSEventHealthListener
        private lateinit var healthListener: CSHealthEventLoggerListener
        private lateinit var healthGateway: CSHealthGateway
        private lateinit var remoteConfig: CSRemoteConfig
        private var logLevel: CSLogLevel = CSLogLevel.OFF
        private val eventListeners = mutableListOf<CSEventListener>()
        private lateinit var eventSchedulerErrorListener: CSEventSchedulerErrorListener
        private var csReportDataTracker: CSReportDataTracker? = null

        /**
         * Specifies a custom [CoroutineDispatcher] for [ClickStream].
         *
         * @param dispatcher An [CoroutineDispatcher] for running workers.
         * @return This [Builder] instance
         */
        public fun setDispatcher(dispatcher: CoroutineDispatcher): Builder = apply {
            this.dispatcher = dispatcher
        }

        /**
         * Specifies a custom [CSHealthEventLogger] for [ClickStream].
         *
         * @param health A [CSHealthEventLogger] for creating custom health tracker's.
         * @return This [Builder] instance
         */
        public fun setHealthListener(health: CSHealthEventLoggerListener): Builder = apply {
            this.healthListener = health
        }

        /**
         * Specifies a custom [CSEventHealthListener] for [ClickStream].
         *
         * @param health A [CSEventHealthListener] for creating custom health tracker's.
         * @return This [Builder] instance
         */
        public fun setEventHealthListener(health: CSEventHealthListener): Builder = apply {
            this.eventHealthListener = health
        }

        /**
         * Specifies the minimum logging level, corresponding to the constants found in
         * [CSLogLevel]. For example, specifying [CSLogLevel.DEBUG] will
         * log everything, whereas specifying [CSLogLevel.OFF] will not log anything.
         *
         * @param level The minimum logging level, corresponding to the constants found in
         *        [CSLogLevel].
         * @return This [Builder] instance
         */
        public fun setLogLevel(level: CSLogLevel): Builder = apply {
            this.logLevel = level
        }

        /**
         * Specifies a custom [CSEventGeneratedTimestampListener] for [ClickStream].
         *
         * @param listener A [CSEventGeneratedTimestampListener] for creating custom event generated
         *        timestamp.
         * @return This [Builder] instance
         */
        public fun setEventGeneratedTimestamp(listener: CSEventGeneratedTimestampListener): Builder =
            apply {
                this.eventGeneratedListener = listener
            }

        /**
         * Specifies a custom [CSSocketConnectionListener] for [ClickStream].
         *
         * @param listener A [CSSocketConnectionListener] for observe websocket connection events.
         * @return This [Builder] instance
         */
        public fun setSocketConnectionListener(listener: CSSocketConnectionListener): Builder =
            apply {
                this.socketConnectionListener = listener
            }

        /**
         * Add any [CSEventInterceptor] to intercept clickstream events.
         *
         * @return This [Builder] instance
         */
        public fun addEventListener(listener: CSEventListener): Builder = apply {
            this.eventListeners.add(listener)
        }

        /**
         * Specifies a custom [CSRemoteConfig] for [ClickStream].
         *
         * @param remoteConfig A [CSRemoteConfig] for remote config.
         * @return This [Builder] instance
         */
        public fun setRemoteConfig(remoteConfig: CSRemoteConfig): Builder =
            apply {
                this.remoteConfig = remoteConfig
            }

        /**
         * Specifies a custom [CSEventSchedulerErrorListener] for Event schedulers.
         *
         * @param eventSchedulerErrorListener A [CSEventSchedulerErrorListener]
         * @return This [Builder] instance
         */
        public fun setEventSchedulerErrorListener(eventSchedulerErrorListener: CSEventSchedulerErrorListener): Builder =
            apply {
                this.eventSchedulerErrorListener = eventSchedulerErrorListener
            }

        /**
         * Specifies a custom [CSReportDataListener] for Clickstream.
         * This should only be set on non prod build for generating reports because of memory overhead.
         *
         * @param csBatchReportListener A [CSReportDataListener]
         * @return This [Builder] instance
         */
        public fun setReportDataListener(csBatchReportListener: CSReportDataListener): Builder =
            apply {
                this.csReportDataTracker = CSReportDataTracker(csBatchReportListener)
            }

        public fun setHealthGateway(csHealthGateway: CSHealthGateway): Builder =
            apply {
                this.healthGateway = csHealthGateway
            }


        /**
         * Builds a [CSConfiguration] object.
         *
         * @return A [CSConfiguration] object with this [Builder]'s parameters.
         */
        public fun build(): CSConfiguration {
            if (::dispatcher.isInitialized.not()) {
                dispatcher = Dispatchers.Default
            }
            if (::eventGeneratedListener.isInitialized.not()) {
                eventGeneratedListener = DefaultCSEventGeneratedTimestampListener()
            }
            if (::healthListener.isInitialized.not()) {
                healthListener = NoOpCSHealthEventLoggerListener()
            }
            if (::socketConnectionListener.isInitialized.not()) {
                socketConnectionListener = NoOpCSConnectionListener()
            }
            if (::remoteConfig.isInitialized.not()) {
                remoteConfig = NoOpCSRemoteConfig()
            }
            if (::eventHealthListener.isInitialized.not()) {
                eventHealthListener = NoOpCSHealthEventListener()
            }
            if (::eventSchedulerErrorListener.isInitialized.not()) {
                eventSchedulerErrorListener = NoOpEventSchedulerErrorListener()
            }

            if (::healthGateway.isInitialized.not()) {
                healthGateway = NoOpCSHealthGateway.factory()
            }
            return CSConfiguration(
                context, dispatcher,
                info, config,
                healthListener, logLevel,
                eventGeneratedListener,
                socketConnectionListener,
                remoteConfig,
                eventHealthListener,
                healthGateway,
                eventListeners,
                eventSchedulerErrorListener,
                csReportDataTracker
            )
        }
    }
}
