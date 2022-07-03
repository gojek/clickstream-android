package clickstream.lifecycle

import androidx.annotation.RestrictTo

/**
 * [CSAppLifeCycle] is an interface which provides onStart and onStop lifecycle based
 * on the concrete implementation, as for now we have 2 implementation, such as:
 * - [DefaultCSAppLifeCycleObserver] which respect to the Application Lifecycle
 * - [DefaultCSActivityLifeCycleObserver] which respect to the Activities Lifecycle
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface CSAppLifeCycle {
    /**
     * Added observer to the internal machinary of [CSAppLifeCycle]
     */
    public fun addObserver(observer: CSAppLifeCycleObserver)
}
