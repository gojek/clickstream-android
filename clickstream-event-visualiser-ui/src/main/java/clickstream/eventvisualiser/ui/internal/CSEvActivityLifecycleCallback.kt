package clickstream.eventvisualiser.ui.internal

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

internal class CSEvActivityLifecycleCallback : Application.ActivityLifecycleCallbacks {

    private var currentActivity: WeakReference<Activity>? = null

    override fun onActivityCreated(activity: Activity, p1: Bundle?) {

    }

    override fun onActivityStarted(p0: Activity) {
    }

    override fun onActivityResumed(p0: Activity) {
        currentActivity = WeakReference(p0)
    }

    override fun onActivityPaused(p0: Activity) {
        currentActivity = null
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {

    }

    fun getCurrentActivity(): Activity {
        return currentActivity?.get() ?: throw throw IllegalStateException(
            "No activity in foreground!. " +
                    "Make sure atleast one activity is in foreground before " +
                    "calling CSEventVisualiserUI#show"
        )
    }

}