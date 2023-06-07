package clickstream.internal.lifecycle.internal

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import clickstream.internal.lifecycle.internal.CSAppResumedLifecycle.Companion.SHUT_DOWN_DUE_TO_APP_PAUSED
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.lifecycle.LifecycleRegistry

internal class CSActivityResumedLifecycle(
    application: Application,
    private val lifecycleRegistry: LifecycleRegistry
) : Lifecycle by lifecycleRegistry {

    init {
        Log.d("ClickStream", "CSActivityResumedLifecycle#init")
        application.registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks())
    }

    private inner class ActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityPaused(activity: Activity) {
            Log.d("ClickStream", "CSActivityResumedLifecycle#onActivityPaused")
            lifecycleRegistry.onNext(
                Lifecycle.State.Stopped.WithReason(ShutdownReason(SHUT_DOWN_DUE_TO_APP_PAUSED, "App is paused"))
            )
        }

        override fun onActivityResumed(activity: Activity) {
            Log.d("ClickStream", "CSActivityResumedLifecycle#onActivityResumed")
            lifecycleRegistry.onNext(Lifecycle.State.Started)
        }

        override fun onActivityStarted(activity: Activity) { /*NoOp*/ }

        override fun onActivityDestroyed(activity: Activity) { /*NoOp*/ }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) { /*NoOp*/ }

        override fun onActivityStopped(activity: Activity) { /*NoOp*/ }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) { /*NoOp*/ }
    }
}