package clickstream.lifecycle.impl

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.lifecycle.CSAppLifeCycleObserver
import clickstream.logger.CSLogger
import java.util.concurrent.CopyOnWriteArrayList

/**
 * The concrete implementation of [CSAppLifeCycle] that using [ProcessLifecycleOwner],
 * which will respect to the Application Lifecycle.
 */
public class DefaultCSAppLifeCycleObserver(
    private val logger: CSLogger
) : CSAppLifeCycle, LifecycleObserver {

    private val observers = CopyOnWriteArrayList<CSAppLifeCycleObserver>()

    init {
        logger.debug { "DefaultCSAppLifeCycleObserver#init" }

        // Ensure that we're adding the observer on the main thread.
        // See: https://github.com/androidx/androidx/commit/1a587cf583809379add635a9047ded82d44306c8
        Handler(Looper.getMainLooper()).post {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public fun onAppStart() {
        logger.debug { "DefaultCSAppLifeCycleObserver#onAppStart" }

        observers.forEach { observer -> observer.onAppStart() }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public fun onAppStop() {
        logger.debug { "DefaultCSAppLifeCycleObserver#onAppStop" }

        observers.forEach { observer -> observer.onAppStop() }
    }

    override fun addObserver(observer: CSAppLifeCycleObserver) {
        observers.add(observer)
    }
}
