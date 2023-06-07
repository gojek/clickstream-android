package clickstream.internal.lifecycle.internal

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.lifecycle.LifecycleRegistry

internal class CSServiceStartedLifecycle(
    lifecycleOwner: LifecycleOwner,
    private val lifecycleRegistry: LifecycleRegistry
) : Lifecycle by lifecycleRegistry {

    init {
        Log.d("ClickStream", "CSServiceStartedLifecycle#init")

        lifecycleOwner.lifecycle.addObserver(ALifecycleObserver())
    }

    private inner class ALifecycleObserver : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            Log.d("ClickStream", "CSServiceStartedLifecycle#onResume")

            lifecycleRegistry.onNext(Lifecycle.State.Started)
        }

        override fun onStop(owner: LifecycleOwner) {
            Log.d("ClickStream", "CSServiceStartedLifecycle#onStop")

            lifecycleRegistry.onNext(Lifecycle.State.Stopped.AndAborted)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            Log.d("ClickStream", "CSServiceStartedLifecycle#onDestroy")

            lifecycleRegistry.onComplete()
        }
    }
}