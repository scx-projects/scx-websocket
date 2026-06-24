package dev.scx.websocket;

import dev.scx.io.endpoint.ByteEndpoint;
import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketInvalidStateException;
import dev.scx.websocket.exception.WebSocketProtocolException;
import dev.scx.websocket.frame.ScxFrameWebSocket;

import static dev.scx.websocket.WebSocketCloseInfo.NORMAL_CLOSE;

/// 消息级别的 websocket 接口.
/// 内部会组合帧或者分帧发送.
/// 内部会保证 close 握手的逻辑.
///
/// @author scx567888
/// @version 0.0.1
public interface ScxWebSocket extends AutoCloseable {

    static ScxWebSocket of(ScxFrameWebSocket frameWebSocket, long maxMessageSize) {
        return new ScxWebSocketImpl(frameWebSocket, maxMessageSize);
    }

    static ScxWebSocket of(ScxFrameWebSocket frameWebSocket) {
        return new ScxWebSocketImpl(frameWebSocket, 1024 * 1024 * 64); // 默认 64MB
    }

    static ScxWebSocket of(ByteEndpoint endpoint, boolean isClient, long maxMessageSize) {
        return new ScxWebSocketImpl(ScxFrameWebSocket.of(endpoint, isClient), maxMessageSize);
    }

    static ScxWebSocket of(ByteEndpoint endpoint, boolean isClient) {
        return new ScxWebSocketImpl(ScxFrameWebSocket.of(endpoint, isClient), 1024 * 1024 * 64); // 默认 64MB
    }

    WebSocketMessage read() throws WebSocketIOException, WebSocketProtocolException;

    void send(WebSocketMessage message) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException;

    /// close 语义等同于 [ScxFrameWebSocket#close()]
    @Override
    void close();

    default void send(String text) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        send(new TextMessage(text));
    }

    default void send(byte[] binary) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        send(new BinaryMessage(binary));
    }

    default void sendPing(byte[] data) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        send(new PingMessage(data));
    }

    default void sendPong(byte[] data) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        send(new PongMessage(data));
    }

    default void sendClose(WebSocketCloseInfo closeInfo) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        send(new CloseMessage(closeInfo));
    }

    default void sendClose(int code, String reason) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        sendClose(new WebSocketCloseInfo(code, reason));
    }

    default void sendClose() throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        sendClose(NORMAL_CLOSE);
    }

}
