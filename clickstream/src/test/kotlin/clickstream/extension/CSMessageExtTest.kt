package clickstream.extension

import com.gojek.clickstream.products.common.Address
import com.gojek.clickstream.products.common.AppType
import com.gojek.clickstream.products.common.Outlet
import com.gojek.clickstream.products.telemetry.Protocol
import com.gojek.clickstream.products.telemetry.PubSubHealth
import com.gojek.clickstream.products.telemetry.QOS
import com.gojek.clickstream.products.telemetry.Topic
import clickstream.toFlatMap
import org.junit.Test

internal class CSMessageExtTest {

    @Test
    fun `Given proto with basic data types as field check if toFlatMap returns valid map`() {

        val address = Address.newBuilder().apply {
            id = "1"
            label = "home address"
            pillPosition = 5
            changedAddressSourceOnUi = true
            locationDetails = "location details"
        }.build()

        val addressMap = address.toFlatMap()

        assert(addressMap["id"] == "1")
        assert(addressMap["label"] == "home address")
        assert(addressMap["pillPosition"] == 5)
        assert(addressMap["changedAddressSourceOnUi"] == true)
        assert(addressMap["locationDetails"] == "location details")
    }

    @Test
    fun `Given proto with a nested proto as field check if toFlatMap returns valid map`() {

        val pubSubHealth = PubSubHealth.newBuilder().apply {
            val topic = Topic.newBuilder()
                .setTopic("test topic")
                .setQosValue(QOS.QOS_ZERO_VALUE)
                .build()

            setTopic(topic)
            protocol = Protocol.PROTOCOL_MQTT
            appType = AppType.Consumer
        }.build()

        pubSubHealth.toFlatMap().run {
            assert(get("topic.topic") == "test topic")
            assert(get("topic.qos") == QOS.QOS_ZERO_VALUE)
            assert(get("protocol") == Protocol.PROTOCOL_MQTT.ordinal)
            assert(get("appType") == AppType.Consumer.ordinal)
        }
    }

    @Test
    fun `Given proto with nested proto and list type as field check if toFlatMap returns valid map`() {
        val originalBadgeList = listOf("badge1", "badge2")
        val apiParameter = Outlet.newBuilder().apply {
            addAllBadges(originalBadgeList)
        }.build()

        apiParameter.toFlatMap().run {
            val badgesList = get("badges")
            assert(badgesList is List<*>)
            (badgesList as List<*>).forEachIndexed { index, badge ->
                assert(badge is String && badge == originalBadgeList[index])
            }
        }
    }
}
