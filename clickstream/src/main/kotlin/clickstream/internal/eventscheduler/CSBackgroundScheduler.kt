@file:Suppress("LongParameterList")

package clickstream.internal.eventscheduler

import clickstream.config.CSEventSchedulerConfig
import clickstream.internal.lifecycle.CSAppLifeCycle
import clickstream.internal.lifecycle.CSBackgroundLifecycleManager
import clickstream.internal.networklayer.CSBackgroundNetworkManager
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSGuIdGenerator
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.internal.utils.CSTimeStampGenerator
import clickstream.logger.CSLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay

private const val TIMEOUT: Int = 5000
private const val ONE_SEC: Long = 1000

/**
 * The BackgroundEventScheduler acts an bridge between the BackgroundWorker & NetworkManager
 *
 * It flushes all the events present in database
 */
@ExperimentalCoroutinesApi
internal class CSBackgroundScheduler(
    appLifeCycleObserver: CSAppLifeCycle,
    networkManager: CSBackgroundNetworkManager,
    dispatcher: CoroutineDispatcher,
    config: CSEventSchedulerConfig,
    eventRepository: CSEventRepository,
    logger: CSLogger,
    guIdGenerator: CSGuIdGenerator,
    timeStampGenerator: CSTimeStampGenerator,
    batteryStatusObserver: CSBatteryStatusObserver,
    networkStatusObserver: CSNetworkStatusObserver,
    private val backgroundLifecycleManager: CSBackgroundLifecycleManager
) : CSEventScheduler(
    appLifeCycleObserver,
    networkManager,
    dispatcher,
    config,
    eventRepository,
    logger,
    guIdGenerator,
    timeStampGenerator,
    batteryStatusObserver,
    networkStatusObserver
) {

    override fun onStart() {
        logger.debug { "CSBackgroundScheduler#onStart" }
    }

    override fun onStop() {
        logger.debug { "CSBackgroundScheduler#onStop - backgroundTaskEnabled ${config.backgroundTaskEnabled}" }
        if (config.backgroundTaskEnabled) {
            job = SupervisorJob()
            coroutineScope = CoroutineScope(job + dispatcher)
            backgroundLifecycleManager.onStart()
            setupObservers()
        }
    }

    /**
     * Flushes all the events present in database and
     * waits for ack
     */
    suspend fun sendEvents(): Boolean {
        logger.debug { "CSBackgroundScheduler#sendEvents" }

        backgroundLifecycleManager.onStart()
        waitForNetwork()
        flushAllEvents()
        waitForAck()
        return eventRepository.getAllEvents().isEmpty()
    }

    /**
     * Terminates lifecycle, hence closes socket connection
     */
    fun terminate() {
        logger.debug { "CSBackgroundScheduler#terminate" }

        coroutineScope.cancel()
        backgroundLifecycleManager.onStop()
    }

    private suspend fun flushAllEvents() {
        logger.debug { "CSBackgroundScheduler#flushAllEvents" }

        val events = eventRepository.getAllEvents()
        if (events.isEmpty()) return

        forwardEvents(batch = events)
    }

    private suspend fun waitForAck() {
        logger.debug { "CSBackgroundScheduler#waitForAck" }

        var timeElapsed = 0
        while (eventRepository.getAllEvents().isNotEmpty() && timeElapsed <= TIMEOUT) {
            delay(ONE_SEC)
            timeElapsed += ONE_SEC.toInt()
        }
    }

    private suspend fun waitForNetwork() {
        logger.debug { "CSBackgroundScheduler#waitForNetwork" }

        var timeout = TIMEOUT
        while (!networkManager.isAvailable() && timeout > 0) {
            logger.debug { "CSBackgroundScheduler#waitForNetwork - Waiting for socket to be open" }

            delay(ONE_SEC)
            timeout -= ONE_SEC.toInt()
        }
    }
}
