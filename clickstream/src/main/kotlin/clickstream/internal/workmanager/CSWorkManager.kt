package clickstream.internal.workmanager

import android.content.Context
import clickstream.config.CSEventSchedulerConfig
import clickstream.config.CSRemoteConfig
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.lifecycle.CSLifeCycleManager
import clickstream.logger.CSLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Acts as a wrapper around WorkManager, and is responsible for setting and cancelling tasks.
 */
@ExperimentalCoroutinesApi
internal class CSWorkManager(
    appLifeCycle: CSAppLifeCycle,
    internal val context: Context,
    private val eventSchedulerConfig: CSEventSchedulerConfig,
    private val logger: CSLogger,
    private val remoteConfig: CSRemoteConfig
) : CSLifeCycleManager(appLifeCycle) {

    init {
        logger.debug { "CSWorkManager#init" }
        addObserver()
    }

    override fun onStart() {
        logger.debug {
            "CSWorkManager#onStart : " +
            "backgroundTaskEnabled ${eventSchedulerConfig.backgroundTaskEnabled}, " +
            "isForegroundEventFlushEnabled ${remoteConfig.isForegroundEventFlushEnabled}"
        }

        if (remoteConfig.isForegroundEventFlushEnabled && eventSchedulerConfig.backgroundTaskEnabled) {
            setupFutureWork()
        }
    }

    override fun onStop() {
        logger.debug { "CSWorkManager#onStop" }

        executeOneTimeWork()
    }

    internal fun executeOneTimeWork() {
        logger.debug { "CSWorkManager#enqueueImmediateService - backgroundTaskEnabled ${eventSchedulerConfig.backgroundTaskEnabled}" }

        if (eventSchedulerConfig.backgroundTaskEnabled) {
            CSEventFlushOneTimeWorkManager.enqueueWork(context)
        }
    }

    private fun setupFutureWork() {
        logger.debug { "CSWorkManager#setupFutureWork" }

        CSEventFlushPeriodicWorkManager.enqueueWork(context)
    }
}
