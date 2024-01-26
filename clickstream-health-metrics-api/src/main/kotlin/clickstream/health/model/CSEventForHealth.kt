package clickstream.health.model

import clickstream.health.intermediate.CSHealthEventProcessor

/**
 * DTO class used by [CSHealthEventProcessor] for filling in events  related details in health.
 *
 * */
public data class CSEventForHealth(
    public val eventGuid: String,
    public val batchGuid: String,
)