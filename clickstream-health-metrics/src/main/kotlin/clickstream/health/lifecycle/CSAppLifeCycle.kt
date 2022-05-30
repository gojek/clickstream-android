package clickstream.internal.lifecycle

public interface CSAppLifeCycle {
    public fun addObserver(observer: CSAppLifeCycleObserver)
}
