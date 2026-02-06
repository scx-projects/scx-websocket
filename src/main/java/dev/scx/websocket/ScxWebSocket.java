package dev.scx.websocket;

import dev.scx.websocket.close_info.ScxWebSocketCloseInfo;
import dev.scx.websocket.exception.WebSocketAlreadyClosedException;
import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketProtocolException;

import static dev.scx.websocket.WebSocketOpCode.*;
import static dev.scx.websocket.close_info.WebSocketCloseInfo.NORMAL_CLOSE;
import static java.nio.charset.StandardCharsets.UTF_8;

/// ScxWebSocket
///
/// @author scx567888
/// @version 0.0.1
public interface ScxWebSocket extends AutoCloseable {

    WebSocketFrame readFrame() throws WebSocketProtocolException, WebSocketIOException, WebSocketAlreadyClosedException;

    void sendFrame(WebSocketFrame frame) throws WebSocketProtocolException, WebSocketIOException, WebSocketAlreadyClosedException;

    /// 立即关闭底层连接并释放相关资源.
    ///
    /// 该方法表示一次**资源级的终止(abort)**, 而非 WebSocket 协议层的关闭：
    ///
    /// - 不发送任何 WebSocket 帧（包括 CLOSE 帧)
    /// - 不等待对端响应, 也不保证完成 WebSocket closing handshake
    /// - 不抛出受检异常
    /// - 多次调用是幂等的
    ///
    /// 调用此方法后, 底层连接将被视为已终止,
    /// 后续对 [#readFrame()] 或 [#sendFrame(WebSocketFrame)] 的调用
    /// 将抛出 [WebSocketException].
    ///
    /// 若需要按照 WebSocket 协议进行优雅关闭,
    /// 应先调用 [#sendClose(ScxWebSocketCloseInfo)] 发送 CLOSE 帧,
    /// 再根据需要调用本方法释放底层资源.
    @Override
    void close();

    default void send(String textMessage) throws WebSocketProtocolException, WebSocketIOException, WebSocketAlreadyClosedException {
        var payload = textMessage.getBytes(UTF_8);
        var frame = WebSocketFrame.of(TEXT, payload);
        sendFrame(frame);
    }

    default void send(byte[] binaryMessage) throws WebSocketProtocolException, WebSocketIOException, WebSocketAlreadyClosedException {
        var frame = WebSocketFrame.of(BINARY, binaryMessage);
        sendFrame(frame);
    }

    default void sendPing(byte[] data) throws WebSocketProtocolException, WebSocketIOException, WebSocketAlreadyClosedException {
        var frame = WebSocketFrame.of(PING, data);
        sendFrame(frame);
    }

    default void sendPong(byte[] data) throws WebSocketProtocolException, WebSocketIOException, WebSocketAlreadyClosedException {
        var frame = WebSocketFrame.of(PONG, data);
        sendFrame(frame);
    }

    default void sendPing() throws WebSocketProtocolException, WebSocketIOException, WebSocketAlreadyClosedException {
        sendPing(new byte[0]);
    }

    default void sendPong() throws WebSocketProtocolException, WebSocketIOException, WebSocketAlreadyClosedException {
        sendPong(new byte[0]);
    }

    default void sendClose(ScxWebSocketCloseInfo closeInfo) throws WebSocketProtocolException, WebSocketIOException, WebSocketAlreadyClosedException {
        var closePayload = closeInfo.toPayload();
        var frame = WebSocketFrame.of(CLOSE, closePayload);
        sendFrame(frame);
    }

    default void sendClose(int code, String reason) throws WebSocketProtocolException, WebSocketIOException, WebSocketAlreadyClosedException {
        sendClose(ScxWebSocketCloseInfo.of(code, reason));
    }

    default void sendClose() throws WebSocketProtocolException, WebSocketIOException, WebSocketAlreadyClosedException {
        sendClose(NORMAL_CLOSE);
    }

}
