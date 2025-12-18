package dev.scx.websocket;

import dev.scx.websocket.close_info.ScxWebSocketCloseInfo;

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

    public ScxWebSocketCloseInfo getCloseInfo() {
        return ScxWebSocketCloseInfo.ofPayload(payloadData);
    }

}
