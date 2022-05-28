package clickstream.internal.eventprocessor.impl

import clickstream.CSDeviceInfo
import clickstream.fake.fakeAppInfo
import clickstream.fake.fakeCSInfo
import clickstream.fake.fakeCSSessionInfo
import clickstream.fake.fakeLocationInfo
import clickstream.fake.fakeUserInfo
import clickstream.internal.eventprocessor.CSMetaProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
public class DefaultCSMetaProviderTest {

    private val testDeviceModel = "device_model"
    private val testDeviceMake = "device_make"
    private val testOS = "os"
    private val testSdkVersion = "sdk_version"
    private val deviceInfo = mock<CSDeviceInfo>()
    private lateinit var metaProvider: CSMetaProvider

    @Before
    public fun setup() {
        metaProvider = DefaultCSMetaProvider(fakeCSInfo(deviceInfo))
        whenever(deviceInfo.getDeviceModel()).thenReturn(testDeviceModel)
        whenever(deviceInfo.getDeviceManufacturer()).thenReturn(testDeviceMake)
        whenever(deviceInfo.getOperatingSystem()).thenReturn(testOS)
        whenever(deviceInfo.getSDKVersion()).thenReturn(testSdkVersion)
    }

    @After
    public fun tearDown() {
        verifyNoMoreInteractions(deviceInfo)
    }

    @Test
    public fun `Given an event When common attributes are fetched Then device info must be returned`() {
        val device = metaProvider.device
        verify(deviceInfo).getDeviceManufacturer()
        verify(deviceInfo).getDeviceModel()
        verify(deviceInfo).getOperatingSystem()
        verify(deviceInfo).getSDKVersion()

        assertEquals(device.deviceMake, testDeviceMake)
        assertEquals(device.deviceModel, testDeviceModel)
        assertEquals(device.operatingSystem, testOS)
        assertEquals(device.operatingSystemVersion, testSdkVersion)
    }

    @Test
    public fun `Given an event When common attributes are fetched Then customer info must be returned`() {
        val customer = metaProvider.customer
        assertEquals(customer.currentCountry, fakeUserInfo().currentCountry)
    }

    @Test
    public fun `Given an event When common attributes are fetched Then session info must be returned`() {
        val session = metaProvider.session
        assertEquals(session.sessionId, fakeCSSessionInfo.sessionID)
    }

    @Test
    public fun `Given an event When common attributes are fetched Then location info must be returned`() {
        runBlocking {
            val location = metaProvider.location()
            val currentLocation = fakeLocationInfo
            assertEquals(location.latitude, currentLocation.latitude, 0.0)
            assertEquals(location.longitude, currentLocation.longitude, 0.0)
        }
    }

    @Test
    public fun `Given an event When common attributes are fetched Then app info must be returned`() {
        val app = metaProvider.app
        assertEquals(app.version, fakeAppInfo.appVersion)
    }
}
