package clickstream.lifecycle.internal

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import clickstream.logger.CSLogger
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.lifecycle.LifecycleRegistry

internal class CSServiceStartedLifecycle(
    lifecycleOwner: LifecycleOwner,
    private val logger: CSLogger,
    private val lifecycleRegistry: LifecycleRegistry
) : Lifecycle by lifecycleRegistry {

    init {
        logger.debug { "CSServiceStartedLifecycle#init" }

        lifecycleOwner.lifecycle.addObserver(ALifecycleObserver())
    }

    private inner class ALifecycleObserver : LifecycleObserver {
        @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_START)
        fun onResume() {
            logger.debug { "CSServiceStartedLifecycle#onResume" }

            lifecycleRegistry.onNext(Lifecycle.State.Started)
        }

        @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_STOP)
        fun onStop() {
            logger.debug { "CSServiceStartedLifecycle#onStop" }

            lifecycleRegistry.onNext(Lifecycle.State.Stopped.AndAborted)
        }

        @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            logger.debug { "CSServiceStartedLifecycle#onDestroy" }

            lifecycleRegistry.onComplete()
        }
    }
}