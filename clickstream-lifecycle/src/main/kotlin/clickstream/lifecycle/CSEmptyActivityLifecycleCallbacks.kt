package clickstream.lifecycle

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle

internal interface CSEmptyActivityLifecycleCallbacks : ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) { /*No Op */ }

    override fun onActivityStarted(activity: Activity) { /*No Op */ }

    override fun onActivityResumed(activity: Activity) { /*No Op */ }

    override fun onActivityPaused(activity: Activity) { /*No Op */ }

    override fun onActivityStopped(activity: Activity) { /*No Op */ }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) { /*No Op */ }

    override fun onActivityDestroyed(activity: Activity) { /*No Op */ }
}
