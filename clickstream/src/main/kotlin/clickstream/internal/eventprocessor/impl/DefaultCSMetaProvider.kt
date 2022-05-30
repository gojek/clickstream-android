package clickstream.internal.eventprocessor.impl

import clickstream.CSMetaProvider
import clickstream.health.CSInfo
import clickstream.health.CSLocationInfo
import com.gojek.clickstream.internal.HealthMeta.App
import com.gojek.clickstream.internal.HealthMeta.Customer
import com.gojek.clickstream.internal.HealthMeta.Device
import com.gojek.clickstream.internal.HealthMeta.Location
import com.gojek.clickstream.internal.HealthMeta.Session

/**
 * This is the implementation of [CSMetaProvider].
 *
 * @param info contains data for location, device, customer, session
 */
internal class DefaultCSMetaProvider(
    private val info: CSInfo
) : CSMetaProvider {

    override suspend fun location(): Location =
        with(info.locationInfo) {
            val currentLocation = CSLocationInfo.Location(latitude, longitude)
            return@with Location.newBuilder().apply {
                latitude = currentLocation.latitude
                longitude = currentLocation.longitude
            }.build()
        }

    override val app: App by lazy {
        App.newBuilder().apply {
            version = info.appInfo.appVersion
        }.build()
    }

    override val customer: Customer by lazy {
        with(info.userInfo) {
            Customer.newBuilder()
                .setSignedUpCountry(signedUpCountry)
                .setCurrentCountry(currentCountry)
                .setIdentity(identity)
                .setEmail(email)
                .build()
        }
    }

    override val device: Device by lazy {
        with(info.deviceInfo) {
            Device.newBuilder().apply {
                deviceModel = this@with.getDeviceModel()
                deviceMake = this@with.getDeviceManufacturer()
                operatingSystem = this@with.getOperatingSystem()
                operatingSystemVersion = this@with.getSDKVersion()
            }.build()
        }
    }

    override val session: Session by lazy {
        Session.newBuilder().apply {
            sessionId = info.sessionInfo.sessionID
        }.build()
    }
}