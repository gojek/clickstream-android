package clickstream.health.internal.factory

import androidx.annotation.RestrictTo
import clickstream.api.CSInfo
import clickstream.health.internal.CSGuIdGenerator
import clickstream.health.internal.time.CSTimeStampMessageBuilder
import clickstream.health.time.CSHealthTimeStampGenerator
import com.gojek.clickstream.internal.Health
import com.gojek.clickstream.internal.HealthMeta

/**
 * This is the implementation of [CSHealthEventFactory]
 *
 * @param guIdGenerator used for generating random guid
 * @param timeStampGenerator used for generating a time stamp
 * @param csInfo used for meta info.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class DefaultCSHealthEventFactory(
    private val guIdGenerator: CSGuIdGenerator,
    private val timeStampGenerator: CSHealthTimeStampGenerator,
    private val csInfo: CSInfo
) : CSHealthEventFactory {

    override suspend fun create(message: Health): Health {
        val builder = message.toBuilder()
        updateMeta(builder)
        updateTimeStamp(builder)
        return builder.build()
    }

    @Throws(Exception::class)
    private fun updateMeta(message: Health.Builder) {
        val guid = message.healthMeta.eventGuid
        message.healthMeta = HealthMeta.newBuilder().apply {
            eventGuid = if (guid.isNullOrBlank()) guIdGenerator.getId() else guid
            location = location()
            customer = customer()
            session = session()
            device = device()
            app = app()
        }.build()
    }

    private fun location() = HealthMeta.Location.newBuilder().apply {
        latitude = csInfo.locationInfo.latitude
        longitude = csInfo.locationInfo.longitude
    }.build()

    private fun customer() = HealthMeta.Customer.newBuilder().apply {
        signedUpCountry = csInfo.userInfo.signedUpCountry
        currentCountry = csInfo.userInfo.currentCountry
        email = csInfo.userInfo.email
        identity = csInfo.userInfo.identity
    }.build()

    private fun session() = HealthMeta.Session.newBuilder().apply {
        sessionId = csInfo.sessionInfo.sessionID
    }.build()

    private fun device() = HealthMeta.Device.newBuilder().apply {
        operatingSystem = csInfo.deviceInfo.getOperatingSystem()
        operatingSystemVersion = csInfo.deviceInfo.getOperatingSystem()
        deviceMake = csInfo.deviceInfo.getDeviceManufacturer()
        deviceModel = csInfo.deviceInfo.getDeviceModel()

    }.build()

    private fun app() = HealthMeta.App.newBuilder().apply {
        version = csInfo.appInfo.appVersion
    }.build()

    @Throws(Exception::class)
    private fun updateTimeStamp(message: Health.Builder) {
        // eventTimestamp uses NTP time
        message.eventTimestamp = CSTimeStampMessageBuilder.build(timeStampGenerator.getTimeStamp())

        // deviceTimestamp uses system clock
        message.deviceTimestamp = CSTimeStampMessageBuilder.build(System.currentTimeMillis())
    }
}
