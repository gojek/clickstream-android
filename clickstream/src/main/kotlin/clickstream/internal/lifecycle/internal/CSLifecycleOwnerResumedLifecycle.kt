package clickstream.internal.lifecycle.internal

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import clickstream.internal.lifecycle.internal.CSAppResumedLifecycle.Companion.SHUT_DOWN_DUE_TO_APP_PAUSED
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.lifecycle.LifecycleRegistry

internal class CSLifecycleOwnerResumedLifecycle(
    lifecycleOwner: LifecycleOwner,
    private val lifecycleRegistry: LifecycleRegistry
) : Lifecycle by lifecycleRegistry {

    init {
        Log.d("ClickStream", "CSLifecycleOwnerResumedLifecycle#init")
        lifecycleOwner.lifecycle.addObserver(ALifecycleObserver())
    }

    private inner class ALifecycleObserver : DefaultLifecycleObserver {
        override fun onPause(owner: LifecycleOwner) {
            Log.d("ClickStream", "CSLifecycleOwnerResumedLifecycle#onPause")

            lifecycleRegistry.onNext(
                Lifecycle.State.Stopped.WithReason(
                    ShutdownReason(
                        SHUT_DOWN_DUE_TO_APP_PAUSED,
                        "App is paused"
                    )
                )
            )
        }

        override fun onResume(owner: LifecycleOwner) {
            Log.d("ClickStream", "CSLifecycleOwnerResumedLifecycle#onResume")

            lifecycleRegistry.onNext(Lifecycle.State.Started)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            Log.d("ClickStream", "CSLifecycleOwnerResumedLifecycle#onDestroy")

            lifecycleRegistry.onComplete()
        }
    }
}