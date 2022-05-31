package clickstream.internal.eventprocessor.impl

import clickstream.api.CSMetaProvider
import clickstream.extension.protoName
import clickstream.health.CSTimeStampGenerator
import clickstream.health.identity.CSGuIdGenerator
import clickstream.health.internal.DefaultCSHealthEventFactory
import com.gojek.clickstream.internal.Health
import com.gojek.clickstream.internal.HealthDetails
import com.gojek.clickstream.internal.HealthMeta
import com.gojek.clickstream.internal.HealthMeta.App
import com.gojek.clickstream.internal.HealthMeta.Customer
import com.gojek.clickstream.internal.HealthMeta.Device
import com.gojek.clickstream.internal.HealthMeta.Location
import com.gojek.clickstream.internal.HealthMeta.Session
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
    private val csTimeStampGenerator = mock(CSTimeStampGenerator::class.java)
    private val csMetaProvider = mock(CSMetaProvider::class.java)
    private lateinit var sut: DefaultCSHealthEventFactory

    @Before
    public fun setup() {
        sut = DefaultCSHealthEventFactory(csGuIdGenerator, csTimeStampGenerator, csMetaProvider)
    }

    @Test
    public fun verifyCreate(): Unit = runBlocking {
        // Given
        val app = getHealthMetaApp()
        val location = getHealthMetaLocation()
        val customer = getHealthMetaCustomer()
        val session = getHealthMetaSession()
        val device = getHealthMetaDevice()

        whenever(csMetaProvider.app).thenReturn(app)
        whenever(csMetaProvider.location()).thenReturn(location)
        whenever(csMetaProvider.customer).thenReturn(customer)
        whenever(csMetaProvider.session).thenReturn(session)
        whenever(csMetaProvider.device).thenReturn(device)

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
        assertTrue(event.healthMeta.device.operatingSystemVersion == "10")
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

    private fun getHealthMetaDevice() = Device.newBuilder()
        .setDeviceMake("Samsung")
        .setDeviceModel("SM-900")
        .setOperatingSystem("Android")
        .setOperatingSystemVersion("10")
        .build()

    private fun getHealthMetaSession() = Session.newBuilder()
        .setSessionId("12345678910")
        .build()

    private fun getHealthMetaCustomer() = Customer.newBuilder()
        .setCurrentCountry("ID")
        .setEmail("test@gmail.com")
        .setIdentity(12)
        .setSignedUpCountry("ID")
        .build()

    private fun getHealthMetaLocation() = Location.newBuilder()
        .setLatitude(-6.1753924)
        .setLongitude(106.8249641)
        .build()

    private fun getHealthMetaApp() = App.newBuilder()
        .setVersion("4.37.0")
        .build()
}
