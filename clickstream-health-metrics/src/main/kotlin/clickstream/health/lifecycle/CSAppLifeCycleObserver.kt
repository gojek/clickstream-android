package clickstream.internal.lifecycle

public interface CSAppLifeCycleObserver {
    public fun onAppStart()
    public fun onAppStop()
}