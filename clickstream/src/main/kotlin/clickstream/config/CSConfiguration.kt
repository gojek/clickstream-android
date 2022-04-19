package clickstream.config

import android.content.Context
import clickstream.config.timestamp.CSEventGeneratedTimestampListener
import clickstream.config.timestamp.DefaultCSEventGeneratedTimestampListener
import clickstream.connection.CSSocketConnectionListener
import clickstream.connection.NoOpCSConnectionListener
import clickstream.internal.di.CSServiceLocator
import clickstream.logger.CSLogLevel
import clickstream.model.CSInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * The [CSConfiguration] object used to customize [ClickStream] upon initialization.
 * [CSConfiguration] contains various parameters used to setup [ClickStream].
 * For example, it is possible to customize the [CoroutineScope] used by [CSServiceLocator]s here.
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
    internal val socketConnectionListener: CSSocketConnectionListener
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
         *  - UserInfo
         *  - SessionInfo
         *  - LocationInfo
         *  - DeviceInfo
         */
        private val info: CSInfo,

        /**
         * Specifies a [CSConfig] which will be used by [ClickStream] for all its
         * internal meta information, such as.
         *  - EventProcessor, to define Instant and Realtime events which respect to QoS0/1.
         *  - EventScheduler, to define configuration properties for the EventScheduler to process the event data.
         *  - NetworkConfig, to define endpoint and network configuration related things.
         */
        private val config: CSConfig
    ) {
        private lateinit var dispatcher: CoroutineDispatcher
        private lateinit var eventGeneratedListener: CSEventGeneratedTimestampListener
        private lateinit var socketConnectionListener: CSSocketConnectionListener
        private var logLevel: CSLogLevel = CSLogLevel.OFF

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
            if (::socketConnectionListener.isInitialized.not()) {
                socketConnectionListener = NoOpCSConnectionListener()
            }
            return CSConfiguration(
                context, dispatcher,
                info, config,
                logLevel,
                eventGeneratedListener,
                socketConnectionListener
            )
        }
    }
}
