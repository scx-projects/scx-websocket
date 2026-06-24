package dev.scx.websocket;

/// WebSocketMessage
///
/// @author scx567888
/// @version 0.0.1
public sealed interface WebSocketMessage permits TextMessage, BinaryMessage, PingMessage, PongMessage, CloseMessage {

}
