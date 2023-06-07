package clickstream.internal.lifecycle.internal

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.tinder.scarlet.Lifecycle
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.lifecycle.LifecycleRegistry

internal class CSAppResumedLifecycle(
    private val lifecycleRegistry: LifecycleRegistry
) : Lifecycle by lifecycleRegistry, DefaultLifecycleObserver {

    init {
        Log.d("ClickStream", "CSAppResumedLifecycle#init")

        // Ensure that we're adding the observer on the main thread.
        // See: https://github.com/androidx/androidx/commit/1a587cf583809379add635a9047ded82d44306c8
        Handler(Looper.getMainLooper()).post {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d("ClickStream", "CSAppResumedLifecycle#onAppStart")

        lifecycleRegistry.onNext(Lifecycle.State.Started)
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d("ClickStream", "CSAppResumedLifecycle#onAppStop")

        lifecycleRegistry.onNext(
            Lifecycle.State.Stopped.WithReason(
                ShutdownReason(
                    SHUT_DOWN_DUE_TO_APP_PAUSED,
                    "App is paused"
                )
            )
        )
    }

    companion object {
        internal const val SHUT_DOWN_DUE_TO_APP_PAUSED = 1000
    }
}