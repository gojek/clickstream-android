package clickstream.internal.analytics

internal object CSErrorReasons {
    const val PARSING_EXCEPTION = "parsing_exception"
    const val LOW_BATTERY = "low_battery"
    const val NETWORK_UNAVAILABLE = "network_unavailable"
    const val SOCKET_NOT_OPEN = "socket_not_open"
    const val UNKNOWN = "unknown"
    const val USER_UNAUTHORIZED = "401 Unauthorized"
    const val SOCKET_TIMEOUT = "socket_timeout"
    const val MAX_USER_LIMIT_REACHED = "max_user_limit_reached"
    const val MAX_CONNECTION_LIMIT_REACHED = "max_connection_limit_reached"
}
