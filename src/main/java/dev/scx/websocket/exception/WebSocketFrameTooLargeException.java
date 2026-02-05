package dev.scx.websocket.exception;

/// 帧 过大异常.
public class WebSocketFrameTooLargeException extends WebSocketException {

    public WebSocketFrameTooLargeException(String message) {
        super(message);
    }

}
