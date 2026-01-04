package dev.scx.websocket.exception;

public final class WebsocketAlreadySentCloseException extends Exception {

    public WebsocketAlreadySentCloseException(String message) {
        super(message);
    }

    public WebsocketAlreadySentCloseException(Throwable cause) {
        super(cause);
    }

    public WebsocketAlreadySentCloseException(String message, Throwable cause) {
        super(message, cause);
    }

}
