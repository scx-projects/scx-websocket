package dev.scx.websocket.frame;

import dev.scx.websocket.op_code.WebSocketOpCode;

/// WebSocketFrame
///
/// @author scx567888
/// @version 0.0.1
public record WebSocketFrame(WebSocketOpCode opCode, byte[] payloadData, boolean fin) {

    public WebSocketFrame {
        if (opCode == null) {
            throw new NullPointerException("opCode must not be null");
        }
        if (payloadData == null) {
            throw new NullPointerException("payloadData must not be null");
        }
    }

}
