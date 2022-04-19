package clickstream.fake

import clickstream.model.CSEvent
import clickstream.internal.utils.CSTimeStampMessageBuilder
import com.gojek.clickstream.common.Customer
import com.gojek.clickstream.common.Device
import com.gojek.clickstream.common.Location
import com.gojek.clickstream.common.Session
import com.gojek.clickstream.products.common.ServiceInfo
import com.gojek.clickstream.products.events.AdCardEvent
import com.gojek.clickstream.products.events.AdCardType
import com.gojek.clickstream.products.shuffle.ShuffleCard
import java.util.UUID

/**
 * Generates a ClickStreamEventWrapper data
 * with default data every time invoked.
 */
public fun defaultEventWrapperData(): CSEvent {
    val event = AdCardEvent.newBuilder().apply {
        meta = meta.toBuilder().apply {
            val objectID = UUID.randomUUID().toString()
            eventGuid = objectID
            eventTimestamp = CSTimeStampMessageBuilder.build(System.currentTimeMillis())

            location = Location.getDefaultInstance()
            device = Device.getDefaultInstance()
            customer = Customer.getDefaultInstance()
            session = Session.getDefaultInstance()
        }.build()
        type = AdCardType.Clicked
        shuffleCard = ShuffleCard.getDefaultInstance()
        serviceInfo = ServiceInfo.getDefaultInstance()
    }.build()

    return CSEvent(
        guid = event.meta.eventGuid,
        timestamp = event.eventTimestamp,
        message = event
    )
}
