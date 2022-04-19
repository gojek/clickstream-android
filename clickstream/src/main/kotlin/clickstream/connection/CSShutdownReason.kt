package clickstream.connection

import com.tinder.scarlet.ShutdownReason

/**
 * Used to initiate a shutdown of a WebSocket.
 *
 * @property code Status code as defined by [Section 7.4 of RFC 6455](http://tools.ietf.org/html/rfc6455#section-7.4)
 * or `0`.
 * @property reason Reason for shutting down.
 */
public data class CSShutdownReason(val code: Int, val reason: String) {
    public companion object {
        private const val NORMAL_CLOSURE_STATUS_CODE = 1000
        private const val NORMAL_CLOSURE_REASON = "Normal closure"

        @JvmField
        public val GRACEFUL: CSShutdownReason =
            CSShutdownReason(NORMAL_CLOSURE_STATUS_CODE, NORMAL_CLOSURE_REASON)
    }
}

internal fun ShutdownReason.mapTo(): CSShutdownReason {
    return CSShutdownReason(this.code, this.reason)
}
