package clickstream.listener

/**
 * Data class used by [CSEventListener] to propagate clickstream events.
 *
 * */
public sealed class CSEventModel {

    /**
     * Intercepted event from clickstream with different states.
     * [Instant] events : fire and forget.
     * [Scheduled] events : Events that are [Scheduled] -> [Dispatched] -> [Acknowledged]
     *
     * @property eventId
     * @property eventName
     * @property productName
     * @property timeStamp
     * @constructor Create empty Intercepted event
     */
    public sealed class Event(
        public open val eventId: String,
        public open val eventName: String?,
        public open val productName: String,
        public open val timeStamp: Long,
    ) : CSEventModel() {
        /**
         * Instant
         *
         * @property eventId
         * @property eventName
         * @property productName
         * @property timeStamp
         * @property properties
         * @constructor Create empty Instant
         */
        public class Instant(
            override val eventId: String,
            override val eventName: String?,
            override val productName: String,
            override val timeStamp: Long,
            public val properties: Map<String, Any?>,
        ) : Event(eventId, eventName, productName, timeStamp)

        /**
         * Scheduled
         *
         * @property eventId
         * @property eventName
         * @property productName
         * @property timeStamp
         * @property properties
         * @constructor Create empty Scheduled
         */
        public class Scheduled(
            override val eventId: String,
            override val eventName: String?,
            override val productName: String,
            override val timeStamp: Long,
            public val properties: Map<String, Any?>,
        ) : Event(eventId, eventName, productName, timeStamp)

        /**
         * Dispatched
         *
         * @property eventId
         * @property eventName
         * @property productName
         * @property timeStamp
         * @constructor Create empty Dispatched
         */
        public class Dispatched(
            override val eventId: String,
            override val eventName: String?,
            override val productName: String,
            override val timeStamp: Long,
        ) : Event(eventId, eventName, productName, timeStamp)

        /**
         * Acknowledged
         *
         * @property eventId
         * @property eventName
         * @property productName
         * @property timeStamp
         * @constructor Create empty Acknowledged
         */
        public class Acknowledged(
            override val eventId: String,
            override val eventName: String?,
            override val productName: String,
            override val timeStamp: Long,
        ) : Event(eventId, eventName, productName, timeStamp)

    }

    /**
     * Intercepted event from clickstream with connection status.
     *
     * @property isConnected - is clickstream connected
     */
    class Connection(val isConnected: Boolean) : CSEventModel()
}