package dev.scx.websocket;

/// CloseMessage
///
/// @author scx567888
public record CloseMessage(WebSocketCloseInfo closeInfo) implements WebSocketMessage {

    public CloseMessage {
        if (closeInfo == null) {
            throw new NullPointerException("closeInfo must not be null");
        }
    }

}
