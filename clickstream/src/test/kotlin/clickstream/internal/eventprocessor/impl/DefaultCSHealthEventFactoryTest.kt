package clickstream.internal.eventprocessor.impl

import clickstream.api.CSAppInfo
import clickstream.api.CSDeviceInfo
import clickstream.api.CSInfo
import clickstream.api.CSLocationInfo
import clickstream.api.CSSessionInfo
import clickstream.api.CSUserInfo
import clickstream.health.internal.CSGuIdGenerator
import clickstream.health.internal.factory.DefaultCSHealthEventFactory
import clickstream.health.time.CSHealthTimeStampGenerator
import clickstream.protoName
import com.gojek.clickstream.internal.Health
import com.gojek.clickstream.internal.HealthDetails
import com.gojek.clickstream.internal.HealthMeta
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
public class DefaultCSHealthEventFactoryTest {

    private val csGuIdGenerator = mock(CSGuIdGenerator::class.java)
    private val csTimeStampGenerator = mock(CSHealthTimeStampGenerator::class.java)
    private val csMetaProvider = mock(CSInfo::class.java)
    private lateinit var sut: DefaultCSHealthEventFactory

    @Before
    public fun setup() {
        sut = DefaultCSHealthEventFactory(
            csGuIdGenerator,
            csTimeStampGenerator,
            csInfo = csMetaProvider
        )
    }

    @Test
    public fun verifyCreate(): Unit = runBlocking {
        // Given
        val app = getHealthMetaApp()
        val location = getHealthMetaLocation()
        val customer = getHealthMetaCustomer()
        val session = getHealthMetaSession()
        val device = getHealthMetaDevice()

        whenever(csMetaProvider.appInfo).thenReturn(app)
        whenever(csMetaProvider.locationInfo).thenReturn(location)
        whenever(csMetaProvider.userInfo).thenReturn(customer)
        whenever(csMetaProvider.sessionInfo).thenReturn(session)
        whenever(csMetaProvider.deviceInfo).thenReturn(device)

        // When
        val health = getHealth().build()
        val event = sut.create(health)

        // Then
        assertTrue(event.protoName() == "Health")
        assertTrue(event.eventTimestamp.seconds == 0L)
        assertTrue(event.healthDetails.eventBatchGuidsList.size == 2)
        assertTrue(event.healthDetails.eventGuidsList.size == 2)
        assertTrue(event.healthMeta.app.version == "4.37.0")
        assertTrue(event.healthMeta.customer.currentCountry == "ID")
        assertTrue(event.healthMeta.customer.email == "test@gmail.com")
        assertTrue(event.healthMeta.customer.identity == 12)
        assertTrue(event.healthMeta.customer.signedUpCountry == "ID")
        assertTrue(event.healthMeta.device.deviceMake == "Samsung")
        assertTrue(event.healthMeta.device.deviceModel == "SM-900")
        assertTrue(event.healthMeta.device.operatingSystem == "Android")
        assertTrue(event.healthMeta.eventGuid == "123456")
        assertTrue(event.healthMeta.location.latitude == -6.1753924)
        assertTrue(event.healthMeta.location.longitude == 106.8249641)
        assertTrue(event.healthMeta.session.sessionId == "12345678910")
    }

    private fun getHealth() = Health.newBuilder()
        .setHealthDetails(
            HealthDetails.newBuilder()
                .addAllEventGuids(listOf("1", "2"))
                .addAllEventBatchGuids(listOf("3", "4"))
                .build()
        )
        .setHealthMeta(
            HealthMeta.newBuilder()
                .setEventGuid("123456")
                .build()
        )

    private fun getHealthMetaDevice() = object : CSDeviceInfo {
        override fun getDeviceManufacturer() = "Samsung"

        override fun getDeviceModel() = "SM-900"

        override fun getSDKVersion() = "30"

        override fun getOperatingSystem() = "Android"

        override fun getDeviceHeight() = "300"

        override fun getDeviceWidth() = "400"

    }

    private fun getHealthMetaSession() = CSSessionInfo("12345678910")

    private fun getHealthMetaCustomer() =
        CSUserInfo(
            currentCountry = "ID",
            email = "test@gmail.com",
            identity = 12,
            signedUpCountry = "ID"
        )

    private fun getHealthMetaLocation() =
        CSLocationInfo(latitude = -6.1753924, longitude = 106.8249641, mapOf())

    private fun getHealthMetaApp() = CSAppInfo(appVersion = "4.37.0")

}
