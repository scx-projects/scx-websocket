package dev.scx.websocket;

/// PingMessage
///
/// @author scx567888
public record PingMessage(byte[] data) implements WebSocketMessage {

    public PingMessage {
        if (data == null) {
            throw new NullPointerException("data must not be null");
        }
    }

}
