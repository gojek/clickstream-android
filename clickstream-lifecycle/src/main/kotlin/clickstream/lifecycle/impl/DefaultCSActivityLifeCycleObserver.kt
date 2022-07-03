package clickstream.lifecycle.impl

import android.app.Activity
import android.app.Application
import android.content.Context
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.lifecycle.CSAppLifeCycleObserver
import clickstream.lifecycle.internal.CSEmptyActivityLifecycleCallbacks
import clickstream.logger.CSLogger
import java.util.concurrent.CopyOnWriteArrayList

/**
 * The concrete implementation of [CSAppLifeCycle] that using [Application.registerActivityLifecycleCallbacks],
 * which will respect to the Activities Lifecycle.
 */
public class DefaultCSActivityLifeCycleObserver(
    context: Context,
    private val logger: CSLogger,
) : CSAppLifeCycle {

    private val observers = CopyOnWriteArrayList<CSAppLifeCycleObserver>()

    init {
        logger.debug { "DefaultCSActivityLifeCycleObserver#init" }

        (context.applicationContext as Application).registerActivityLifecycleCallbacks(
            object : CSEmptyActivityLifecycleCallbacks {
                override fun onActivityStarted(activity: Activity) {
                    super.onActivityStarted(activity)
                    logger.debug { "DefaultCSActivityLifeCycleObserver#onActivityStarted" }

                    observers.forEach { observer -> observer.onAppStart() }
                }

                override fun onActivityStopped(activity: Activity) {
                    super.onActivityStopped(activity)
                    logger.debug { "DefaultCSActivityLifeCycleObserver#onActivityStopped" }

                    observers.forEach { observer -> observer.onAppStop() }
                }
            })
    }

    override fun addObserver(observer: CSAppLifeCycleObserver) {
        observers.add(observer)
    }
}
