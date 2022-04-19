package clickstream.connection

import com.tinder.scarlet.Message

/**
 * Represents a WebSocket message.
 */
public sealed class CSMessage {
    /**
     * Represents a UTF-8 encoded text message.
     *
     * @property value The text data.
     */
    public data class Text(val value: String) : CSMessage()

    /**
     * Represents a binary message.
     *
     * @property value The binary data.
     */
    public data class Bytes(val value: ByteArray) : CSMessage() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Bytes

            if (!value.contentEquals(other.value)) return false

            return true
        }

        override fun hashCode(): Int {
            return value.contentHashCode()
        }
    }
}

internal fun Message.mapTo(): CSMessage {
    return when (this) {
        is Message.Bytes -> CSMessage.Bytes(this.value)
        is Message.Text -> CSMessage.Text(this.value)
    }
}
