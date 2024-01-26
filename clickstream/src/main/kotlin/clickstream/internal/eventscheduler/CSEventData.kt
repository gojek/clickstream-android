package clickstream.internal.eventscheduler

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import clickstream.CSEvent
import clickstream.eventName
import clickstream.health.model.CSEventForHealth
import clickstream.internal.CSEventInternal
import com.gojek.clickstream.de.Event
import com.google.protobuf.ByteString
import com.google.protobuf.MessageLite
import java.util.Locale

/**
 * The data class which will be stored to the DB
 *
 * @param eventGuid - The unique ID for each event batch
 * @param eventRequestGuid - The ID for group of events
 * @param eventTimeStamp - The timestamp at which the event was created
 * @param isOnGoing - Is the Event processed or not
 * @param messageAsBytes - The event sent from the client
 */
@Entity(tableName = "EventData")
public data class CSEventData(
    @PrimaryKey
    val eventGuid: String,
    val eventRequestGuid: String?,
    val eventTimeStamp: Long,
    val isOnGoing: Boolean,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val messageAsBytes: ByteArray,
    val messageName: String
) {

    /**
     * Constructs the [Event] data using the messageAsBytes & messageName
     *
     * @return [Event]
     */
    public fun event(appPrefix: String? = null): Event {
        val messageType = with(messageName.split(".").last().lowercase(Locale.getDefault())) {
            if (!appPrefix.isNullOrEmpty()) {
                "$appPrefix-$this"
            } else {
                this
            }
        }
        return Event.newBuilder().apply {
            eventBytes = ByteString.copyFrom(messageAsBytes)
            type = messageType
        }.build()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CSEventData

        if (eventGuid != other.eventGuid) return false
        if (eventRequestGuid != other.eventRequestGuid) return false
        if (eventTimeStamp != other.eventTimeStamp) return false
        if (isOnGoing != other.isOnGoing) return false
        if (!messageAsBytes.contentEquals(other.messageAsBytes)) return false
        if (messageName != other.messageName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = eventGuid.hashCode()
        result = 31 * result + (eventRequestGuid?.hashCode() ?: 0)
        result = 31 * result + eventTimeStamp.hashCode()
        result = 31 * result + isOnGoing.hashCode()
        result = 31 * result + messageAsBytes.contentHashCode()
        result = 31 * result + messageName.hashCode()
        return result
    }

    internal fun toCSEventForHealth(batchId: String? = null) =
        CSEventForHealth(eventGuid = eventGuid, batchGuid = batchId ?: eventRequestGuid ?: "")

    public companion object {

        /**
         * Creates a new instance of EventData with the given [CSEvent]
         */
        public fun create(event: CSEvent): CSEventData {
            val eventGuid: String = event.guid
            val eventTimeStamp: Long = event.timestamp.seconds
            val message: MessageLite = event.message
            val messageAsBytes: ByteArray = message.toByteArray()
            val messageName: String = event.message::class.qualifiedName.orEmpty()

            return CSEventData(
                eventGuid = eventGuid,
                eventRequestGuid = null,
                eventTimeStamp = eventTimeStamp,
                messageAsBytes = messageAsBytes,
                messageName = messageName,
                isOnGoing = false
            )
        }

        /**
         * Creates a new instance of EventData with the given [CSEventInternal]
         */
        internal fun create(event: CSEventInternal): CSEventData {
            when (event) {
                is CSEventInternal.CSEvent -> {
                    return CSEventData(
                        eventGuid = event.guid,
                        eventRequestGuid = null,
                        eventTimeStamp = event.timestamp.seconds,
                        messageAsBytes = event.message.toByteArray(),
                        messageName = event.message::class.qualifiedName.orEmpty(),
                        isOnGoing = false
                    )
                }
                is CSEventInternal.CSBytesEvent -> {
                    return CSEventData(
                        eventGuid = event.guid,
                        eventRequestGuid = null,
                        eventTimeStamp = event.timestamp.seconds,
                        messageAsBytes = event.eventData,
                        messageName = event.eventName,
                        isOnGoing = false
                    )
                }
            }
        }

        private const val IN_KB = 1024
    }
}
