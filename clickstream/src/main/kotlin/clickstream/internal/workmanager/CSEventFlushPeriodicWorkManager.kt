package clickstream.internal.workmanager

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.NetworkType.CONNECTED
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import clickstream.internal.di.CSServiceLocator
import clickstream.logger.CSLogLevel.INFO
import java.util.concurrent.TimeUnit.HOURS
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * The coroutine worker invoked by WorkManager which in-turn calls
 * the backgroundScheduler to perform the task
 */
@ExperimentalCoroutinesApi
internal class CSEventFlushPeriodicWorkManager private constructor(
    context: Context,
    params: WorkerParameters
) : CSBaseEventFlushWorkManager(context, params) {

    companion object {

        private const val CLICKSTREAM_FUTURE_TASK_TAG = "Clickstream_Event_Flushing_Future_Task"
        private const val REPEATED_INTERVAL = 6L

        fun enqueueWork(context: Context) {
            PeriodicWorkRequest.Builder(
                CSBaseEventFlushWorkManager::class.java,
                REPEATED_INTERVAL,
                HOURS
            ).setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(CONNECTED)
                    .build()
            ).addTag(CLICKSTREAM_FUTURE_TASK_TAG).build()
                .let { request ->
                    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                        CLICKSTREAM_FUTURE_TASK_TAG, KEEP, request
                    )
                }

            if (CSServiceLocator.getInstance().logLevel >= INFO) {
                addObserveForWorkManagerStatus(context, CLICKSTREAM_FUTURE_TASK_TAG)
            }
        }

        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(CLICKSTREAM_FUTURE_TASK_TAG)
        }
    }
}
