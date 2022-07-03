package clickstream.lifecycle

import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleObserver

/**
 * Lifecycle class for clickstream,
 * Start -> Called when app goes to background
 * Stop -> Called `connectionTerminationTimerWaitTimeInMillis` after app moves to background
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public abstract class CSLifeCycleManager(
    private val appLifeCycleObserver: CSAppLifeCycle
) : LifecycleObserver {

    /**
     * Subscribes to the application LifeCycle
     */
    public fun addObserver() {
        appLifeCycleObserver.addObserver(object : CSAppLifeCycleObserver {
            override fun onAppStart() {
                onStart()
            }

            override fun onAppStop() {
                onStop()
            }
        })
    }

    public abstract fun onStart()

    public abstract fun onStop()
}
