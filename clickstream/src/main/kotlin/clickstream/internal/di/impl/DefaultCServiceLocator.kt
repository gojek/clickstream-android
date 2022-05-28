package clickstream.internal.di.impl

import android.app.Application
import android.content.Context
import clickstream.CSInfo
import clickstream.config.CSConfig
import clickstream.config.CSEventSchedulerConfig
import clickstream.config.CSRemoteConfig
import clickstream.config.timestamp.CSEventGeneratedTimestampListener
import clickstream.connection.CSSocketConnectionListener
import clickstream.internal.analytics.CSHealthEventLogger
import clickstream.internal.analytics.CSHealthEventProcessor
import clickstream.internal.analytics.CSHealthEventRepository
import clickstream.analytics.event.CSEventHealthListener
import clickstream.internal.analytics.impl.DefaultCSHealthEventRepository
import clickstream.internal.db.CSAppVersionSharedPref
import clickstream.internal.db.CSDatabase
import clickstream.internal.db.impl.DefaultCSAppVersionSharedPref
import clickstream.internal.di.CSServiceLocator
import clickstream.internal.eventprocessor.CSEventProcessor
import clickstream.internal.eventprocessor.CSHealthEventFactory
import clickstream.internal.eventprocessor.CSMetaProvider
import clickstream.internal.eventprocessor.impl.DefaultCSHealthEventFactory
import clickstream.internal.eventprocessor.impl.DefaultCSMetaProvider
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
    healthEventLogger: CSHealthEventLogger,
    override val dispatcher: CoroutineDispatcher,
    private val eventGeneratedTimestampListener: CSEventGeneratedTimestampListener,
    private val socketConnectionListener: CSSocketConnectionListener,
    private val remoteConfig: CSRemoteConfig,
    override val eventHealthListener: CSEventHealthListener
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

    /**
     * The Db will which will store the events sent to the sdk
     */
    private val db: CSDatabase by lazy {
        CSDatabase.getInstance(context)
    }

    /**
     * The preference which will store the app version
     */
    private val appVersionPreference: CSAppVersionSharedPref by lazy {
        DefaultCSAppVersionSharedPref(context)
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

    private val metaProvider: CSMetaProvider by lazy {
        DefaultCSMetaProvider(info = info)
    }

    private val healthEventFactory: CSHealthEventFactory by lazy {
        DefaultCSHealthEventFactory(
            guIdGenerator = guidGenerator,
            timeStampGenerator = timeStampGenerator,
            metaProvider = metaProvider,
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

    override val healthEventRepository: CSHealthEventRepository by lazy {
        DefaultCSHealthEventRepository(
            sessionId = info.sessionInfo.sessionID,
            healthEventDao = db.healthEventDao(),
            info = info,
            remoteConfig = remoteConfig,
            logger = logger
        )
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

    override val healthEventProcessor: CSHealthEventProcessor by lazy {
        CSHealthEventProcessor(
            appLifeCycleObserver = appLifeCycleObserver,
            healthEventFactory = healthEventFactory,
            healthEventRepository = healthEventRepository,
            dispatcher = dispatcher,
            healthEventConfig = config.healthEventConfig,
            info = info,
            logger = logger,
            healthEventLogger = healthEventLogger,
            appVersion = info.appInfo.appVersion,
            appVersionPreference = appVersionPreference,
            remoteConfig = remoteConfig
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