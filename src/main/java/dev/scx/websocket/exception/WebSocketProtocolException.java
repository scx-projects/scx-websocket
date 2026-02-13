package dev.scx.websocket.exception;

/// 协议异常.
///
/// 单帧异常 如 帧过大. 掩码错误 或者 多帧组合异常 如 非法 聚合帧 等.
///
/// @author scx567888
/// @version 0.0.1
public class WebSocketProtocolException extends RuntimeException {

    private final int closeCode;

    public WebSocketProtocolException(int closeCode, String message) {
        super(message);
        this.closeCode = closeCode;
    }

    public int closeCode() {
        return closeCode;
    }

}
