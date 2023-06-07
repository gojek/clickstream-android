import android.content.Context
import clickstream.api.CSInfo
import clickstream.health.CSHealthGateway
import clickstream.health.intermediate.CSHealthEventLoggerListener
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.health.intermediate.CSMemoryStatusProvider
import clickstream.health.internal.DefaultCSGuIdGenerator
import clickstream.health.internal.database.CSHealthDatabase
import clickstream.health.internal.database.CSHealthEventDao
import clickstream.health.internal.factory.CSHealthEventFactory
import clickstream.health.internal.processor.CSHealthEventProcessorImpl
import clickstream.health.internal.repository.CSHealthEventRepository
import clickstream.health.internal.factory.DefaultCSHealthEventFactory
import clickstream.health.internal.repository.DefaultCSHealthEventRepository
import clickstream.health.model.CSHealthEventConfig
import clickstream.health.time.CSHealthTimeStampGenerator
import clickstream.logger.CSLogger
import clickstream.util.CSAppVersionSharedPref
import clickstream.util.impl.DefaultCSAppVersionSharedPref

public class DefaultCSHealthGateway(
    private val context: Context,
    private val csMemoryStatusProvider: CSMemoryStatusProvider,
    private val csHealthEventConfig: CSHealthEventConfig,
    private val csInfo: CSInfo,
    private val logger: CSLogger,
    private val timeStampGenerator: CSHealthTimeStampGenerator,
    private val csHealthEventLoggerListener: CSHealthEventLoggerListener,
) : CSHealthGateway {

    override val healthEventProcessor: CSHealthEventProcessor? by lazy {
        if (isHealthEventEnabled()) {
            CSHealthEventProcessorImpl(
                healthEventRepository = healthRepository,
                healthEventConfig = csHealthEventConfig,
                info = csInfo,
                logger = logger,
                healthEventFactory = csHealthEventFactory,
                memoryStatusProvider = csMemoryStatusProvider,
                csHealthEventLogger = csHealthEventLoggerListener
            )
        } else {
            null
        }
    }

    override suspend fun clearHealthEventsForVersionChange() {
        CSHealthEventProcessorImpl.clearHealthEventsForVersionChange(
            csAppVersionSharedPref,
            csInfo.appInfo.appVersion,
            healthRepository,
            logger
        )
    }

    private fun isHealthEventEnabled(): Boolean {
        return CSHealthEventProcessorImpl.isHealthEventEnabled(
            csMemoryStatusProvider,
            csHealthEventConfig,
            csInfo
        )
    }

    private val csAppVersionSharedPref: CSAppVersionSharedPref by lazy {
        DefaultCSAppVersionSharedPref(context)
    }

    private val healthRepository: CSHealthEventRepository by lazy {
        DefaultCSHealthEventRepository(csHealthEventDao, csInfo)
    }

    private val csHealthEventDao: CSHealthEventDao by lazy {
        CSHealthDatabase.getInstance(context).healthEventDao()
    }

    private val csHealthEventFactory: CSHealthEventFactory by lazy {
        DefaultCSHealthEventFactory(DefaultCSGuIdGenerator(), timeStampGenerator, csInfo)
    }
}