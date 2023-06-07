package clickstream.internal.lifecycle

internal interface CSAppLifeCycle {
    fun addObserver(observer: CSAppLifeCycleObserver)
}
