package clickstream.internal.eventscheduler

/**
 * A listener class that provides callback for error inside [CSEventScheduler] class.
 * */
public interface CSEventSchedulerErrorListener {
    /**
     * Callback for when error occurs
     *
     * @param tag: unique tag for the error.
     * @param throwable: Throwable object associated with the error.
     */
    public fun onError(tag: String, throwable: Throwable)
}