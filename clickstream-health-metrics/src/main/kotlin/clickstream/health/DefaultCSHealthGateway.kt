package clickstream.health

import android.content.Context
import clickstream.api.CSInfo
import clickstream.health.identity.CSGuIdGenerator
import clickstream.health.identity.DefaultCSGuIdGenerator
import clickstream.health.intermediate.CSEventHealthListener
import clickstream.health.intermediate.CSHealthEventFactory
import clickstream.health.intermediate.CSHealthEventLoggerListener
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.internal.CSHealthDatabase
import clickstream.health.internal.DefaultCSHealthEventFactory
import clickstream.health.internal.DefaultCSHealthEventProcessor
import clickstream.health.internal.DefaultCSHealthEventRepository
import clickstream.health.model.CSHealthEventConfig
import clickstream.health.time.CSHealthTimeStampGenerator
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.logger.CSLogger
import clickstream.util.CSAppVersionSharedPref
import clickstream.util.impl.DefaultCSAppVersionSharedPref
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

public object DefaultCSHealthGateway {

    public fun factory(
        appVersion: String,
        sessionId: String,
        context: Context,
        healthEventConfig: CSHealthEventConfig,
        csInfo: CSInfo,
        logger: CSLogger,
        healthEventLogger: CSHealthEventLoggerListener,
        timeStampGenerator: CSHealthTimeStampGenerator,
        eventHealthListener: CSEventHealthListener,
        appLifeCycle: CSAppLifeCycle,
        guIdGenerator: CSGuIdGenerator = DefaultCSGuIdGenerator(),
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        appVersionPreference: CSAppVersionSharedPref = DefaultCSAppVersionSharedPref(context)
    ): CSHealthGateway {

        return object : CSHealthGateway {
            override val eventHealthListener: CSEventHealthListener = eventHealthListener
            override val healthEventRepository: CSHealthEventRepository by lazy {
                DefaultCSHealthEventRepository(
                    sessionId = sessionId,
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
                    info = csInfo,
                    logger = logger,
                    healthEventLoggerListener = healthEventLogger,
                    healthEventFactory = healthEventFactory,
                    appVersion = appVersion,
                    appVersionPreference = appVersionPreference
                )
            }
            override val healthEventFactory: CSHealthEventFactory by lazy {
                DefaultCSHealthEventFactory(
                    guIdGenerator = guIdGenerator,
                    timeStampGenerator = timeStampGenerator,
                    csInfo = csInfo
                )
            }
        }
    }
}
