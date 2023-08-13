package clickstream.extension

import clickstream.health.proto.Health
import clickstream.internal.networklayer.proto.raccoon.Event
import com.google.protobuf.Internal.isValidUtf8
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
 * Checks whether the given message contains valid UTF8 characters.
 *
 * @return TRUE if the message has valid characters, else returns FALSE
 */
public fun MessageLite.isValidMessage(): Boolean {
    fun isNestedType(field: Field): Boolean {
        return field.type.name.contains("com.gojek.clickstream") && (field.name == "DEFAULT_INSTANCE").not()
    }

    fun isStringType(field: Field): Boolean = field.type == String::class.java

    var isValidMessage = true
    for (field in this.javaClass.declaredFields) {
        when {
            isNestedType(field) -> {
                field.isAccessible = true
                val messageLite = field.get(this) as? MessageLite
                isValidMessage =
                    if (messageLite != null) isValidMessage && messageLite.isValidMessage() else isValidMessage
            }
            isStringType(field) -> {
                field.isAccessible = true
                val value = field.get(this) as? String ?: ""
                isValidMessage = isValidMessage && isValidUtf8(value.toByteArray())
            }
        }
    }
    return isValidMessage
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
public fun MessageLite.protoName(): String {
    return this::class.qualifiedName?.split(".")?.last() ?: ""
}

/**
 * Typecasts the message into respective proto type and returns the eventName value.
 */
public fun MessageLite.eventName(): String? {
    return runCatching {
        val declaredField = this.javaClass.getDeclaredField("eventName_")
        declaredField.isAccessible = true
        declaredField.get(this) as String
    }.getOrNull()
}

public fun MessageLite.messageName(): String {
    return this::class.qualifiedName.orEmpty()
}

/**
 * Converts [MessageLite] to [Map<String,Any?>] using reflection.
 * */
public fun MessageLite.toFlatMap(): Map<String, Any?> {

    fun isValidField(field: Field) =
        field.name.split(".").last().run {
            endsWith("_")
        }

    fun getPropertyNameFromField(field: Field) = field.name.split(".").last()
        .substringBefore("_")

    val propertyMap = mutableMapOf<String, Any?>()

    fun populatePropertyMap(messageLite: MessageLite, prefix: String) {
        val declaredMethods = messageLite.javaClass.declaredFields
        val validFields = declaredMethods.filter { isValidField(it) }
        return validFields.forEach {
            it.isAccessible = true
            val fieldName = getPropertyNameFromField(it)
            val key = if (prefix.isNotEmpty()) "$prefix.$fieldName" else fieldName
            when (val fieldValue = it.get(messageLite)) {
                is MessageLite -> populatePropertyMap(fieldValue, key)
                else -> propertyMap[key] = fieldValue
            }
        }
    }
    populatePropertyMap(this, "")
    return propertyMap
}