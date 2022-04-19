package com.gojek.android.clickstream.eventscheduler.worker

import android.content.Context
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import clickstream.ACCESS_TOKEN
import clickstream.APP_VERSION
import clickstream.SESSION_ID
import clickstream.USER_ID
import clickstream.eventscheduler.worker.ClickStreamBackgroundWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.CoreMatchers.`is`
import org.junit.Rule
import java.util.UUID

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ClickstreamBackgroundWorkerTest {
    private lateinit var context: Context
    private lateinit var workManager: WorkManager

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(ApplicationProvider.getApplicationContext())
    }

    private val workerTag = "ClickstreamTask"

    private val accessToken = UUID.randomUUID().toString()
    private val sessionID = UUID.randomUUID().toString()
    private val userID = 123456
    private val appVersion = "1.0"

    @Test
    fun testOneTimeRequestWorkerShouldSucceed() {
        val input = workDataOf(
            ACCESS_TOKEN to accessToken,
            SESSION_ID to sessionID,
            USER_ID to userID,
            APP_VERSION to appVersion
        )
        val request = OneTimeWorkRequestBuilder<ClickStreamBackgroundWorkerTestable>()
            .setInputData(input)
            .build()
        workManager.enqueueUniqueWork(
            workerTag,
            ExistingWorkPolicy.REPLACE,
            request
        ).result.get()
        val workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
    }

    @Test
    fun testWorkerFailsIfAccessTokenIsNotProvided() {
        val input = workDataOf(SESSION_ID to sessionID)
        val request = OneTimeWorkRequestBuilder<ClickStreamBackgroundWorkerTestable>()
            .setInputData(input)
            .build()
        workManager.enqueueUniqueWork(
            workerTag,
            ExistingWorkPolicy.REPLACE,
            request
        ).result.get()
        val workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo.state, `is`(WorkInfo.State.FAILED))
    }

    @Test
    fun testWorkerFailsIfSessionIDIsNotProvided() {
        val input = workDataOf(ACCESS_TOKEN to accessToken)
        val request = OneTimeWorkRequestBuilder<ClickStreamBackgroundWorkerTestable>()
            .setInputData(input)
            .build()
        workManager.enqueueUniqueWork(
            workerTag,
            ExistingWorkPolicy.REPLACE,
            request
        ).result.get()
        val workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo.state, `is`(WorkInfo.State.FAILED))
    }

    internal class ClickStreamBackgroundWorkerTestable(context: Context, params: WorkerParameters) :
        ClickStreamBackgroundWorker(context, params) {

        override val coroutineContext = SynchronousExecutor().asCoroutineDispatcher()

        override suspend fun doWork(): Result = runBlocking {
            super.doWork()
        }
    }
}