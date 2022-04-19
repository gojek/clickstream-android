package clickstream.extension

import com.gojek.clickstream.de.Event
import com.gojek.clickstream.internal.Health
import com.google.protobuf.MessageLite
import java.lang.reflect.Field
import java.util.Locale

/**
 * Gets the field for the given field name
 */
public fun MessageLite.getField(fieldName: String): Field {
    return this.javaClass.getDeclaredField(fieldName)
}

/**
 * Checks whether the Event type is Health or not
 *
 * @return true - If it's a health event
 * @return false - If it's not a health event
 */
public fun Event.isHealthEvent(): Boolean {
    val currentType = this.type
    return currentType == Health::class.qualifiedName
        .orEmpty().split(".").last()
        .toLowerCase(Locale.ROOT)
}

/**
 * Returns the name of the message.
 */
public fun MessageLite.eventName(): String {
    return this::class.qualifiedName?.split(".")?.last() ?: ""
}
