package clickstream.fake

import clickstream.internal.utils.CSTimeStampMessageBuilder
import clickstream.model.CSEvent
import clickstream.proto.App
import clickstream.proto.Device
import clickstream.proto.User
import java.util.UUID

/**
 * Generates a ClickStreamEventWrapper data
 * with default data every time invoked.
 */
public fun defaultEventWrapperData(): CSEvent {
    val event = User.newBuilder().apply {
        guid = "Some Guid"
        name = "John Doe"
        age = 35
        gender = "male"
        phoneNumber = 1234567890
        email = "john.doe@example.com"
        app = App.newBuilder().apply {
            version = "0.0.1"
            packageName = "com.clickstream"
        }.build()
        device = Device.newBuilder().apply {
            operatingSystem = "android"
            operatingSystemVersion = "29"
            deviceMake = "Samsung"
            deviceModel = "SM2028"
        }.build()
    }.build()

    return CSEvent(
        guid = UUID.randomUUID().toString(),
        timestamp = CSTimeStampMessageBuilder.build(System.currentTimeMillis()),
        message = event
    )
}
