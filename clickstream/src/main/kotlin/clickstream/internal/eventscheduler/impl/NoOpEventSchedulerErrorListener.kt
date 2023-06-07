package clickstream.internal.eventscheduler.impl

import clickstream.internal.eventscheduler.CSEventSchedulerErrorListener

internal class NoOpEventSchedulerErrorListener : CSEventSchedulerErrorListener {
    override fun onError(tag: String, throwable: Throwable) {
        /*NoOp*/
    }
}