package clickstream.internal.di.impl

import android.app.Application
import android.content.Context
import clickstream.api.CSInfo
import clickstream.config.CSConfig
import clickstream.config.CSEventSchedulerConfig
import clickstream.config.CSRemoteConfig
import clickstream.connection.CSSocketConnectionListener
import clickstream.health.identity.CSGuIdGenerator
import clickstream.health.identity.DefaultCSGuIdGenerator
import clickstream.health.intermediate.CSEventHealthListener
import clickstream.health.intermediate.CSHealthEventFactory
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.time.CSEventGeneratedTimestampListener
import clickstream.health.time.CSTimeStampGenerator
import clickstream.health.time.DefaultCSTimeStampGenerator
import clickstream.internal.db.CSDatabase
import clickstream.internal.di.CSServiceLocator
import clickstream.internal.eventprocessor.CSEventProcessor
import clickstream.internal.eventscheduler.CSBackgroundEventScheduler
import clickstream.internal.eventscheduler.CSEventRepository
import clickstream.internal.eventscheduler.CSForegroundEventScheduler
import clickstream.internal.eventscheduler.CSWorkManagerEventScheduler
import clickstream.internal.eventscheduler.impl.DefaultCSEventRepository
import clickstream.internal.networklayer.CSEventService
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.networklayer.CSNetworkRepository
import clickstream.internal.networklayer.CSNetworkRepositoryImpl
import clickstream.internal.networklayer.CSWorkManagerNetworkManager
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSFlowStreamAdapterFactory
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.internal.workmanager.CSWorkManager
import clickstream.lifecycle.CSAndroidLifecycle
import clickstream.lifecycle.CSAndroidLifecycle.APPLICATION_THROTTLE_TIMEOUT_MILLIS
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.lifecycle.CSBackgroundLifecycleManager
import clickstream.listener.CSEventListener
import clickstream.logger.CSLogLevel
import clickstream.logger.CSLogger
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.messageadapter.protobuf.ProtobufMessageAdapter
import com.tinder.scarlet.retry.ExponentialBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * The Default implementation of the Service Locator which will be used for injection for
 * ClickStream.
 */
