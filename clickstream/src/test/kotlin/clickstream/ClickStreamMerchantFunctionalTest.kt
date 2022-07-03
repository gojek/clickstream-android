package clickstream

import android.app.Application
import android.os.Build.VERSION_CODES
import androidx.test.core.app.ApplicationProvider
import clickstream.api.CSAppInfo
import clickstream.api.CSInfo
import clickstream.api.CSLocationInfo
import clickstream.api.CSSessionInfo
import clickstream.config.CSConfiguration
import clickstream.extension.protoName
import clickstream.fake.FakeCSAppLifeCycle
import clickstream.fake.FakeHealthGateway
import clickstream.fake.createCSConfig
import clickstream.fake.fakeUserInfo
import clickstream.internal.DefaultCSDeviceInfo
import clickstream.model.CSEvent
import clickstream.utils.CoroutineTestRule
import com.gojek.clickstream.common.App
import com.gojek.clickstream.common.EventMeta
import com.gojek.clickstream.common.Merchant
import com.gojek.clickstream.common.MerchantUser
import com.gojek.clickstream.common.MerchantUserRole
import com.gojek.clickstream.products.events.AdCardEvent
import com.google.protobuf.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.OkHttpClient.Builder
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [VERSION_CODES.P])
@Ignore
public class ClickStreamMerchantFunctionalTest {
    @get:Rule
    public val coroutineRule: CoroutineTestRule = CoroutineTestRule()

    private val fakeHealthGateway = FakeHealthGateway(mock(), mock(), mock(), mock())
    private val app = ApplicationProvider.getApplicationContext<Application>()

    @Test
    public fun `Given EventMeta When Merchant property is filled Then final generated EventMeta should have Merchant metadata`() {
        // Given
        val csInfo = createCSInfo()
        val appLifecycle = FakeCSAppLifeCycle()
        ClickStream.initialize(
            CSConfiguration.Builder(
                context = app,
                info = csInfo,
                config = createCSConfig(),
                appLifeCycle = appLifecycle
            )
            .setDispatcher(coroutineRule.testDispatcher)
            .setHealthGateway(fakeHealthGateway)
            .build()
        )
        val sut = ClickStream.getInstance()

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

    private fun generateCSMerchantEvent(guid: String): CSEvent {
        return CSEvent(
            guid = guid,
            timestamp = Timestamp.getDefaultInstance(),
            message = AdCardEvent.newBuilder()
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
        )
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
}
