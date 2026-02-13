package dev.scx.websocket.message;

import dev.scx.websocket.close_info.ScxWebSocketCloseInfo;
import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketInvalidStateException;
import dev.scx.websocket.exception.WebSocketProtocolException;

import static dev.scx.websocket.close_info.WebSocketCloseInfo.NORMAL_CLOSE;
import static dev.scx.websocket.message.WebSocketMessageType.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/// 消息级别的 websocket 接口.
/// 内部会组合帧或者分帧发送.
/// 内部会保证 close 握手的逻辑.
///
/// @author scx567888
/// @version 0.0.1
public interface ScxMessageWebSocket extends AutoCloseable {

    WebSocketMessage readMessage() throws WebSocketIOException, WebSocketProtocolException;

    void sendMessage(WebSocketMessage message) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException;

    @Override
    void close();

    default void send(String textMessage) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        sendMessage(new WebSocketMessage(TEXT, textMessage.getBytes(UTF_8)));
    }

    default void send(byte[] binaryMessage) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        sendMessage(new WebSocketMessage(BINARY, binaryMessage));
    }

    default void sendPing(byte[] data) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        sendMessage(new WebSocketMessage(PING, data));
    }

    default void sendPong(byte[] data) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        sendMessage(new WebSocketMessage(PONG, data));
    }

    default void sendClose(ScxWebSocketCloseInfo closeInfo) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        sendMessage(new WebSocketMessage(CLOSE, closeInfo.toPayload()));
    }

    default void sendClose(int code, String reason) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        sendClose(ScxWebSocketCloseInfo.of(code, reason));
    }

    default void sendClose() throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        sendClose(NORMAL_CLOSE);
    }

}
