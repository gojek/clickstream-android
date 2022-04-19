package clickstream.internal.lifecycle

import androidx.lifecycle.LifecycleObserver

/**
 * Lifecycle class for clickstream,
 * Start -> Called when app goes to background
 * Stop -> Called `connectionTerminationTimerWaitTimeInMillis` after app moves to background
 */
internal abstract class CSLifeCycleManager(
    private val appLifeCycleObserver: CSAppLifeCycle
) : LifecycleObserver {

    /**
     * Subscribes to the application LifeCycle
     */
    fun addObserver() {
        appLifeCycleObserver.addObserver(object : CSAppLifeCycleObserver {
            override fun onAppStart() {
                onStart()
            }

            override fun onAppStop() {
                onStop()
            }
        })
    }

    abstract fun onStart()

    abstract fun onStop()
}
