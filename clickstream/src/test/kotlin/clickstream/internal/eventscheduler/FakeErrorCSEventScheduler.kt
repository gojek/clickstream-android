package clickstream.internal.eventscheduler

import clickstream.config.CSEventSchedulerConfig
import clickstream.api.CSInfo
import clickstream.config.CSRemoteConfig
import clickstream.health.intermediate.CSEventHealthListener
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.internal.lifecycle.CSAppLifeCycle
import clickstream.internal.lifecycle.CSSocketConnectionManager
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSGuIdGenerator
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.internal.utils.CSTimeStampGenerator
import clickstream.listener.CSEventListener
import clickstream.logger.CSLogger
import clickstream.report.CSReportDataTracker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
internal open class FakeErrorCSEventScheduler(
    appLifeCycleObserver: CSAppLifeCycle,
    networkManager: CSNetworkManager,
    dispatcher: CoroutineDispatcher,
    config: CSEventSchedulerConfig,
    eventRepository: CSEventRepository,
    healthEventRepository: CSHealthEventRepository,
    logger: CSLogger,
    guIdGenerator: CSGuIdGenerator,
    timeStampGenerator: CSTimeStampGenerator,
    batteryStatusObserver: CSBatteryStatusObserver,
    networkStatusObserver: CSNetworkStatusObserver,
    info: CSInfo,
    eventHealthListener: CSEventHealthListener,
    eventListeners: List<CSEventListener>,
    errorListener: CSEventSchedulerErrorListener,
    csReportDataTracker: CSReportDataTracker? = null,
    batchSizeRegulator: CSEventBatchSizeStrategy,
    socketConnectionManager: CSSocketConnectionManager,
    remoteConfig: CSRemoteConfig
) : CSEventScheduler(
    appLifeCycleObserver,
    networkManager,
    dispatcher,
    config,
    eventRepository,
    healthEventRepository,
    logger,
    guIdGenerator,
    timeStampGenerator,
    batteryStatusObserver,
    networkStatusObserver,
    info,
    eventHealthListener,
    eventListeners,
    errorListener,
    csReportDataTracker,
    batchSizeRegulator,
    socketConnectionManager,
    remoteConfig

) {

    override val tag: String
        get() = "FakeErrorCSEventScheduler"

    fun emitError(errorMessage: String) {
        coroutineScope.launch(handler) {
            throw IllegalStateException(errorMessage)
        }
    }
}
