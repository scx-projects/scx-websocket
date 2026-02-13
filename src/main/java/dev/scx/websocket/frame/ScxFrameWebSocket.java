package dev.scx.websocket.frame;

import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketProtocolException;

/// ScxFrameWebSocket 是一个帧级 (frame-level) 的 WebSocket 接口,
///
/// 只负责保证读取和写入时帧的协议合法性 (单帧).
/// 不涉及任何状态管理.
///
/// 调用 [#close()] 表示一次资源级的终止 (abort), 不属于 WebSocket 协议层的 closing handshake.
///
/// @author scx567888
/// @version 0.0.1
public interface ScxFrameWebSocket extends AutoCloseable {

    /// @throws WebSocketIOException       底层 IO 异常.
    /// @throws WebSocketProtocolException 读取到的帧不合法.
    WebSocketFrame readFrame() throws WebSocketIOException, WebSocketProtocolException;

    /// @throws WebSocketIOException       底层 IO 异常.
    /// @throws WebSocketProtocolException 写出的帧不合法.
    void sendFrame(WebSocketFrame frame) throws WebSocketIOException, WebSocketProtocolException;

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
    /// 应先调用 [#sendFrame(WebSocketFrame)] 发送 CLOSE 帧,
    /// 再根据需要调用本方法释放底层资源.
    @Override
    void close();

}
