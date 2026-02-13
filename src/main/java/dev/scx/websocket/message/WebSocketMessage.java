package dev.scx.websocket.message;

/// WebSocketMessage
///
/// @author scx567888
/// @version 0.0.1
public record WebSocketMessage(WebSocketMessageType type, byte[] payloadData) {

    public WebSocketMessage {
        if (type == null) {
            throw new NullPointerException("type must not be null");
        }
        if (payloadData == null) {
            throw new NullPointerException("payloadData must not be null");
        }
    }

}
