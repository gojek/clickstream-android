package clickstream.fake

import clickstream.ClickStream
import clickstream.model.CSEvent

public class FakeClickStream(
    private val clickStream: ClickStream
) : ClickStream by clickStream {

    private val _events: MutableList<CSEvent> = mutableListOf()
    public val event: List<CSEvent>
        get() = _events

    override fun trackEvent(event: CSEvent, expedited: Boolean) {
        _events.add(event)
    }
}
