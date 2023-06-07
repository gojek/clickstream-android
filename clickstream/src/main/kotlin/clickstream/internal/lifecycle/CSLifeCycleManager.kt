package clickstream.internal.lifecycle

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

internal abstract class CSLifeCycleManager(
    private val appLifeCycleObserver: CSAppLifeCycle
) : DefaultLifecycleObserver {

    abstract val tag: String
    /**
     * Subscribes to the application LifeCycle
     */
    fun addObserver() {
        // Ensure that we're adding the observer on the main thread.
        // See: https://github.com/androidx/androidx/commit/1a587cf583809379add635a9047ded82d44306c8
        Handler(Looper.getMainLooper()).post {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        onStart()
    }

    override fun onStop(owner: LifecycleOwner) {
        onStop()
    }

    abstract fun onStart()

    abstract fun onStop()
}
