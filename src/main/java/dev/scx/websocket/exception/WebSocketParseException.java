package dev.scx.websocket.exception;

// todo 这个类 不合理
/// WebSocket 异常
///
/// @author scx567888
/// @version 0.0.1
public class WebSocketParseException extends Exception {

    public WebSocketParseException(String message) {
        super(message);
    }

    public WebSocketParseException(Throwable cause) {
        super(cause);
    }

    public WebSocketParseException(String message, Throwable cause) {
        super(message, cause);
    }

    private int closeCode;

    public WebSocketParseException(int closeCode, String message) {
        super(message);
        this.closeCode = closeCode;
    }

    public int closeCode() {
        return closeCode;
    }

}
