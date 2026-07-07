package dev.scx.websocket.exception;

/// WebSocketInvalidStateException.
///
/// 比如在发送 close 帧之后, 发送其他帧.
///
/// @author scx567888
public final class WebSocketInvalidStateException extends RuntimeException {

    public WebSocketInvalidStateException(String message) {
        super(message);
    }

}
