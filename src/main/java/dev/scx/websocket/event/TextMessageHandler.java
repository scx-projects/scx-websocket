package dev.scx.websocket.event;

/// 文本消息处理器
///
/// @author scx567888
/// @version 0.0.1
public interface TextMessageHandler {

    void handle(String text, boolean last);

}
