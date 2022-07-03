package clickstream.internal.workmanager

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.CoroutineWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import clickstream.internal.di.CSServiceLocator
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal open class CSBaseEventFlushWorkManager(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        CSServiceLocator.getInstance().logger.debug { "CSBaseEventFlushWorkManager#doWork" }

        return try {
            val serviceLocator = CSServiceLocator.getInstance()
            val backgroundScheduler = serviceLocator.workManagerEventScheduler
            val success = backgroundScheduler.sendEvents()
            if (success) Result.success() else Result.failure()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {

        private val handler by lazy { Handler(Looper.getMainLooper()) }

        fun addObserveForWorkManagerStatus(context: Context, workerName: String) {
            fun getWorkInfosByTagLiveData(workerName: String) {
                val logger = CSServiceLocator.getInstance().logger
                WorkManager.getInstance(context)
                    .getWorkInfosByTagLiveData(workerName).observe(
                        ProcessLifecycleOwner.get()
                    ) { state: MutableList<WorkInfo> ->
                        state.forEach { entry ->
                            logger.debug { "CSFlushScheduledService#addObserveForWorkManagerStatus - $workerName : ${entry.progress} ${entry.state}" }
                        }
                    }
            }

            handler.post {
                getWorkInfosByTagLiveData(workerName)
            }
        }
    }
}
