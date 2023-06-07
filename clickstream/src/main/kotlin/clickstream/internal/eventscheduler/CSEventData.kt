package clickstream.internal.eventscheduler

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import clickstream.CSEvent
import clickstream.eventName
import clickstream.health.model.CSEventHealth
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
        val messageType = with(messageName.split(".").last().toLowerCase(Locale.getDefault())) {
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

    public companion object {

        /**
         * Creates a new instance of EventData with the given [CSEvent]
         */
        public fun create(event: CSEvent): Pair<CSEventData, CSEventHealth> {
            val eventGuid: String = event.guid
            val eventTimeStamp: Long = event.timestamp.seconds
            val message: MessageLite = event.message
            val messageAsBytes: ByteArray = message.toByteArray()
            val messageSerializedSizeInBytes: Int = message.serializedSize
            val messageName: String = event.message::class.qualifiedName.orEmpty()
            val eventName: String = message.eventName() ?: ""

            return CSEventData(
                eventGuid = eventGuid,
                eventRequestGuid = null,
                eventTimeStamp = eventTimeStamp,
                messageAsBytes = messageAsBytes,
                messageName = messageName,
                isOnGoing = false
            ) to CSEventHealth(
                eventGuid = eventGuid,
                eventTimeStamp = eventTimeStamp,
                messageSerializedSizeInBytes = messageSerializedSizeInBytes,
                messageName = messageName,
                eventName = eventName
            )
        }

        /**
         * Creates a new instance of EventData with the given [CSEventInternal]
         */
        internal fun create(event: CSEventInternal): Pair<CSEventData, CSEventHealth> {
            when (event) {
                is CSEventInternal.CSEvent -> {
                    return CSEventData(
                        eventGuid = event.guid,
                        eventRequestGuid = null,
                        eventTimeStamp = event.timestamp.seconds,
                        messageAsBytes = event.message.toByteArray(),
                        messageName = event.message::class.qualifiedName.orEmpty(),
                        isOnGoing = false
                    ) to CSEventHealth(
                        eventGuid = event.guid,
                        eventTimeStamp = event.timestamp.seconds,
                        messageSerializedSizeInBytes = event.message.serializedSize,
                        messageName = event.message::class.qualifiedName.orEmpty(),
                        eventName = event.message.eventName() ?: ""
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
                    ) to CSEventHealth(
                        eventGuid = event.guid,
                        eventTimeStamp = event.timestamp.seconds,
                        messageSerializedSizeInBytes = event.eventData.size,
                        messageName = event.eventName,
                        eventName = event.eventName
                    )
                }
            }
        }

        private const val IN_KB = 1024
    }
}
