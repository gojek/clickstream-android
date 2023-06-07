package clickstream.internal.lifecycle

import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.Lifecycle.State.Started
import com.tinder.scarlet.Lifecycle.State.Stopped.WithReason
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.lifecycle.LifecycleRegistry

private const val SHUTDOWN_REASON_CODE = 1001

/**
 * Lifecycle class for clickstream,
 * Start -> Called when app goes to background
 * Stop -> Called `connectionTerminationTimerWaitTimeInMillis` after app moves to background
 */
internal class CSBackgroundLifecycleManager(
    private val lifecycleRegistry: LifecycleRegistry
) : Lifecycle by lifecycleRegistry {

    constructor() : this(LifecycleRegistry())

    /**
     * Starts the socket
     */
    fun onStart() {
        lifecycleRegistry.onNext(Started)
    }

    /**
     * Stops the socket
     */
    fun onStop() {
        lifecycleRegistry.onNext(
            WithReason(
                ShutdownReason(SHUTDOWN_REASON_CODE, "Gracefully stopped by the lifecycle")
            )
        )
    }
}
