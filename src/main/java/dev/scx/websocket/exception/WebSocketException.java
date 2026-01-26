package dev.scx.websocket.exception;

// todo 这个类待细化.
/// WebSocketException
///
/// @author scx567888
/// @version 0.0.1
public class WebSocketException extends Exception {

    public WebSocketException(String message) {
        super(message);
    }

    public WebSocketException(Throwable cause) {
        super(cause);
    }

    public WebSocketException(String message, Throwable cause) {
        super(message, cause);
    }

}
