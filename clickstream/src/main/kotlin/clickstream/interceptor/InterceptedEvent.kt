package clickstream.interceptor

import clickstream.internal.eventscheduler.CSEventData

public data class InterceptedEvent(val listOfEvent: List<CSEventData>, val state: EventState)