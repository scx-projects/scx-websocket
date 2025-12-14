package dev.scx.websocket.event;

/// 关闭处理器
///
/// @author scx567888
/// @version 0.0.1
public interface CloseHandler {

    void handle(int code, String reason);

}
