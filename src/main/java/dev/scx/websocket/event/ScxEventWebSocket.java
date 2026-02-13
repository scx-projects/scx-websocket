package dev.scx.websocket.event;

import dev.scx.websocket.close_info.ScxWebSocketCloseInfo;
import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketInvalidStateException;
import dev.scx.websocket.exception.WebSocketProtocolException;
import dev.scx.websocket.message.WebSocketMessage;

import java.util.function.Consumer;

import static dev.scx.websocket.close_info.WebSocketCloseInfo.NORMAL_CLOSE;
import static dev.scx.websocket.message.WebSocketMessageType.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/// 事件形式的 websocket (本质是另一种风格的 ScxMessageWebSocket 用法)
///
/// @author scx567888
/// @version 0.0.1
public interface ScxEventWebSocket extends AutoCloseable {

    ScxEventWebSocket onText(Consumer<String> textHandler);

    ScxEventWebSocket onBinary(Consumer<byte[]> binaryHandler);

    ScxEventWebSocket onPing(Consumer<byte[]> pingHandler);

    ScxEventWebSocket onPong(Consumer<byte[]> pongHandler);

    ScxEventWebSocket onClose(Consumer<ScxWebSocketCloseInfo> closeHandler);

    ScxEventWebSocket onError(Consumer<Throwable> errorHandler);

    void sendMessage(WebSocketMessage message) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException;

    /// 以上回调设置完成之后调用以便启动 websocket 监听 (这个方法是 阻塞的)
    void start();

    /// 关闭底层连接.
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
