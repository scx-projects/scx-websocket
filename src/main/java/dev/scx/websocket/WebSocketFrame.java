package dev.scx.websocket;

/// WebSocketFrame
///
/// @author scx567888
/// @version 0.0.1
public record WebSocketFrame(WebSocketOpCode opCode, byte[] payloadData, boolean fin) {

    public static WebSocketFrame of(WebSocketOpCode opCode, byte[] payloadData, boolean fin) {
        return new WebSocketFrame(opCode, payloadData, fin);
    }

    public static WebSocketFrame of(WebSocketOpCode opCode, byte[] payloadData) {
        return new WebSocketFrame(opCode, payloadData, true);
    }

}
