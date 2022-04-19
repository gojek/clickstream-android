package clickstream.internal.workmanager

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy.KEEP
import androidx.work.NetworkType.CONNECTED
import androidx.work.OneTimeWorkRequestBuilder
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
internal class CSEventFlushOneTimeWorkManager private constructor(
    context: Context,
    params: WorkerParameters
) : CSBaseEventFlushWorkManager(context, params) {

    companion object {

        private const val CLICKSTREAM_TASK_TAG = "Clickstream_Event_Flushing_Task"

        fun enqueueWork(context: Context) {
            if (CSServiceLocator.getInstance().logLevel >= INFO) {
                Log.d("ClickStream", "CSEventFlushOneTimeWorkManager#enqueueWork")
            }

            OneTimeWorkRequestBuilder<CSBaseEventFlushWorkManager>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(CONNECTED)
                        .build()
                )
                .addTag(CLICKSTREAM_TASK_TAG)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    CSServiceLocator.getInstance().eventSchedulerConfig.workRequestDelayInHr,
                    HOURS
                )
                .build()
                .let { request ->
                    WorkManager.getInstance(context)
                        .enqueueUniqueWork(CLICKSTREAM_TASK_TAG, KEEP, request)
                }

            if (CSServiceLocator.getInstance().logLevel >= INFO) {
                addObserveForWorkManagerStatus(context, CLICKSTREAM_TASK_TAG)
            }
        }

        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(CLICKSTREAM_TASK_TAG)
        }
    }
}
