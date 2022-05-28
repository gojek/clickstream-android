package clickstream.internal.networklayer

import clickstream.CSInfo
import clickstream.connection.CSSocketConnectionListener
import clickstream.internal.analytics.CSHealthEventRepository
import clickstream.internal.lifecycle.CSAppLifeCycle
import clickstream.logger.CSLogger
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
 * @param healthEventRepository - Used for logging health events
 */
@ExperimentalCoroutinesApi
internal class CSBackgroundNetworkManager(
    appLifeCycleObserver: CSAppLifeCycle,
    networkRepository: CSNetworkRepository,
    dispatcher: CoroutineDispatcher,
    logger: CSLogger,
    healthEventRepository: CSHealthEventRepository,
    info: CSInfo,
    connectionListener: CSSocketConnectionListener
) : CSNetworkManager(
    appLifeCycleObserver,
    networkRepository,
    dispatcher,
    logger,
    healthEventRepository,
    info,
    connectionListener
) {

    init {
        logger.debug { "CSBackgroundNetworkManager#init" }
    }

    override fun onStart() {
        logger.debug { "CSBackgroundNetworkManager#onStart" }
        coroutineScope.cancel()
        logger.debug { "CSBackgroundNetworkManager#onStart - coroutineScope cancelled" }
    }

    override fun onStop() {
        logger.debug { "CSBackgroundNetworkManager#onStop" }
        job = SupervisorJob()
        coroutineScope = CoroutineScope(job + dispatcher)
        observeSocketState()
        logger.debug { "CSBackgroundNetworkManager#onStart - coroutineScope started and observeSocketState" }
    }
}
