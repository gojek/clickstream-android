package clickstream.internal.lifecycle

internal interface CSAppLifeCycleObserver {
    fun onAppStart()
    fun onAppStop()
}