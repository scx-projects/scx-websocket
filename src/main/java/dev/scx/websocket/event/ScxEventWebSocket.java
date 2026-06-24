package dev.scx.websocket.event;

import dev.scx.websocket.*;
import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketInvalidStateException;
import dev.scx.websocket.exception.WebSocketProtocolException;
import dev.scx.websocket.frame.ScxFrameWebSocket;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static dev.scx.websocket.WebSocketCloseInfo.NORMAL_CLOSE;

/// 事件形式的 websocket (本质是另一种风格的 ScxWebSocket 用法)
///
/// @author scx567888
/// @version 0.0.1
public interface ScxEventWebSocket extends AutoCloseable {

    static ScxEventWebSocket of(ScxWebSocket messageWebSocket) {
        return new ScxEventWebSocketImpl(messageWebSocket);
    }

    /// 为了防止 回调之间互相阻塞 可以传递一个 callbackExecutor 来处理每个 callback
    static ScxEventWebSocket of(ScxWebSocket messageWebSocket, Executor callbackExecutor) {
        return new ScxEventWebSocketImpl(messageWebSocket, callbackExecutor);
    }

    ScxEventWebSocket onText(Consumer<String> textHandler);

    ScxEventWebSocket onBinary(Consumer<byte[]> binaryHandler);

    ScxEventWebSocket onPing(Consumer<byte[]> pingHandler);

    ScxEventWebSocket onPong(Consumer<byte[]> pongHandler);

    ScxEventWebSocket onClose(Consumer<WebSocketCloseInfo> closeHandler);

    ScxEventWebSocket onError(Consumer<Throwable> errorHandler);

    void send(WebSocketMessage message) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException;

    /// 以上回调设置完成之后调用以便启动 websocket 监听 (这个方法是 阻塞的)
    void start();

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
