package dev.scx.websocket.exception;

import dev.scx.websocket.close_info.ScxWebSocketCloseInfo;

/// 协议异常. 比如 帧过大. 掩码错误等.
///
/// @author scx567888
/// @version 0.0.1
public class WebSocketProtocolException extends RuntimeException {

    private final ScxWebSocketCloseInfo closeInfo;

    public WebSocketProtocolException(ScxWebSocketCloseInfo closeInfo) {
        this.closeInfo = closeInfo;
    }

    public ScxWebSocketCloseInfo closeInfo() {
        return closeInfo;
    }

}
