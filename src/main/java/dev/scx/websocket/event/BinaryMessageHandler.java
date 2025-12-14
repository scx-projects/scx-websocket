package dev.scx.websocket.event;

/// 二进制消息处理器
///
/// @author scx567888
/// @version 0.0.1
public interface BinaryMessageHandler {

    void handle(byte[] binary, boolean last);

}
