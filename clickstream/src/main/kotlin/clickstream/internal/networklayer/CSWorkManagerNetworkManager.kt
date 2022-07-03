package clickstream.internal.networklayer

import android.annotation.SuppressLint
import clickstream.api.CSInfo
import clickstream.connection.CSSocketConnectionListener
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.logger.CSLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

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
internal class CSWorkManagerNetworkManager(
    appLifeCycle: CSAppLifeCycle,
    networkRepository: CSNetworkRepository,
    private val dispatcher: CoroutineDispatcher,
    private val logger: CSLogger,
    healthEventRepository: CSHealthEventRepository,
    info: CSInfo,
    connectionListener: CSSocketConnectionListener
) : CSNetworkManager(
    appLifeCycle,
    networkRepository,
    dispatcher,
    logger,
    healthEventRepository,
    info,
    connectionListener
) {

    private val coroutineExceptionHandler: CoroutineExceptionHandler by lazy {
        CoroutineExceptionHandler { _, throwable ->
            logger.error {
                "================== CRASH IS HAPPENING ================== \n" +
                "= In : CSBackgroundEventScheduler                      = \n" +
                "= Due : ${throwable.message}                           = \n" +
                "==================== END OF CRASH ====================== \n"
            }
        }
    }

    init {
        logger.debug { "CSWorkManagerNetworkManager#init" }
    }

    override fun onStart() {
        logger.debug { "CSWorkManagerNetworkManager#onStart" }
        logger.debug { "CSWorkManagerNetworkManager#onStart - coroutineScope cancelled" }

        cancelJob()
    }

    @SuppressLint("VisibleForTests")
    override fun onStop() {
        logger.debug { "CSWorkManagerNetworkManager#onStop" }
        logger.debug { "CSWorkManagerNetworkManager#onStart - coroutineScope started and observeSocketState" }

        coroutineScope.launch(coroutineExceptionHandler) {
            observeSocketConnectionState()
        }
    }
}
