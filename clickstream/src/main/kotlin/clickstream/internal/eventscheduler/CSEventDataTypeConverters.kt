package clickstream.internal.eventscheduler

import androidx.room.TypeConverter
import com.google.protobuf.ByteString
import java.nio.charset.Charset

/**
 * TypeConverter supplied to the room to convert the
 * [ByteString] into a string so that it can be stored into
 * the DB.
 *
 * Also, supports converting back the string into the proto message.
 */
internal class CSEventDataTypeConverters {

    /**
     * fromByteString converts the byte string into string.
     *
     * @param byteString - [ByteString] message which needs to be converted
     */
    @TypeConverter
    fun fromByteString(byteString: ByteString): String =
        byteString.toString(Charset.defaultCharset())

    /**
     * toByteString converts the string into byte string.
     *
     * @param messageAsString - string which is converted into
     * [ByteString]
     */
    @TypeConverter
    fun toByteString(messageAsString: String): ByteString =
        ByteString.copyFrom(messageAsString, Charset.defaultCharset())
}
