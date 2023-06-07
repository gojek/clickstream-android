package clickstream.internal.lifecycle.impl

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import clickstream.internal.lifecycle.CSAppLifeCycle
import clickstream.internal.lifecycle.CSAppLifeCycleObserver
import java.util.concurrent.CopyOnWriteArrayList

internal class DefaultCSAppLifeCycleObserver : CSAppLifeCycle, DefaultLifecycleObserver {

    private val observers = CopyOnWriteArrayList<CSAppLifeCycleObserver>()

    init {
        // Ensure that we're adding the observer on the main thread.
        // See: https://github.com/androidx/androidx/commit/1a587cf583809379add635a9047ded82d44306c8
        Handler(Looper.getMainLooper()).post {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        observers.forEach { it.onAppStart() }
    }

    override fun onStop(owner: LifecycleOwner) {
        observers.forEach { it.onAppStop() }
    }

    override fun addObserver(observer: CSAppLifeCycleObserver) {
        observers.add(observer)
    }
}
