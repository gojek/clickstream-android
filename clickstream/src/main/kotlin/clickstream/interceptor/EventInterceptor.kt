package clickstream.interceptor

public interface EventInterceptor {
    public fun onIntercept(interceptedEventBatch: InterceptedEvent)
    public fun startInterception()
    public fun stopInterception()
}