package clickstream

import android.app.Application
import android.os.Build.VERSION_CODES
import androidx.test.core.app.ApplicationProvider
import clickstream.api.CSAppInfo
import clickstream.api.CSInfo
import clickstream.api.CSLocationInfo
import clickstream.api.CSSessionInfo
import clickstream.config.CSConfig
import clickstream.config.CSConfiguration
import clickstream.config.CSEventProcessorConfig
import clickstream.config.CSEventSchedulerConfig
import clickstream.config.CSNetworkConfig
import clickstream.fake.fakeUserInfo
import clickstream.internal.DefaultCSDeviceInfo
import clickstream.utils.CoroutineTestRule
import com.gojek.clickstream.common.App
import com.gojek.clickstream.common.Customer
import com.gojek.clickstream.common.EventMeta
import com.gojek.clickstream.common.Merchant
import com.gojek.clickstream.common.MerchantUser
import com.gojek.clickstream.common.MerchantUserRole
import com.gojek.clickstream.products.events.AdCardEvent
import com.google.protobuf.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [VERSION_CODES.P])
public class ClickStreamFunctionalTest {
    @get:Rule
    public val coroutineRule: CoroutineTestRule = CoroutineTestRule()

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var sut: ClickStream

    @Before
    public fun setup() {
        ClickStream.release()
    }

    @Test
    public fun `Given EventMeta When Customer property is filled Then final generated EventMeta should have Customer metadata`() {
        // Given
        val userInfo = fakeUserInfo()
        val csInfo = createCSInfo().copy(userInfo = userInfo)
        ClickStream.initialize(
            CSConfiguration.Builder(
                context = app,
                info = csInfo,
                config = createCSConfig()
            ).setDispatcher(coroutineRule.testDispatcher).build()
        )

        sut = ClickStream.getInstance()

        // When
        val event = generateCSCustomerEvent("12")
        sut.trackEvent(event, true)

        // Then
        assertTrue(event.message.protoName() == "AdCardEvent")
        assertTrue((event.message as AdCardEvent).meta.customer.email == userInfo.email)
        assertTrue((event.message as AdCardEvent).meta.customer.currentCountry == userInfo.currentCountry)
        assertTrue((event.message as AdCardEvent).meta.customer.signedUpCountry == userInfo.signedUpCountry)
        assertTrue((event.message as AdCardEvent).meta.customer.identity == userInfo.identity)
        assertTrue((event.message as AdCardEvent).meta.hasMerchant().not())
    }

    @Test
    public fun `Given EventMeta for BytesEvent When Customer property is filled Then final generated EventMeta should have Customer metadata`() {
        // Given
        val userInfo = fakeUserInfo()
        val csInfo = createCSInfo().copy(userInfo = userInfo)
        ClickStream.initialize(
            CSConfiguration.Builder(
                context = app,
                info = csInfo,
                config = createCSConfig()
            ).setDispatcher(coroutineRule.testDispatcher).build()
        )

        sut = ClickStream.getInstance()

        // When
        val adCardEvent = buildAdCardEventForCustomer()
        val bytesEvent = CSBytesEvent(
            "",
            Timestamp.getDefaultInstance(),
            adCardEvent.protoName(),
            adCardEvent.toByteArray()
        )

        sut.trackEvent(bytesEvent, true)

        val event = AdCardEvent.newBuilder().mergeFrom(bytesEvent.eventData).build()
        // Then
        assertTrue(event.protoName() == "AdCardEvent")
        assertTrue((event as AdCardEvent).meta.customer.email == userInfo.email)
        assertTrue(event.meta.customer.currentCountry == userInfo.currentCountry)
        assertTrue(event.meta.customer.signedUpCountry == userInfo.signedUpCountry)
        assertTrue(event.meta.customer.identity == userInfo.identity)
        assertTrue(event.meta.hasMerchant().not())
    }

    @Test
    public fun `Given EventMeta When Merchant property is filled Then final generated EventMeta should have Merchant metadata`() {
        // Given
        val csInfo = createCSInfo()
        ClickStream.initialize(
            CSConfiguration.Builder(
                context = app,
                info = csInfo,
                config = createCSConfig()
            ).setDispatcher(coroutineRule.testDispatcher).build()
        )
        sut = ClickStream.getInstance()

        // When
        val event = generateCSMerchantEvent("12")
        sut.trackEvent(event, true)

        // Then
        assertTrue(event.message.protoName() == "AdCardEvent")
        assertTrue((event.message as AdCardEvent).meta.hasMerchant())
        assertTrue((event.message as AdCardEvent).meta.merchant.saudagarId == "1")
        assertTrue((event.message as AdCardEvent).meta.merchant.user.role == MerchantUserRole.MERCHANT_USER_ROLE_ADMIN)
        assertTrue((event.message as AdCardEvent).meta.merchant.user.signedUpCountry == "ID")
        assertTrue((event.message as AdCardEvent).meta.merchant.user.phone == "085")
        assertTrue((event.message as AdCardEvent).meta.merchant.user.identity == 12)
        assertTrue((event.message as AdCardEvent).meta.merchant.user.email == "test@gmail.com")
        assertTrue((event.message as AdCardEvent).meta.hasCustomer().not())
    }

