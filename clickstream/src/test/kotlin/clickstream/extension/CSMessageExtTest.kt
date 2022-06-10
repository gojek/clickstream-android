package clickstream.extension

import com.gojek.clickstream.products.common.*
import com.gojek.clickstream.products.telemetry.Protocol
import com.gojek.clickstream.products.telemetry.PubSubHealth
import com.gojek.clickstream.products.telemetry.QOS
import com.gojek.clickstream.products.telemetry.Topic
import org.json.JSONObject
import org.junit.Test

internal class CSMessageExtTest {

    @Test
    fun `Given proto with basic data types as field check if toJson returns valid json`() {

        val address = Address.newBuilder().apply {
            id = "1"
            label = "home address"
            pillPosition = 5
            changedAddressSourceOnUi = true
            locationDetails = "location details"
        }.build()

        val addressJson = address.toJson()

        assert(addressJson["Id"] == "1")
        assert(addressJson["Label"] == "home address")
        assert(addressJson["PillPosition"] == 5)
        assert(addressJson["ChangedAddressSourceOnUi"] == true)
        assert(addressJson["LocationDetails"] == "location details")

    }

    @Test
    fun `Given proto with a nested proto as field check if toJson returns valid json`() {

        val pubSubHealth = PubSubHealth.newBuilder().apply {
            val topic = Topic.newBuilder()
                .setTopic("test topic")
                .setQosValue(QOS.QOS_ZERO_VALUE)
                .build()

            setTopic(topic)
            protocol = Protocol.PROTOCOL_MQTT
            appType = AppType.Consumer

        }.build()

        pubSubHealth.toJson().run {
            val topic = getJSONObject("Topic")
            assert(topic is JSONObject)
            topic.run {
                assert(get("QosValue") == QOS.QOS_ZERO_VALUE)
                assert(get("Topic") == "test topic")
            }
            assert(get("Protocol") == Protocol.PROTOCOL_MQTT)
            assert(get("AppType") == AppType.Consumer)
        }
    }

    @Test
    fun `Given proto with nested proto and list type as field check if toJson returns valid json`() {
        val originalBadgeList = listOf("badge1", "badge2")
        val apiParameter = Outlet.newBuilder().apply {
            addAllBadges(originalBadgeList)
        }.build()

        apiParameter.toJson().run {
            val badgesListJSON = get("BadgesList")
            assert(badgesListJSON is List<*>)
            (badgesListJSON as List<*>).forEachIndexed { index, badge ->
                assert(badge is String && badge == originalBadgeList[index])
            }
        }
    }
}