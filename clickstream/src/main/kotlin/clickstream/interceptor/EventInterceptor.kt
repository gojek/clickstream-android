package clickstream.interceptor

import clickstream.internal.eventscheduler.CSEventData

public interface EventInterceptor {
    public fun onIntercept(interceptedEventBatch: InterceptedEvent)
    public fun startInterception()
    public fun stopInterception()
}