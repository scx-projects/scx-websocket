package dev.scx.websocket;

/// BinaryMessage
///
/// @author scx567888
/// @version 0.0.1
public record BinaryMessage(byte[] binary) implements WebSocketMessage {

    public BinaryMessage {
        if (binary == null) {
            throw new NullPointerException("binary must not be null");
        }
    }

}
