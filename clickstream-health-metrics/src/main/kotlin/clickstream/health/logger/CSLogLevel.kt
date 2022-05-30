package clickstream.logger

/**
 * Types for LogLevel config during development or production.
 */
public enum class CSLogLevel(
    private val value: Int
) {

    OFF(-1),
    INFO(0),
    DEBUG(2);

    /**
     * Returns the Integer value for the log type
     */
    public fun getValue(): Int {
        return value
    }
}