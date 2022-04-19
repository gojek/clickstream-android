package clickstream.internal.utils

import java.util.UUID

/**
 * Generate a random ID every time the getId() is invoked.
 */
internal interface CSGuIdGenerator {

    /**
     * Returns a random unique ID
     */
    fun getId(): String
}

/**
 * Implementation of the [CSGuIdGenerator]
 */
internal class CSGuIdGeneratorImpl : CSGuIdGenerator {

    override fun getId(): String {
        return UUID.randomUUID().toString()
    }
}
