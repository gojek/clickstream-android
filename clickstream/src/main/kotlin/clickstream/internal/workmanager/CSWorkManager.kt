package clickstream.internal.workmanager

import android.content.Context
import clickstream.config.CSEventSchedulerConfig
import clickstream.config.CSRemoteConfig
import clickstream.internal.lifecycle.CSAppLifeCycle
import clickstream.internal.lifecycle.CSBackgroundLifecycleManager
import clickstream.internal.lifecycle.CSLifeCycleManager
import clickstream.logger.CSLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Acts as a wrapper around WorkManager, and is responsible for setting and cancelling tasks.
 */
@ExperimentalCoroutinesApi
internal class CSWorkManager(
    appLifeCycleObserver: CSAppLifeCycle,
    private val context: Context,
    private val eventSchedulerConfig: CSEventSchedulerConfig,
    private val logger: CSLogger,
    private val backgroundLifecycleManager: CSBackgroundLifecycleManager,
    private val remoteConfig: CSRemoteConfig
) : CSLifeCycleManager(appLifeCycleObserver) {

    init {
        logger.debug { "CSWorkManager#init" }
        addObserver()
    }

    override fun onStart() {
        logger.debug {
            "CSWorkManager#onStart -" +
                    "backgroundTaskEnabled ${eventSchedulerConfig.backgroundTaskEnabled}, " +
                    "isForegroundEventFlushEnabled ${remoteConfig.isForegroundEventFlushEnabled}"
        }

        backgroundLifecycleManager.onStop()
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
