package clickstream.config

import android.content.Context
import clickstream.api.CSDeviceInfo
import clickstream.api.CSInfo
import clickstream.config.timestamp.DefaultCSEventGeneratedTimestampListener
import clickstream.connection.CSSocketConnectionListener
import clickstream.connection.NoOpCSConnectionListener
import clickstream.health.intermediate.CSEventHealthListener
import clickstream.health.intermediate.CSHealthEventFactory
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.CSHealthGateway
import clickstream.health.NoOpCSHealthGateway
import clickstream.health.time.CSEventGeneratedTimestampListener
import clickstream.internal.di.CSServiceLocator
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.logger.CSLogLevel
import clickstream.listener.CSEventListener
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
    internal val logLevel: CSLogLevel,
    internal val eventGeneratedTimeStamp: CSEventGeneratedTimestampListener,
    internal val socketConnectionListener: CSSocketConnectionListener,
    internal val remoteConfig: CSRemoteConfig,
    internal val eventHealthListener: CSEventHealthListener,
    internal val healthEventRepository: CSHealthEventRepository,
    internal val healthEventProcessor: CSHealthEventProcessor,
    internal val healthEventFactory: CSHealthEventFactory,
    internal val appLifeCycle: CSAppLifeCycle,
    internal val eventListeners: List<CSEventListener> = listOf(),
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
        private val config: CSConfig,

        /**
         * Specify Clicstream lifecycle, this is needed in order to send events
         * to the backend.
         */
        private val appLifeCycle: CSAppLifeCycle
    ) {
        private lateinit var dispatcher: CoroutineDispatcher
        private lateinit var eventGeneratedListener: CSEventGeneratedTimestampListener
        private lateinit var socketConnectionListener: CSSocketConnectionListener
        private lateinit var remoteConfig: CSRemoteConfig
        private lateinit var healthGateway: CSHealthGateway
        private var logLevel: CSLogLevel = CSLogLevel.OFF
        private val eventListeners = mutableListOf<CSEventListener>()

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
         * Specify implementation of [CSHealthGateway], by default it would use
         * [NoOpCSHealthGateway]
         *
         * @return This [Builder] instance
         */
        public fun setHealthGateway(healthGateway: CSHealthGateway): Builder =
            apply {
                this.healthGateway = healthGateway
            }


        /**
         * Configure a single client scoped listener that will receive all analytic events
         * for this client.
         *
         * @see CSEventListener for semantics and restrictions on listener implementations.
         */
        public fun addEventListener(eventListener: CSEventListener): Builder = apply {
            this.eventListeners.add(eventListener)
        }

        public fun build(): CSConfiguration {
            if (::dispatcher.isInitialized.not()) {
                dispatcher = Dispatchers.Default
            }
            if (::eventGeneratedListener.isInitialized.not()) {
                eventGeneratedListener = DefaultCSEventGeneratedTimestampListener()
            }
            if (::socketConnectionListener.isInitialized.not()) {
                socketConnectionListener = NoOpCSConnectionListener()
            }
            if (::remoteConfig.isInitialized.not()) {
                remoteConfig = NoOpCSRemoteConfig()
            }
            if (::healthGateway.isInitialized.not()) {
                healthGateway = NoOpCSHealthGateway.factory()
            }
            return CSConfiguration(
                context, dispatcher,
                info, config,
                logLevel,
                eventGeneratedListener,
                socketConnectionListener,
                remoteConfig,
                healthGateway.eventHealthListener,
                healthGateway.healthEventRepository,
                healthGateway.healthEventProcessor,
                healthGateway.healthEventFactory,
                appLifeCycle,
                eventListeners
            )
        }
    }
}
