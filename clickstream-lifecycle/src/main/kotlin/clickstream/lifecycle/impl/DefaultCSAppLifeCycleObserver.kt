package clickstream.lifecycle.impl

import android.app.Activity
import android.app.Application
import android.content.Context
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.lifecycle.CSAppLifeCycleObserver
import clickstream.lifecycle.CSEmptyActivityLifecycleCallbacks
import java.util.concurrent.CopyOnWriteArrayList

public class DefaultCSAppLifeCycleObserver(
    context: Context
) : CSAppLifeCycle {

    private val observers = CopyOnWriteArrayList<CSAppLifeCycleObserver>()

    init {
        (context.applicationContext as Application)
            .registerActivityLifecycleCallbacks(object : CSEmptyActivityLifecycleCallbacks {
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
