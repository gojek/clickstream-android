package clickstream.internal

import androidx.annotation.GuardedBy
import androidx.annotation.RestrictTo
import clickstream.CSEvent
import clickstream.ClickStream
import clickstream.config.CSConfiguration
import clickstream.internal.di.CSServiceLocator
import clickstream.internal.di.impl.DefaultCServiceLocator
import clickstream.internal.eventprocessor.CSEventProcessor
import clickstream.internal.workmanager.CSWorkManager
import clickstream.logger.CSLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
internal class DefaultClickStream private constructor(
    private val processor: CSEventProcessor,
    private val service: CSWorkManager,
    private val logger: CSLogger,
    dispatcher: CoroutineDispatcher
) : ClickStream {

    private val handler: CoroutineExceptionHandler by lazy {
        CoroutineExceptionHandler { _, throwable ->
            logger.debug { throwable.message.toString() }
        }
    }
    private val scope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + dispatcher + handler)
    }

    override fun trackEvent(event: CSEvent, expedited: Boolean) {
        scope.launch {
            processor.trackEvent(event)
            if (expedited) {
                service.executeOneTimeWork()
            }
        }
    }

    companion object {

        @Volatile
        @GuardedBy("lock")
        private var sInstance: DefaultClickStream? = null
        private val lock = Any()

        /**
         * Retrieves the singleton instance of [DefaultClickStream].
         *
         * @return The singleton instance of [ClickStream].
         *
         * @hide
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        fun getInstance(): ClickStream? {
            synchronized(lock) {
                if (sInstance != null) {
                    return sInstance
                }
                return sInstance
            }
        }

        /**
         * Initializes the singleton instance of [DefaultClickStream]. You should only do this one
         * time if you want to use [ClickStream] properly.
         * Call [DefaultClickStream.initialize] multiple time will throw [IllegalStateException].
         *
         * @param configuration The [CSConfiguration] for used to set up [ClickStream].
         *
         * @throws IllegalStateException when [DefaultClickStream.initialize] called multiple times.
         * @see release
         *
         * @hide
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        fun initialize(configuration: CSConfiguration) {
            synchronized(lock) {
                if (sInstance != null) {
                    throw IllegalStateException(
                        "ClickStream is already initialized. " +
                        "If you want to re-initialize ClickStream with new CSConfiguration, " +
                        "please call ClickStream#release first. " +
                        "See ClickStream#initialize(Context, CSConfiguration) or " +
                        "the class level. " +
                        "KotlinDoc for more information."
                    )
                }

                if (sInstance == null) {
                    val ctx = configuration.context
                    val serviceLocator = DefaultCServiceLocator(
                        context = ctx,
                        info = configuration.info,
                        config = configuration.config,
                        logLevel = configuration.logLevel,
                        healthEventLogger = configuration.healthLogger,
                        dispatcher = configuration.dispatcher,
                        eventGeneratedTimestampListener = configuration.eventGeneratedTimeStamp,
                        socketConnectionListener = configuration.socketConnectionListener,
                        remoteConfig = configuration.remoteConfig,
                        eventHealthListener = configuration.eventHealthListener
                    )

                    CSServiceLocator.setServiceLocator(serviceLocator)

                    sInstance = DefaultClickStream(
                        processor = serviceLocator.eventProcessor,
                        service = serviceLocator.workManager,
                        logger = serviceLocator.logger,
                        dispatcher = configuration.dispatcher
                    )
                }
            }
        }

        /**
         * Release [DefaultClickStream] instance.
         *
         * This function is necessary to be called before [initialize] [ClickStream] if
         * [CSConfiguration] changed.
         *
         * @hide
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        fun release() {
            synchronized(lock) {
                if (sInstance != null) {
                    sInstance = null
                }
            }
        }
    }
}
