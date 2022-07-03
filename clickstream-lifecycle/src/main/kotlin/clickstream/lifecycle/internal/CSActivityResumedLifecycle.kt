package clickstream.lifecycle.internal

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import clickstream.logger.CSLogger
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.lifecycle.LifecycleRegistry

internal class CSActivityResumedLifecycle(
    application: Application,
    private val logger: CSLogger,
    private val lifecycleRegistry: LifecycleRegistry
) : Lifecycle by lifecycleRegistry {

    init {
        logger.debug { "CSActivityResumedLifecycle#init" }

        application.registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks())
    }

    private inner class ActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityPaused(activity: Activity) {
            logger.debug { "CSActivityResumedLifecycle#onActivityPaused" }

            lifecycleRegistry.onNext(
                Lifecycle.State.Stopped.WithReason(ShutdownReason(1000, "App is paused"))
            )
        }

        override fun onActivityResumed(activity: Activity) {
            logger.debug { "CSActivityResumedLifecycle#onActivityResumed" }

            lifecycleRegistry.onNext(Lifecycle.State.Started)
        }

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityDestroyed(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    }
}