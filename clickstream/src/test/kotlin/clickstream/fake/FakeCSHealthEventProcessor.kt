package clickstream.fake

import clickstream.api.CSAppInfo
import clickstream.health.identity.DefaultCSGuIdGenerator
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.health.internal.DefaultCSHealthEventFactory
import clickstream.health.internal.DefaultCSHealthEventProcessor
import clickstream.health.time.CSTimeStampGenerator
import clickstream.logger.CSLogLevel
import clickstream.logger.CSLogger
import kotlinx.coroutines.CoroutineDispatcher

internal val fakeCSMetaProvider = FakeCSMetaProvider()
internal val fakeCSHealthEventDTOs = fakeCSHealthEventDTOs(fakeCSMetaProvider)
internal val fakeCSInfo = fakeCSInfo(fakeCSMetaProvider)
internal val fakeCSAppLifeCycle = FakeCSAppLifeCycle()
internal val fakeCSHealthEventRepository = FakeCSHealthEventRepository(fakeCSHealthEventDTOs)
internal val fakeCSHealthEventLoggerListener = FakeCSHealthEventLoggerListener()
internal val fakeCSHealthEventFactory = DefaultCSHealthEventFactory(
    guIdGenerator = DefaultCSGuIdGenerator(),
    timeStampGenerator = object : CSTimeStampGenerator {
        override fun getTimeStamp(): Long {
            return 1
        }
    },
    metaProvider = fakeCSMetaProvider
)
internal val fakeCSAppVersionSharedPref = FakeCSAppVersionSharedPref(true)

internal fun FakeCSHealthEventProcessor(
    dispatcher: CoroutineDispatcher
): CSHealthEventProcessor {
    return DefaultCSHealthEventProcessor(
        appLifeCycleObserver = fakeCSAppLifeCycle,
        healthEventRepository = fakeCSHealthEventRepository,
        dispatcher = dispatcher,
        healthEventConfig = fakeCSHealthEventConfig,
        info = fakeCSInfo.copy(appInfo = CSAppInfo(fakeCSMetaProvider.app.version)),
        logger = CSLogger(CSLogLevel.OFF),
        healthEventLoggerListener = fakeCSHealthEventLoggerListener,
        healthEventFactory = fakeCSHealthEventFactory,
        appVersion = fakeCSMetaProvider.app.version,
        appVersionPreference = fakeCSAppVersionSharedPref
    )
}