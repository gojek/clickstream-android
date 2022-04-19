package clickstream.connection

/**
 * A non-blocking interface to a WebSocket.
 */
public sealed class CSConnectionEvent {
    /**
     * Invoked when a WebSocket has been accepted by the remote peer and may begin transmitting messages.
     */
    public object OnConnectionConnected : CSConnectionEvent()

    /**
     * Invoked when a [text message][CSMessage.Text] or [binary message][CSMessage.Bytes] has been received.
     *
     * @property message The raw message.
     */
    public data class OnMessageReceived(val message: CSMessage) : CSConnectionEvent()

    /**
     * Invoked when the connection starting.
     */
    public object OnConnectionConnecting : CSConnectionEvent()

    /**
     * Invoked when the peer has indicated that no more incoming messages will be transmitted.
     *
     * @property shutdownReason Reason to shutdown from the peer.
     */
    public data class OnConnectionClosing(val shutdownReason: CSShutdownReason) : CSConnectionEvent()

    /**
     * Invoked when both peers have indicated that no more messages will be transmitted and the connection has been
     * successfully released. No further calls to this listener will be made.
     *
     * @property shutdownReason Reason to shutdown from the peer.
     */
    public data class OnConnectionClosed(val shutdownReason: CSShutdownReason) : CSConnectionEvent()

    /**
     * Invoked when a web socket has been closed due to an error reading from or writing to the network. Both outgoing
     * and incoming messages may have been lost. No further calls to this listener will be made.
     *
     * @property throwable The error causing the failure.
     */
    public data class OnConnectionFailed(val throwable: Throwable) : CSConnectionEvent()
}