    @Test
    public fun `Given EventMeta for BytesEvent When Merchant property is filled Then final generated EventMeta should have Merchant metadata`() {
        // Given
        val csInfo = createCSInfo()
        ClickStream.initialize(
            CSConfiguration.Builder(
                context = app,
                info = csInfo,
                config = createCSConfig()
            ).setDispatcher(coroutineRule.testDispatcher).build()
        )
        sut = ClickStream.getInstance()

        // When
        val adCardEvent = buildAdCardEventForMerchant()
        val bytesEvent = CSBytesEvent(
            "",
            Timestamp.getDefaultInstance(),
            adCardEvent.protoName(),
            adCardEvent.toByteArray()
        )
        sut.trackEvent(bytesEvent, true)

        val event = AdCardEvent.newBuilder().mergeFrom(bytesEvent.eventData).build()
        // Then
        assertTrue(event.protoName() == "AdCardEvent")
        assertTrue((event as AdCardEvent).meta.hasMerchant())
        assertTrue(event.meta.merchant.saudagarId == "1")
        assertTrue(event.meta.merchant.user.role == MerchantUserRole.MERCHANT_USER_ROLE_ADMIN)
        assertTrue(event.meta.merchant.user.signedUpCountry == "ID")
        assertTrue(event.meta.merchant.user.phone == "085")
        assertTrue(event.meta.merchant.user.identity == 12)
        assertTrue(event.meta.merchant.user.email == "test@gmail.com")
        assertTrue(event.meta.hasCustomer().not())
    }

    private fun generateCSMerchantEvent(guid: String): CSEvent {
        return CSEvent(
            guid = guid,
            timestamp = Timestamp.getDefaultInstance(),
            message = buildAdCardEventForMerchant()
        )
    }

    private fun buildAdCardEventForMerchant() = AdCardEvent.newBuilder()
        .setMeta(
            EventMeta.newBuilder()
                .setApp(App.newBuilder().setVersion("4.35.0"))
                .setMerchant(
                    Merchant.newBuilder()
                        .setSaudagarId("1")
                        .setUser(
                            MerchantUser.newBuilder()
                                .setRole(MerchantUserRole.MERCHANT_USER_ROLE_ADMIN)
                                .setSignedUpCountry("ID")
                                .setPhone("085")
                                .setIdentity(12)
                                .setEmail("test@gmail.com")
                                .build()
                        )
                        .build()
                )
                .build()
        )
        .build()

    private fun generateCSCustomerEvent(guid: String): CSEvent {
        return CSEvent(
            guid = guid,
            timestamp = Timestamp.getDefaultInstance(),
            message = buildAdCardEventForCustomer()
        )
    }

    private fun buildAdCardEventForCustomer(): AdCardEvent {
        val userInfo = fakeUserInfo()
        return AdCardEvent.newBuilder()
            .setMeta(
                EventMeta.newBuilder()
                    .setApp(App.newBuilder().setVersion("4.35.0"))
                    .setCustomer(
                        Customer.newBuilder()
                            .setCurrentCountry(userInfo.currentCountry)
                            .setEmail(userInfo.email)
                            .setIdentity(userInfo.identity)
                            .setSignedUpCountry(userInfo.signedUpCountry)
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun createCSInfo(): CSInfo {
        return CSInfo(
            appInfo = CSAppInfo(appVersion = "4.37.0"),
            locationInfo = CSLocationInfo(
                latitude = -6.1753924,
                longitude = 106.8249641,
                s2Ids = emptyMap()
            ), sessionInfo = CSSessionInfo(sessionID = "1234"),
            deviceInfo = DefaultCSDeviceInfo(), fakeUserInfo()
        )
    }

    private fun createCSConfig(): CSConfig {
        return CSConfig(
            eventProcessorConfiguration = CSEventProcessorConfig(
                realtimeEvents = emptyList(),
                instantEvent = listOf("AdCardEvent")
            ),
            eventSchedulerConfig = CSEventSchedulerConfig.default(),
            networkConfig = CSNetworkConfig.default("", mapOf()),
        )
    }
}
