package clickstream.internal.utils

/**
 * Communicates responses from a server or offline requests. One and only one method will be
 * invoked in response to a given request.
 *
 * @param <T> Successful response.
 */
internal interface CSCallback<T> {

    /**
     * Invoked for a received response.
     *
     * @param <T> an object <T> for expected Successful response
     */
    fun onSuccess(data: T)

    /**
     * Invoked when a exception occurred talking to the server or when an unexpected
     *
     * @param error
     * @param guid
     */
    fun onError(error: Throwable, guid: String)
}
