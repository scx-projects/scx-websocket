package dev.scx.websocket.exception;

/// 协议异常. 比如 帧过大. 掩码错误等.
///
/// @author scx567888
/// @version 0.0.1
public class WebSocketProtocolException extends WebSocketException {

    public WebSocketProtocolException(String message) {
        super(message);
    }

}
