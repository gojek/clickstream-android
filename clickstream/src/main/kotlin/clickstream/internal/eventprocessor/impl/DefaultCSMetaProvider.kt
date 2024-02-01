package clickstream.internal.eventprocessor.impl

import clickstream.api.CSInfo
import clickstream.api.CSLocationInfo
import clickstream.api.CSMetaProvider
import clickstream.health.proto.HealthMeta

/**
 * This is the implementation of [CSMetaProvider].
 *
 * @param info contains data for location, device, customer, session
 */
public class DefaultCSMetaProvider(
    private val info: CSInfo
) : CSMetaProvider {

    override suspend fun location(): HealthMeta.Location =
        with(info.locationInfo) {
            val currentLocation = CSLocationInfo.Location(latitude, longitude)
            return@with HealthMeta.Location.newBuilder().apply {
                latitude = currentLocation.latitude
                longitude = currentLocation.longitude
            }.build()
        }

    override val app: HealthMeta.App by lazy {
        HealthMeta.App.newBuilder().apply {
            version = info.appInfo.appVersion
        }.build()
    }

    override val customer: HealthMeta.Customer by lazy {
        with(info.userInfo) {
            HealthMeta.Customer.newBuilder()
                .setSignedUpCountry(signedUpCountry)
                .setCurrentCountry(currentCountry)
                .setIdentity(identity)
                .setEmail(email)
                .build()
        }
    }

    override val device: HealthMeta.Device by lazy {
        with(info.deviceInfo) {
            HealthMeta.Device.newBuilder().apply {
                deviceModel = this@with.getDeviceModel()
                deviceMake = this@with.getDeviceManufacturer()
                operatingSystem = this@with.getOperatingSystem()
                operatingSystemVersion = this@with.getSDKVersion()
            }.build()
        }
    }

    override val session: HealthMeta.Session by lazy {
        HealthMeta.Session.newBuilder().apply {
            sessionId = info.sessionInfo.sessionID
        }.build()
    }
}