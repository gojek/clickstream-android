package clickstream.internal.eventprocessor.impl

import clickstream.fake.fakeCSInfo
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
public class DefaultCSMetaProviderMerchantTest {

    @Test
    public fun `Should return a valid Customer property When CSCustomerInfo is set`() {
        val sut = DefaultCSMetaProvider(fakeCSInfo())
        with(sut.customer) {
            assertTrue(signedUpCountry == "ID")
            assertTrue(currentCountry == "ID")
            assertTrue(identity == 12345)
            assertTrue(email == "test@gmail.com")
        }
    }
}
