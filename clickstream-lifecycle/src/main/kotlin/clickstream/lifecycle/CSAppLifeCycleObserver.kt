package clickstream.lifecycle

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface CSAppLifeCycleObserver {
    public fun onAppStart()
    public fun onAppStop()
}