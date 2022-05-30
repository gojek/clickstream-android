package clickstream.health

import android.content.Context
import clickstream.CSMetaProvider
import clickstream.health.internal.CSHealthDatabase
import clickstream.health.internal.DefaultCSHealthEventFactory
import clickstream.health.internal.DefaultCSHealthEventProcessor
import clickstream.health.internal.DefaultCSHealthEventRepository
import clickstream.health.model.CSHealthEventConfig
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.logger.CSLogger
import clickstream.util.CSAppVersionSharedPref
import clickstream.util.impl.DefaultCSAppVersionSharedPref
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher

public object DefaultCSHealthGateway {

    public fun factory(
        context: Context,
        csInfo: CSInfo,
        appLifeCycle: CSAppLifeCycle,
        dispatcher: CoroutineDispatcher,
        healthEventConfig: CSHealthEventConfig,
        info: CSInfo,
        logger: CSLogger,
        healthEventLogger: CSHealthEventLogger,
        appVersion: String,
        appVersionPreference: CSAppVersionSharedPref = DefaultCSAppVersionSharedPref(context),
        guIdGenerator: CSGuIdGenerator,
        timeStampGenerator: CSTimeStampGenerator,
        metaProvider: CSMetaProvider,
        eventHealthListener: CSEventHealthListener
    ): CSHealthGateway {

        return object : CSHealthGateway {
            override val appLifeCycle: CSAppLifeCycle = appLifeCycle
            override val eventHealthListener: CSEventHealthListener = eventHealthListener
            override val healthEventRepository: CSHealthEventRepository by lazy {
                DefaultCSHealthEventRepository(
                    sessionId = UUID.randomUUID().toString(),
                    healthEventDao = CSHealthDatabase.getInstance(context).healthEventDao(),
                    info = csInfo
                )
            }
            override val healthEventProcessor: CSHealthEventProcessor by lazy {
                DefaultCSHealthEventProcessor(
                    appLifeCycleObserver = appLifeCycle,
                    healthEventRepository = healthEventRepository,
                    dispatcher = dispatcher,
                    healthEventConfig = healthEventConfig,
                    info = info,
                    logger = logger,
                    healthEventLogger = healthEventLogger,
                    healthEventFactory = healthEventFactory,
                    appVersion = appVersion,
                    appVersionPreference = appVersionPreference
                )
            }
            override val healthEventFactory: CSHealthEventFactory by lazy {
                DefaultCSHealthEventFactory(
                    guIdGenerator = guIdGenerator,
                    timeStampGenerator = timeStampGenerator,
                    metaProvider = metaProvider
                )
            }
        }
    }
}
