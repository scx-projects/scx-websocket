package dev.scx.websocket.exception;

public class WebSocketAlreadyClosedException extends WebSocketException {

    public WebSocketAlreadyClosedException(String message) {
        super(message);
    }

}
