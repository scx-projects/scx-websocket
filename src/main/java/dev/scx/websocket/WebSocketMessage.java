package dev.scx.websocket;

/// WebSocketMessage
///
/// @author scx567888
public sealed interface WebSocketMessage permits TextMessage, BinaryMessage, PingMessage, PongMessage, CloseMessage {

}
