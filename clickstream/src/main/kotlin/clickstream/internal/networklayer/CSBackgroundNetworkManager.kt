package clickstream.internal.networklayer

import clickstream.api.CSInfo
import clickstream.connection.CSSocketConnectionListener
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.listener.CSEventListener
import clickstream.logger.CSLogger
import clickstream.report.CSReportDataTracker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * The NetworkManager is responsible for communicating with repository and the event scheduler
 * when the app is in Background
 *
 * @param networkRepository - Handles the communication with the server
 * @param dispatcher - CoroutineDispatcher on which the events are observed and processed
 * @param logger - To create logs
 * @param healthEventProcessor - Used for logging health events
 */
@ExperimentalCoroutinesApi
internal class CSBackgroundNetworkManager(
    appLifeCycleObserver: CSAppLifeCycle,
    networkRepository: CSNetworkRepository,
    dispatcher: CoroutineDispatcher,
    logger: CSLogger,
    healthEventProcessor: CSHealthEventProcessor?,
    info: CSInfo,
    connectionListener: CSSocketConnectionListener,
    csReportDataTracker: CSReportDataTracker?,
    eventListeners: List<CSEventListener>
) : CSNetworkManager(
    appLifeCycleObserver,
    networkRepository,
    dispatcher,
    logger,
    healthEventProcessor,
    info,
    connectionListener,
    csReportDataTracker,
    eventListeners
) {

    override val tag: String
        get() = "CSBackgroundNetworkManager"

    override fun onStart() {
        logger.debug { "$tag#onStart" }
        coroutineScope.cancel()
        logger.debug { "$tag#onStart - coroutineScope cancelled" }
    }

    override fun onStop() {
        logger.debug { "$tag#onStop" }
        job = SupervisorJob()
        coroutineScope = CoroutineScope(job + dispatcher)
        observeSocketState()
        logger.debug { "$tag#onStart - coroutineScope started and observeSocketState" }
    }
}
