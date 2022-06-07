package clickstream.interceptor

import clickstream.internal.eventscheduler.CSEventData

public interface EventInterceptor {
    public fun onIntercept(csEventData: CSEventData)
    public fun startInterception()
    public fun stopInterception()
}