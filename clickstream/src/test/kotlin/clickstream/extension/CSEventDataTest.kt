package clickstream.extension

import clickstream.fake.defaultEventWrapperData
import clickstream.internal.eventscheduler.CSEventData
import org.junit.Test

internal class CSEventDataTest {

    @Test
    fun `Given event prefix check if event function returns Event with correct type`() {

        val csEventData = CSEventData.create(defaultEventWrapperData())
        val eventWithoutPrefix = csEventData.first.event()
        val eventWithPrefix = csEventData.first.event("gobiz")

        assert(eventWithoutPrefix.type == "adcardevent")
        assert(eventWithPrefix.type == "gobiz-adcardevent")
    }
}