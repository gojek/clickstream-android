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
    appLifeCycleObserver: CSAppLifeCycle,
    private val context: Context,
    private val eventSchedulerConfig: CSEventSchedulerConfig,
    private val logger: CSLogger,
    private val remoteConfig: CSRemoteConfig
) : CSLifeCycleManager(appLifeCycleObserver) {

    init {
        logger.debug { "$tag#init" }
        addObserver()
    }

    override val tag: String
        get() = "CSWorkManager"

    override fun onStart() {
        logger.debug {
            "$tag#onStart -" +
                    "backgroundTaskEnabled ${eventSchedulerConfig.backgroundTaskEnabled}, " +
                    "isForegroundEventFlushEnabled ${remoteConfig.isForegroundEventFlushEnabled}"
        }
        cancelBackgroundWork()
        if (remoteConfig.isForegroundEventFlushEnabled && eventSchedulerConfig.backgroundTaskEnabled) {
            setupFutureWork()
        }
    }

    override fun onStop() {
        logger.debug { "$tag#onStop" }

        executeOneTimeWork()
    }

    internal fun executeOneTimeWork() {
        logger.debug { "$tag#enqueueImmediateService - backgroundTaskEnabled ${eventSchedulerConfig.backgroundTaskEnabled}" }

        if (eventSchedulerConfig.backgroundTaskEnabled) {
            CSEventFlushOneTimeWorkManager.enqueueWork(context)
        }
    }

    private fun setupFutureWork() {
        logger.debug { "$tag#setupFutureWork" }

        CSEventFlushPeriodicWorkManager.enqueueWork(context)
    }

    private fun cancelBackgroundWork() {
        CSEventFlushOneTimeWorkManager.cancelWork(context)
    }
}
