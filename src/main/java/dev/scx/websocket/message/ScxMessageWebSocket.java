package dev.scx.websocket.message;

import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketInvalidStateException;
import dev.scx.websocket.exception.WebSocketProtocolException;

/// 消息级别的 websocket 接口.
/// 内部会组合帧或者分帧发送.
/// 内部会保证 close 握手的逻辑.
public interface ScxMessageWebSocket extends AutoCloseable {

    WebSocketMessage readMessage() throws WebSocketIOException, WebSocketProtocolException;

    void sendMessage(WebSocketMessage message) throws WebSocketIOException, WebSocketInvalidStateException;

    @Override
    void close();

}
