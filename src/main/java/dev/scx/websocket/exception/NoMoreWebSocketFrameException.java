package dev.scx.websocket.exception;

public final class NoMoreWebSocketFrameException extends Exception {

    public NoMoreWebSocketFrameException(String message) {
        super(message);
    }

    public NoMoreWebSocketFrameException(Throwable cause) {
        super(cause);
    }

    public NoMoreWebSocketFrameException(String message, Throwable cause) {
        super(message, cause);
    }

}
