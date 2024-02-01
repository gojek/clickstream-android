package clickstream.extension

import clickstream.proto.App
import clickstream.proto.User
import org.junit.Test

internal class CSMessageExtTest {

    @Test
    fun `Given proto with basic data types as field check if toFlatMap returns valid map`() {

        val user = User.newBuilder().apply {
            guid = "Some Guid"
            name = "John Doe"
            age = 35
            gender = "male"
            phoneNumber = 1234567890
            email = "john.doe@example.com"
        }.build()

        val userMap = user.toFlatMap()

        assert(userMap["guid"] == "Some Guid")
        assert(userMap["name"] == "John Doe")
        assert(userMap["age"] == 35)
        assert(userMap["gender"] == "male")
        assert(userMap["phoneNumber"] == 1234567890.toLong())
        assert(userMap["email"] == "john.doe@example.com")

    }

    @Test
    fun `Given proto with a nested proto as field check if toFlatMap returns valid map`() {

        val pubSubHealth = User.newBuilder().apply {
            guid = "Some Guid"
            name = "John Doe"
            val appProto = App.newBuilder().apply {
                version = "0.0.1"
                packageName = "com.clickstream"
            }.build()

            app = appProto


        }.build()

        pubSubHealth.toFlatMap().run {
            assert(get("app.version") == "0.0.1")
            assert(get("app.packageName") == "com.clickstream")
            assert(get("guid") == "Some Guid")
            assert(get("name") == "John Doe")
        }
    }
}