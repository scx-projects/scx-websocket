package dev.scx.websocket;

/// TextMessage
///
/// @author scx567888
public record TextMessage(String text) implements WebSocketMessage {

    public TextMessage {
        if (text == null) {
            throw new NullPointerException("text must not be null");
        }
    }

}
