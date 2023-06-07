package clickstream.internal.workmanager

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.CoroutineWorker
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
        val serviceLocator = CSServiceLocator.getInstance()
        val backgroundScheduler = serviceLocator.backgroundScheduler
        return try {
            Log.e("KSHITIJ", "HERE")
            val success = backgroundScheduler.sendEvents()
            if (success) Result.success() else Result.failure()
        } catch (e: Exception) {
            Log.e("KSHITIJ", e.toString())
            Result.failure()
        } finally {
            backgroundScheduler.terminate()
        }
    }

    companion object {

        private val handler by lazy { Handler(Looper.getMainLooper()) }

        fun addObserveForWorkManagerStatus(context: Context, workerName: String) {
            fun getWorkInfosByTagLiveData(workerName: String) {
                WorkManager.getInstance(context)
                    .getWorkInfosByTagLiveData(workerName).observe(
                        ProcessLifecycleOwner.get()
                    ) { state ->
                        state.forEach { entry ->
                            Log.d(
                                "ClickStream",
                                "CSFlushScheduledService#addObserveForWorkManagerStatus - $workerName : ${entry.progress} ${entry.state}"
                            )
                        }
                    }
            }

            handler.post {
                getWorkInfosByTagLiveData(workerName)
            }
        }
    }
}
