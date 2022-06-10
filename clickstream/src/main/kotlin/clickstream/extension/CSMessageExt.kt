package clickstream.extension

import com.gojek.clickstream.de.Event
import com.gojek.clickstream.internal.Health
import com.google.protobuf.Internal.isValidUtf8
import com.google.protobuf.MessageLite
import org.json.JSONObject
import java.lang.reflect.Field
import java.lang.reflect.Method
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

/**
 * Converts [MessageLite] to [JSONObject] using reflection.
 * */
public fun MessageLite.toJson(): JSONObject {

    fun isValidMethod(method: Method) =
        method.parameterTypes.isEmpty() && method.name.split(".").last().run {
            startsWith("get") && !equals("getDefaultInstance") && !contains("Byte")
        }

    fun getPropertyNameFromMethod(methodName: String) = methodName.split(".").last()
        .substring(3).substringBefore("(")

    fun getAllPropertyValueAndName(messageLite: MessageLite): List<Property> {
        val declaredMethods = messageLite.javaClass.declaredMethods
        val validMethods = declaredMethods.filter { isValidMethod(it) }
        return validMethods.map {
            it.isAccessible = true
            Property(
                getPropertyNameFromMethod(it.name),
                it.invoke(messageLite)
            )
        }
    }

    fun getJson(messageLite: MessageLite, json: JSONObject): JSONObject {
        val listOfProperty = getAllPropertyValueAndName(messageLite)
        listOfProperty.forEach { property ->
            val value = when (property.value) {
                is MessageLite -> getJson(property.value, JSONObject())
                is List<*> -> property.value.map { propertyValue ->
                    (propertyValue as? MessageLite)?.run {
                        getJson(this, JSONObject())
                    } ?: propertyValue
                }
                else -> property.value
            }
            json.put(property.name, value)
        }
        return json
    }
    return getJson(this, JSONObject())
}

private data class Property(val name: String, val value: Any?)