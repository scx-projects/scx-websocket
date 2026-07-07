package dev.scx.websocket;

/// PongMessage
///
/// @author scx567888
public record PongMessage(byte[] data) implements WebSocketMessage {

    public PongMessage {
        if (data == null) {
            throw new NullPointerException("data must not be null");
        }
    }

}
