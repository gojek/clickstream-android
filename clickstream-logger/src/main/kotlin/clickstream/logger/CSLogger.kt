package clickstream.logger

import android.util.Log
import clickstream.logger.CSLogConstant.CLICK_STREAM_LOG_TAG

/**
 * A logger helper that facade [android.util.Log].
 *
 * Internal log can use any engine e.g Timber or sl4j.
 */
public class CSLogger(
    private val logLevel: CSLogLevel
) {

    /**
     * Logs the message in debug mode
     *
     * @param message which will be printed
     */
    public inline fun debug(crossinline message: () -> String) {
        if (isDebug()) Log.d(CLICK_STREAM_LOG_TAG, message())
    }

    public inline fun error(message: () -> String) {
        if (isDebug()) Log.e(CLICK_STREAM_LOG_TAG, message())
    }

    /**
     * Logs the message along with the given suffix in debug mode
     *
     * @param suffix which holds some additional info or tag
     * @param message which will be printed
     */
    public inline fun debug(suffix: () -> String, message: () -> String) {
        if (isDebug()) Log.d("$CLICK_STREAM_LOG_TAG:${suffix()}", message())
    }

    /**
     * Logs the message along with the given suffix in debug mode.
     * If has any error, prints that too
     *
     * @param suffix which holds some additional info or tag
     * @param message which will be printed
     * @param t which holds the exception
     */
    public inline fun debug(suffix: () -> String, message: () -> String, t: () -> Throwable) {
        if (isDebug()) Log.d("$CLICK_STREAM_LOG_TAG:${suffix()}", message(), t())
    }

    @PublishedApi
    internal fun isDebug(): Boolean = logLevel.getValue() > CSLogLevel.INFO.getValue()
}