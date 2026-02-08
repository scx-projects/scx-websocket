package dev.scx.websocket.frame;

import dev.scx.websocket.op_code.WebSocketOpCode;

import static dev.scx.websocket.frame.WebSocketFrameHelper.checkFrame;

/// WebSocketFrame
///
/// @author scx567888
/// @version 0.0.1
public final class WebSocketFrame {

    private final WebSocketOpCode opCode;
    private final byte[] payloadData;
    private final boolean fin;

    private WebSocketFrame(WebSocketOpCode opCode, byte[] payloadData, boolean fin) {
        this.opCode = opCode;
        this.payloadData = payloadData;
        this.fin = fin;
    }

    public static WebSocketFrame of(WebSocketOpCode opCode, byte[] payloadData, boolean fin) {
        checkFrame(opCode, payloadData, fin);
        return new WebSocketFrame(opCode, payloadData, fin);
    }

    public WebSocketOpCode opCode() {
        return opCode;
    }

    public byte[] payloadData() {
        return payloadData;
    }

    public boolean fin() {
        return fin;
    }

}
