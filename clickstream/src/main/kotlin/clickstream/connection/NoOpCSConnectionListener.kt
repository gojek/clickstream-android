package clickstream.connection

internal class NoOpCSConnectionListener : CSSocketConnectionListener {
    override fun onEventChanged(event: CSConnectionEvent) {
        /*No Op*/
    }
}
