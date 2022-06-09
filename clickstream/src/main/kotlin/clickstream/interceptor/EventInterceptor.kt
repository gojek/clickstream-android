package clickstream.interceptor

public interface EventInterceptor {
    public fun onIntercept(interceptedEventBatch: InterceptedEvent)
}