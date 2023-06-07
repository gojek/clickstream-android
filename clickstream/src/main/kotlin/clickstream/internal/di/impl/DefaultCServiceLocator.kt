package clickstream.internal.di.impl

import android.app.Application
import android.content.Context
import clickstream.api.CSInfo
import clickstream.config.CSConfig
import clickstream.config.CSEventSchedulerConfig
import clickstream.config.CSRemoteConfig
import clickstream.config.timestamp.CSEventGeneratedTimestampListener
import clickstream.connection.CSSocketConnectionListener
import clickstream.health.CSHealthGateway
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.internal.db.CSBatchSizeSharedPref
import clickstream.internal.db.CSDatabase
import clickstream.internal.db.impl.DefaultCSBatchSizeSharedPref
import clickstream.internal.di.CSServiceLocator
import clickstream.internal.eventprocessor.CSEventProcessor
import clickstream.internal.eventscheduler.CSBackgroundScheduler
import clickstream.internal.eventscheduler.CSEventSchedulerErrorListener
import clickstream.internal.eventscheduler.CSEventBatchSizeStrategy
import clickstream.internal.eventscheduler.CSEventRepository
import clickstream.internal.eventscheduler.CSEventScheduler
import clickstream.internal.eventscheduler.impl.EventByteSizeBasedBatchStrategy
import clickstream.internal.eventscheduler.impl.DefaultCSEventRepository
import clickstream.internal.networklayer.CSBackgroundNetworkManager
import clickstream.internal.networklayer.CSEventService
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.networklayer.CSNetworkRepository
import clickstream.internal.networklayer.CSNetworkRepositoryImpl
import clickstream.internal.networklayer.socket.CSSocketConnectionManager
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSFlowStreamAdapterFactory
import clickstream.internal.utils.CSGuIdGenerator
import clickstream.internal.utils.CSGuIdGeneratorImpl
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.internal.utils.CSTimeStampGenerator
import clickstream.internal.utils.DefaultCSTimeStampGenerator
import clickstream.internal.workmanager.CSWorkManager
import clickstream.lifecycle.impl.DefaultCSAppLifeCycleObserver
import clickstream.listener.CSEventListener
import clickstream.logger.CSLogLevel
import clickstream.logger.CSLogger
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.messageadapter.protobuf.ProtobufMessageAdapter
import com.tinder.scarlet.retry.ExponentialBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.OkHttpClient
import clickstream.report.CSReportDataTracker
import com.tinder.scarlet.lifecycle.LifecycleRegistry

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
    private val healthGateway: CSHealthGateway,
    private val eventListeners: List<CSEventListener>,
    private val eventSchedulerErrorListener: CSEventSchedulerErrorListener,
    private val csReportDataTracker: CSReportDataTracker?
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

    private val socketConnectionManager: CSSocketConnectionManager by lazy {
        CSSocketConnectionManager(LifecycleRegistry(), context.applicationContext as Application)
    }

    private val eventService: CSEventService by lazy {
        Scarlet.Builder().lifecycle(socketConnectionManager).apply()
    }

    private val networkRepository: CSNetworkRepository by lazy {
        CSNetworkRepositoryImpl(
            networkConfig = config.networkConfig,
            eventService = eventService,
            dispatcher = dispatcher,
            timeStampGenerator = timeStampGenerator,
            logger = logger,
            healthProcessor = healthEventProcessor,
            info = info
        )
    }

    private val eventRepository: CSEventRepository by lazy {
        DefaultCSEventRepository(
            eventDataDao = db.eventDataDao()
        )
    }

    private val appLifeCycleObserver: clickstream.lifecycle.CSAppLifeCycle by lazy {
        DefaultCSAppLifeCycleObserver(logger)
    }

    override val logger: CSLogger by lazy {
        CSLogger(logLevel)
    }

    override val healthEventProcessor: CSHealthEventProcessor? = healthGateway.healthEventProcessor

    private val backgroundNetworkManager: CSNetworkManager = CSBackgroundNetworkManager(
        appLifeCycleObserver = appLifeCycleObserver,
        networkRepository = CSNetworkRepositoryImpl(
            networkConfig = config.networkConfig,
            eventService = eventService,
            dispatcher = dispatcher,
            timeStampGenerator = timeStampGenerator,
            logger = logger,
            healthProcessor = healthEventProcessor,
            info = info
        ),
        dispatcher = dispatcher,
        logger = logger,
        healthEventProcessor = healthEventProcessor,
        info = info,
        connectionListener = socketConnectionListener,
        csReportDataTracker = csReportDataTracker,
        eventListeners = eventListeners
    )

    override val networkManager: CSNetworkManager by lazy {
        CSNetworkManager(
            appLifeCycleObserver = appLifeCycleObserver,
            networkRepository = networkRepository,
            dispatcher = dispatcher,
            logger = logger,
            healthEventProcessor = healthEventProcessor,
            info = info,
            connectionListener = socketConnectionListener,
            csReportDataTracker = csReportDataTracker,
            csEventListeners = eventListeners
        )
    }

    override val eventScheduler: CSEventScheduler by lazy {
        CSEventScheduler(
            appLifeCycleObserver = appLifeCycleObserver,
            networkManager = networkManager,
            config = config.eventSchedulerConfig,
            logger = logger,
            eventRepository = eventRepository,
            healthEventProcessor = healthEventProcessor,
            dispatcher = dispatcher,
            guIdGenerator = guidGenerator,
            timeStampGenerator = timeStampGenerator,
            batteryStatusObserver = batteryStatusObserver,
            networkStatusObserver = networkStatusObserver,
            info = info,
            eventListeners = eventListeners,
            errorListener = eventSchedulerErrorListener,
            csReportDataTracker = csReportDataTracker,
            batchSizeRegulator = batchSizeStrategy,
            socketConnectionManager = socketConnectionManager,
            remoteConfig = remoteConfig,
            csHealthGateway = healthGateway
        )
    }

    override val eventProcessor: CSEventProcessor by lazy {
        CSEventProcessor(
            config = config.eventProcessorConfiguration,
            eventScheduler = eventScheduler,
            dispatcher = dispatcher,
            logger = logger,
        )
    }

    private val batchSizeStrategy: CSEventBatchSizeStrategy by lazy {
        EventByteSizeBasedBatchStrategy(logger, batchSizeSharedPref)
    }

    private val batchSizeSharedPref: CSBatchSizeSharedPref by lazy {
        DefaultCSBatchSizeSharedPref(context)
    }

    override val workManager: CSWorkManager by lazy {
        CSWorkManager(
            appLifeCycleObserver = appLifeCycleObserver,
            context = context,
            eventSchedulerConfig = config.eventSchedulerConfig,
            logger = logger,
            remoteConfig = remoteConfig,
        )
    }

    override val backgroundScheduler: CSBackgroundScheduler = CSBackgroundScheduler(
        appLifeCycleObserver = appLifeCycleObserver,
        networkManager = backgroundNetworkManager,
        config = config.eventSchedulerConfig,
        logger = logger,
        eventRepository = eventRepository,
        healthProcessor = healthEventProcessor,
        dispatcher = dispatcher,
        guIdGenerator = guidGenerator,
        timeStampGenerator = timeStampGenerator,
        csSocketConnectionManager = socketConnectionManager,
        batteryStatusObserver = batteryStatusObserver,
        networkStatusObserver = networkStatusObserver,
        info = info,
        eventListeners = eventListeners,
        errorListener = eventSchedulerErrorListener,
        csReportDataTracker = csReportDataTracker,
        batchSizeRegulator = batchSizeStrategy,
        remoteConfig = remoteConfig,
        batchSizeSharedPref = batchSizeSharedPref,
        csHealthGateway = healthGateway
    )

    override val eventSchedulerConfig: CSEventSchedulerConfig by lazy {
        config.eventSchedulerConfig
    }

    private inline fun <reified T> Scarlet.Builder.apply(): T {
        val okHttpClient = if (config.networkConfig.okHttpClient != null) {
            logger.debug { "DefaultCSServiceLocator#connectionUnauth - false" }
            config.networkConfig.okHttpClient
        } else {
            logger.debug { "DefaultCSServiceLocator#connectionUnauth - true" }
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request()
                    val newRequest = request.newBuilder().apply {
                        config.networkConfig.headers.forEach {
                            header(it.key, it.value)
                        }
                    }.build()

                    newRequest.headers().names().forEach { name ->
                        val v = newRequest.header(name)
                        logger.debug { "Header -> $name : $v" }
                    }
                    chain.proceed(newRequest)
                }
                .writeTimeout(config.networkConfig.writeTimeout, TimeUnit.SECONDS)
                .readTimeout(config.networkConfig.readTimeout, TimeUnit.SECONDS)
                .connectTimeout(config.networkConfig.connectTimeout, TimeUnit.SECONDS)
                .pingInterval(config.networkConfig.pingInterval, TimeUnit.SECONDS)
                .build()
        }

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