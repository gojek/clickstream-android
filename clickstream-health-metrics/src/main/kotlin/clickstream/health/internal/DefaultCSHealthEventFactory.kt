package clickstream.health.internal

import androidx.annotation.RestrictTo
import clickstream.api.CSMetaProvider
import clickstream.health.time.CSTimeStampGenerator
import clickstream.health.identity.CSGuIdGenerator
import clickstream.health.intermediate.CSHealthEventFactory
import clickstream.health.proto.Health
import clickstream.health.proto.HealthMeta

/**
 * This is the implementation of [CSHealthEventFactory]
 *
 * @param guIdGenerator used for generating random guid
 * @param timeStampGenerator used for generating a time stamp
 * @param metaProvider used for getting meta data
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class DefaultCSHealthEventFactory(
    private val guIdGenerator: CSGuIdGenerator,
    private val timeStampGenerator: CSTimeStampGenerator,
    private val metaProvider: CSMetaProvider
) : CSHealthEventFactory {

    override suspend fun create(message: Health): Health {
        val builder = message.toBuilder()
        updateMeta(builder)
        updateTimeStamp(builder)
        return builder.build()
    }

    @Throws(Exception::class)
    private suspend fun updateMeta(message: Health.Builder) {
        val guid = message.healthMeta.eventGuid
        message.healthMeta = HealthMeta.newBuilder().apply {
            eventGuid = if (guid.isNullOrBlank()) guIdGenerator.getId() else guid
            location = metaProvider.location()
            customer = metaProvider.customer
            session = metaProvider.session
            device = metaProvider.device
            app = metaProvider.app
        }.build()
    }

    @Throws(Exception::class)
    private fun updateTimeStamp(message: Health.Builder) {
        // eventTimestamp uses NTP time
        message.eventTimestamp = CSTimeStampMessageBuilder.build(timeStampGenerator.getTimeStamp())

        // deviceTimestamp uses system clock
        message.deviceTimestamp = CSTimeStampMessageBuilder.build(System.currentTimeMillis())
    }
}
