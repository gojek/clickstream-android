package clickstream.fake

import clickstream.api.CSMetaProvider
import com.gojek.clickstream.internal.HealthMeta

internal class FakeCSMetaProvider : CSMetaProvider {
    override suspend fun location(): HealthMeta.Location {
        return HealthMeta.Location.newBuilder()
            .setLatitude(-6.1753924)
            .setLongitude(106.8249641)
            .build()
    }

    override val customer: HealthMeta.Customer
        get() = HealthMeta.Customer.newBuilder()
            .setCurrentCountry("ID")
            .setEmail("test@gmail.com")
            .setIdentity(12)
            .setSignedUpCountry("ID")
            .build()

    override val app: HealthMeta.App
        get() = HealthMeta.App.newBuilder()
            .setVersion("4.37.0")
            .build()

    override val device: HealthMeta.Device
        get() = HealthMeta.Device.newBuilder()
            .setDeviceMake("Samsung")
            .setDeviceModel("SM-900")
            .setOperatingSystem("Android")
            .setOperatingSystemVersion("10")
            .build()

    override val session: HealthMeta.Session
        get() = HealthMeta.Session.newBuilder()
            .setSessionId("12345678910")
            .build()
}