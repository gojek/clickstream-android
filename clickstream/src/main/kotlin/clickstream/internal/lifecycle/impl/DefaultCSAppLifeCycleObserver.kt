package clickstream.internal.lifecycle.impl

import android.app.Activity
import android.app.Application
import android.content.Context
import clickstream.internal.lifecycle.CSAppLifeCycle
import clickstream.internal.lifecycle.CSAppLifeCycleObserver
import clickstream.internal.lifecycle.EmptyActivityLifecycleCallbacks
import java.util.concurrent.CopyOnWriteArrayList

internal class DefaultCSAppLifeCycleObserver(
    context: Context
) : CSAppLifeCycle {

    private val observers = CopyOnWriteArrayList<CSAppLifeCycleObserver>()

    init {
        (context.applicationContext as Application)
            .registerActivityLifecycleCallbacks(object : EmptyActivityLifecycleCallbacks {
                override fun onActivityStarted(activity: Activity) {
                    super.onActivityStarted(activity)
                    observers.forEach { observer ->
                        observer.onAppStart()
                    }
                }

                override fun onActivityStopped(activity: Activity) {
                    super.onActivityStopped(activity)
                    observers.forEach { observer ->
                        observer.onAppStop()
                    }
                }
            })
    }

    override fun addObserver(observer: CSAppLifeCycleObserver) {
        observers.add(observer)
    }
}
