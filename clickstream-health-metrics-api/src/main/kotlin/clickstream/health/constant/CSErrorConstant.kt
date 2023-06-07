package clickstream.health.constant

public object CSErrorConstant {
    public const val PARSING_EXCEPTION: String = "parsing_exception"
    public const val LOW_BATTERY: String = "low_battery"
    public const val NETWORK_UNAVAILABLE: String = "network_unavailable"
    public const val SOCKET_NOT_OPEN: String = "socket_not_open"
    public const val UNKNOWN: String = "unknown"
    public const val USER_UNAUTHORIZED: String = "401 Unauthorized"
    public const val SOCKET_TIMEOUT: String = "socket_timeout"
    public const val MAX_USER_LIMIT_REACHED: String = "max_user_limit_reached"
    public const val MAX_CONNECTION_LIMIT_REACHED: String = "max_connection_limit_reached"
}