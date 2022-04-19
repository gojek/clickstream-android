package clickstream.internal.utils

/**
 * A sealed class which denotes the result of an action
 */
internal sealed class CSResult<T> {

    /**
     * When the action is success, it is called with the ack value
     */
    internal data class Success<T>(val value: T) : CSResult<T>()

    /**
     * When the action is failure, it is called with the ack value and error message
     */
    internal data class Failure<T>(val exception: Throwable, val value: T) : CSResult<T>()
}
