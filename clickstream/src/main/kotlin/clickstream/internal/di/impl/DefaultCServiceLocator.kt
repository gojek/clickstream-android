package clickstream.internal.di.impl

import android.app.Application
import android.content.Context
import clickstream.config.CSConfig
import clickstream.config.CSEventSchedulerConfig
import clickstream.config.CSRemoteConfig
import clickstream.config.timestamp.CSEventGeneratedTimestampListener
import clickstream.connection.CSSocketConnectionListener
import clickstream.health.CSEventHealthListener
import clickstream.health.CSHealthEventFactory
import clickstream.health.CSHealthEventRepository
import clickstream.health.CSInfo
import clickstream.health.DefaultCSHealthEventProcessor
import clickstream.internal.db.CSDatabase
import clickstream.internal.di.CSServiceLocator
import clickstream.internal.eventprocessor.CSEventProcessor
import clickstream.internal.eventscheduler.CSBackgroundScheduler
import clickstream.internal.eventscheduler.CSEventRepository
import clickstream.internal.eventscheduler.CSEventScheduler
import clickstream.internal.eventscheduler.impl.DefaultCSEventRepository
import clickstream.internal.lifecycle.CSAppLifeCycle
import clickstream.internal.lifecycle.CSBackgroundLifecycleManager
import clickstream.internal.lifecycle.impl.DefaultCSAppLifeCycleObserver
import clickstream.internal.networklayer.CSBackgroundNetworkManager
import clickstream.internal.networklayer.CSEventService
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.networklayer.CSNetworkRepository
import clickstream.internal.networklayer.CSNetworkRepositoryImpl
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSFlowStreamAdapterFactory
import clickstream.internal.utils.CSGuIdGenerator
import clickstream.internal.utils.CSGuIdGeneratorImpl
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.internal.utils.CSTimeStampGenerator
import clickstream.internal.utils.DefaultCSTimeStampGenerator
import clickstream.internal.workmanager.CSWorkManager
import clickstream.logger.CSLogLevel
import clickstream.logger.CSLogger
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
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
    override val logLevel: CSLogLevel,
    override val dispatcher: CoroutineDispatcher,
    private val eventGeneratedTimestampListener: CSEventGeneratedTimestampListener,
    private val socketConnectionListener: CSSocketConnectionListener,
    private val remoteConfig: CSRemoteConfig,
    override val eventHealthListener: CSEventHealthListener,
    override val healthEventRepository: CSHealthEventRepository,
    override val healthEventProcessor: DefaultCSHealthEventProcessor,
    override val healthEventFactory: CSHealthEventFactory
) : CSServiceLocator {

    private val guidGenerator: CSGuIdGenerator by lazy {
        CSGuIdGeneratorImpl()
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

    private val db: CSDatabase by lazy {
        CSDatabase.getInstance(context)
    }

    private val lifecycle: Lifecycle by lazy {
        AndroidLifecycle.ofApplicationForeground(context.applicationContext as Application)
    }

    private val backgroundLifecycleManager: CSBackgroundLifecycleManager by lazy {
        CSBackgroundLifecycleManager()
    }

    private val eventService: CSEventService by lazy {
        Scarlet.Builder().lifecycle(lifecycle).apply()
    }

    private val backgroundEventService: CSEventService by lazy {
        Scarlet.Builder().lifecycle(backgroundLifecycleManager).apply()
    }

    private val networkRepository: CSNetworkRepository by lazy {
        CSNetworkRepositoryImpl(
            networkConfig = config.networkConfig,
            eventService = eventService,
            dispatcher = dispatcher,
            timeStampGenerator = timeStampGenerator,
            logger = logger,
            healthEventRepository = healthEventRepository,
            info = info
        )
    }

    private val eventRepository: CSEventRepository by lazy {
        DefaultCSEventRepository(
            eventDataDao = db.eventDataDao()
        )
    }

    private val backgroundNetworkManager: CSBackgroundNetworkManager by lazy {
        CSBackgroundNetworkManager(
            appLifeCycleObserver = appLifeCycleObserver,
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

    private val appLifeCycleObserver: CSAppLifeCycle by lazy {
        DefaultCSAppLifeCycleObserver(context)
    }

    override val logger: CSLogger by lazy {
        CSLogger(logLevel)
    }

    override val networkManager: CSNetworkManager by lazy {
        CSNetworkManager(
            appLifeCycleObserver = appLifeCycleObserver,
            networkRepository = networkRepository,
            dispatcher = dispatcher,
            logger = logger,
            healthEventRepository = healthEventRepository,
            info = info,
            connectionListener = socketConnectionListener
        )
    }

    override val eventScheduler: CSEventScheduler by lazy {
        CSEventScheduler(
            appLifeCycleObserver = appLifeCycleObserver,
            networkManager = networkManager,
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
            eventHealthListener = eventHealthListener
        )
    }

    override val eventProcessor: CSEventProcessor by lazy {
        CSEventProcessor(
            config = config.eventProcessorConfiguration,
            eventScheduler = eventScheduler,
            dispatcher = dispatcher,
            healthEventRepository = healthEventRepository,
            logger = logger,
            info = info
        )
    }

    override val workManager: CSWorkManager by lazy {
        CSWorkManager(
            appLifeCycleObserver = appLifeCycleObserver,
            context = context,
            eventSchedulerConfig = config.eventSchedulerConfig,
            logger = logger,
            backgroundLifecycleManager = backgroundLifecycleManager,
            remoteConfig = remoteConfig
        )
    }

    override val backgroundScheduler: CSBackgroundScheduler by lazy {
        CSBackgroundScheduler(
            appLifeCycleObserver = appLifeCycleObserver,
            networkManager = backgroundNetworkManager,
            config = config.eventSchedulerConfig,
            logger = logger,
            eventRepository = eventRepository,
            healthEventRepository = healthEventRepository,
            dispatcher = dispatcher,
            guIdGenerator = guidGenerator,
            timeStampGenerator = timeStampGenerator,
            backgroundLifecycleManager = backgroundLifecycleManager,
            batteryStatusObserver = batteryStatusObserver,
            networkStatusObserver = networkStatusObserver,
            info = info,
            eventHealthListener = eventHealthListener
        )
    }

    override val eventSchedulerConfig: CSEventSchedulerConfig by lazy {
        config.eventSchedulerConfig
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