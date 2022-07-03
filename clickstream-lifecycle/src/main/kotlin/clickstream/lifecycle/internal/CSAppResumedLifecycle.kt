package clickstream.lifecycle.internal

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import clickstream.logger.CSLogger
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.lifecycle.LifecycleRegistry

internal class CSAppResumedLifecycle(
    private val logger: CSLogger,
    private val lifecycleRegistry: LifecycleRegistry
) : Lifecycle by lifecycleRegistry, LifecycleObserver {

    init {
        logger.debug { "CSAppResumedLifecycle#init" }

        // Ensure that we're adding the observer on the main thread.
        // See: https://github.com/androidx/androidx/commit/1a587cf583809379add635a9047ded82d44306c8
        Handler(Looper.getMainLooper()).post {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
    }

    @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_START)
    public fun onAppStart() {
        logger.debug { "CSAppResumedLifecycle#onAppStart" }

        lifecycleRegistry.onNext(Lifecycle.State.Started)
    }

    @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_STOP)
    public fun onAppStop() {
        logger.debug { "CSAppResumedLifecycle#onAppStop" }

        lifecycleRegistry.onNext(
            Lifecycle.State.Stopped.WithReason(ShutdownReason(1000, "App is paused"))
        )
    }
}