package dev.scx.websocket;

import dev.scx.websocket.close_info.ScxWebSocketCloseInfo;
import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketInvalidStateException;
import dev.scx.websocket.exception.WebSocketProtocolException;

import static dev.scx.websocket.WebSocketOpCode.*;
import static dev.scx.websocket.close_info.WebSocketCloseInfo.NORMAL_CLOSE;
import static java.nio.charset.StandardCharsets.UTF_8;

/// ScxWebSocket 是一个帧级 (frame-level) 的 WebSocket 接口,
/// 负责保证 WebSocket 协议中最基本的 closing handshake 行为.
///
/// ### Closing handshake 行为约定
///
/// - 当 [#readFrame()] 读取到 `CLOSE` 帧时,
///   若本端尚未发送过 `CLOSE` 帧，则必须尝试发送一个 `CLOSE` 帧作为响应.
///
/// - 当通过 [#sendFrame(WebSocketFrame)] 成功发送 `CLOSE` 帧后，
///   连接将进入 closing 状态; 在该状态下禁止发送数据帧  (`TEXT`/`BINARY`/`CONTINUATION`).
///
/// 上述行为由 `ScxWebSocket` 的实现保证, 调用方无需显式处理 closing handshake 的细节.
///
/// 调用 [#close()] 表示一次资源级的终止 (abort), 不属于 WebSocket 协议层的 closing handshake.
///
/// @author scx567888
/// @version 0.0.1
public interface ScxWebSocket extends AutoCloseable {

    WebSocketFrame readFrame() throws WebSocketIOException, WebSocketProtocolException;

    void sendFrame(WebSocketFrame frame) throws WebSocketIOException, WebSocketInvalidStateException;

    /// 立即关闭底层连接并释放相关资源.
    ///
    /// 该方法表示一次**资源级的终止(abort)**, 而非 WebSocket 协议层的关闭：
    ///
    /// - 不发送任何 WebSocket 帧（包括 CLOSE 帧)
    /// - 不等待对端响应, 也不保证完成 WebSocket closing handshake
    /// - 不抛出受检异常.
    /// - 多次调用是幂等的.
    ///
    /// 调用此方法后, 底层连接将被视为已终止,
    /// 后续对 [#readFrame()] 或 [#sendFrame(WebSocketFrame)] 的调用, 将抛出 [WebSocketIOException].
    ///
    /// 若需要按照 WebSocket 协议进行优雅关闭,
    /// 应先调用 [#sendClose(ScxWebSocketCloseInfo)] 发送 CLOSE 帧,
    /// 再根据需要调用本方法释放底层资源.
    @Override
    void close();

    default void send(String textMessage) throws WebSocketIOException, WebSocketInvalidStateException {
        var payload = textMessage.getBytes(UTF_8);
        var frame = WebSocketFrame.of(TEXT, payload, true);
        sendFrame(frame);
    }

    default void send(byte[] binaryMessage) throws WebSocketIOException, WebSocketInvalidStateException {
        var frame = WebSocketFrame.of(BINARY, binaryMessage, true);
        sendFrame(frame);
    }

    default void sendPing(byte[] data) throws WebSocketIOException, WebSocketInvalidStateException {
        var frame = WebSocketFrame.of(PING, data, true);
        sendFrame(frame);
    }

    default void sendPong(byte[] data) throws WebSocketIOException, WebSocketInvalidStateException {
        var frame = WebSocketFrame.of(PONG, data, true);
        sendFrame(frame);
    }

    default void sendPing() throws WebSocketIOException, WebSocketInvalidStateException {
        sendPing(new byte[0]);
    }

    default void sendPong() throws WebSocketIOException, WebSocketInvalidStateException {
        sendPong(new byte[0]);
    }

    default void sendClose(ScxWebSocketCloseInfo closeInfo) throws WebSocketIOException, WebSocketInvalidStateException {
        var closePayload = closeInfo.toPayload();
        var frame = WebSocketFrame.of(CLOSE, closePayload, true);
        sendFrame(frame);
    }

    default void sendClose(int code, String reason) throws WebSocketIOException, WebSocketInvalidStateException {
        sendClose(ScxWebSocketCloseInfo.of(code, reason));
    }

    default void sendClose() throws WebSocketIOException, WebSocketInvalidStateException {
        sendClose(NORMAL_CLOSE);
    }

}