@ExperimentalCoroutinesApi
internal class DefaultCServiceLocator(
    private val context: Context,
    private val info: CSInfo,
    private val config: CSConfig,
    private val eventGeneratedTimestampListener: CSEventGeneratedTimestampListener,
    private val socketConnectionListener: CSSocketConnectionListener,
    private val remoteConfig: CSRemoteConfig,
    override val logLevel: CSLogLevel,
    override val dispatcher: CoroutineDispatcher,
    override val eventHealthListener: CSEventHealthListener,
    override val healthEventRepository: CSHealthEventRepository,
    override val healthEventProcessor: CSHealthEventProcessor,
    override val healthEventFactory: CSHealthEventFactory,
    override val appLifeCycle: CSAppLifeCycle,
    override val eventListener: List<CSEventListener>
) : CSServiceLocator {

    private val db: CSDatabase by lazy {
        CSDatabase.getInstance(context)
    }

    private val guidGenerator: CSGuIdGenerator by lazy {
        DefaultCSGuIdGenerator()
    }

    private val timeStampGenerator: CSTimeStampGenerator by lazy {
        DefaultCSTimeStampGenerator(eventGeneratedTimestampListener)
    }

    private val batteryStatusObserver: CSBatteryStatusObserver by lazy {
        CSBatteryStatusObserver(context, config.networkConfig.minBatteryLevel)
    }

    private val networkStatusObserver: CSNetworkStatusObserver by lazy {
        CSNetworkStatusObserver(context)
    }

    private val eventRepository: CSEventRepository by lazy {
        DefaultCSEventRepository(eventDataDao = db.eventDataDao())
    }

    override val foregroundLifecycleRegistry: LifecycleRegistry by lazy {
        LifecycleRegistry(APPLICATION_THROTTLE_TIMEOUT_MILLIS)
    }

    private val foregroundLifecycleManager: Lifecycle by lazy {
        CSAndroidLifecycle.ofApplicationForeground(context.applicationContext as Application, logger, foregroundLifecycleRegistry)
    }

    override val backgroundLifecycleManager: CSBackgroundLifecycleManager by lazy {
        CSBackgroundLifecycleManager()
    }

    private val backgroundEventService: CSEventService by lazy {
        Scarlet.Builder().lifecycle(backgroundLifecycleManager).apply()
    }

    private val foregroundEventService: CSEventService by lazy {
        Scarlet.Builder().lifecycle(foregroundLifecycleManager).apply()
    }

    private val networkRepository: CSNetworkRepository by lazy {
        CSNetworkRepositoryImpl(
            networkConfig = config.networkConfig,
            eventService = foregroundEventService,
            dispatcher = dispatcher,
            timeStampGenerator = timeStampGenerator,
            logger = logger,
            healthEventRepository = healthEventRepository,
            info = info
        )
    }

    private val workManagerNetworkManager: CSWorkManagerNetworkManager by lazy {
        CSWorkManagerNetworkManager(
            appLifeCycle = appLifeCycle,
            networkRepository = CSNetworkRepositoryImpl(
                networkConfig = config.networkConfig,
                eventService = backgroundEventService,
                dispatcher = dispatcher,
                timeStampGenerator = timeStampGenerator,
                logger = logger,
                healthEventRepository = healthEventRepository,
                info = info
            ),
            dispatcher = dispatcher,
            logger = logger,
            healthEventRepository = healthEventRepository,
            info = info,
            connectionListener = socketConnectionListener
        )
    }

    override val logger: CSLogger by lazy { CSLogger(logLevel) }

    override val eventSchedulerConfig: CSEventSchedulerConfig by lazy { config.eventSchedulerConfig }

    override val foregroundNetworkManager: CSNetworkManager by lazy {
        CSNetworkManager(
            appLifeCycle = appLifeCycle,
            networkRepository = networkRepository,
            dispatcher = dispatcher,
            logger = logger,
            healthEventRepository = healthEventRepository,
            info = info,
            connectionListener = socketConnectionListener
        )
    }

    override val backgroundEventScheduler: CSBackgroundEventScheduler by lazy {
        CSBackgroundEventScheduler(
            appLifeCycle = appLifeCycle,
            dispatcher = dispatcher,
            guIdGenerator = guidGenerator,
            timeStampGenerator = timeStampGenerator,
            batteryStatusObserver = batteryStatusObserver,
            networkStatusObserver = networkStatusObserver,
            eventListeners = eventListener,
            healthEventProcessor = healthEventProcessor,
            info = info,
            eventRepository = eventRepository,
            healthEventRepository = healthEventRepository,
            logger = logger,
            networkManager = foregroundNetworkManager
        )
    }

    override val foregroundEventScheduler: CSForegroundEventScheduler by lazy {
        CSForegroundEventScheduler(
            appLifeCycle = appLifeCycle,
            networkManager = foregroundNetworkManager,
            config = config.eventSchedulerConfig,
            logger = logger,
            eventRepository = eventRepository,
            healthEventRepository = healthEventRepository,
            dispatcher = dispatcher,
            guIdGenerator = guidGenerator,
            timeStampGenerator = timeStampGenerator,
            batteryStatusObserver = batteryStatusObserver,
            networkStatusObserver = networkStatusObserver,
            info = info,
            eventHealthListener = eventHealthListener,
            eventListeners = eventListener
        ).also {
            // initialise backgroundEventScheduler
            backgroundEventScheduler
        }
    }

    override val workManagerEventScheduler: CSWorkManagerEventScheduler by lazy {
        CSWorkManagerEventScheduler(
            appLifeCycle = appLifeCycle,
            guIdGenerator = guidGenerator,
            timeStampGenerator = timeStampGenerator,
            batteryStatusObserver = batteryStatusObserver,
            networkStatusObserver = networkStatusObserver,
            eventListeners = eventListener,
            dispatcher = dispatcher,
            healthEventProcessor = healthEventProcessor,
            backgroundLifecycleManager = backgroundLifecycleManager,
            info = info,
            eventRepository = eventRepository,
            healthEventRepository = healthEventRepository,
            logger = logger,
            networkManager = workManagerNetworkManager
        )
    }

    override val eventProcessor: CSEventProcessor by lazy {
        CSEventProcessor(
            config = config.eventProcessorConfiguration,
            eventScheduler = foregroundEventScheduler,
            dispatcher = dispatcher,
            healthEventRepository = healthEventRepository,
            logger = logger,
            info = info
        )
    }

    override val workManager: CSWorkManager by lazy {
        CSWorkManager(
            appLifeCycle = appLifeCycle,
            context = context,
            eventSchedulerConfig = config.eventSchedulerConfig,
            logger = logger,
            remoteConfig = remoteConfig
        )
    }

    private inline fun <reified T> Scarlet.Builder.apply(): T {
        return with(config.networkConfig) {
            webSocketFactory(okHttpClient.newWebSocketFactory(endPoint))
                .addStreamAdapterFactory(CSFlowStreamAdapterFactory())
                .addMessageAdapterFactory(ProtobufMessageAdapter.Factory())
                .backoffStrategy(
                    ExponentialBackoffStrategy(
                        initialDurationMillis = initialRetryDurationInMs,
                        maxDurationMillis = maxConnectionRetryDurationInMs
                    )
                )
                .build()
                .create()
        }
    }
}
