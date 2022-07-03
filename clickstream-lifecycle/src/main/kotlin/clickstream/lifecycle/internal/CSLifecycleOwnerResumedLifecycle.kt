package clickstream.lifecycle.internal

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import clickstream.logger.CSLogger
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.lifecycle.LifecycleRegistry

internal class CSLifecycleOwnerResumedLifecycle(
    lifecycleOwner: LifecycleOwner,
    private val logger: CSLogger,
    private val lifecycleRegistry: LifecycleRegistry
) : Lifecycle by lifecycleRegistry {

    init {
        logger.debug { "CSLifecycleOwnerResumedLifecycle#init" }

        lifecycleOwner.lifecycle.addObserver(ALifecycleObserver())
    }

    private inner class ALifecycleObserver : LifecycleObserver {
        @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            logger.debug { "CSLifecycleOwnerResumedLifecycle#onPause" }

            lifecycleRegistry.onNext(
                Lifecycle.State.Stopped.WithReason(ShutdownReason(1000, "Paused"))
            )
        }

        @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_RESUME)
        fun onResume() {
            logger.debug { "CSLifecycleOwnerResumedLifecycle#onResume" }

            lifecycleRegistry.onNext(Lifecycle.State.Started)
        }

        @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            logger.debug { "CSLifecycleOwnerResumedLifecycle#onDestroy" }

            lifecycleRegistry.onComplete()
        }
    }
}