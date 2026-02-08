package dev.scx.websocket.message;

import dev.scx.websocket.op_code.WebSocketOpCode;

public class WebSocketMessage {

    private final WebSocketOpCode opCode;
    private final byte[] payloadData;

    public WebSocketMessage(WebSocketOpCode opCode, byte[] payloadData) {
        this.opCode = opCode;
        this.payloadData = payloadData;
    }

}
