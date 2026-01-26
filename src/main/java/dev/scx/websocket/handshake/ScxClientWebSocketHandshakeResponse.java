package dev.scx.websocket.handshake;

import dev.scx.http.ScxHttpClientResponse;
import dev.scx.websocket.ScxWebSocket;

/// ScxClientWebSocketHandshakeResponse
///
/// @author scx567888
/// @version 0.0.1
public interface ScxClientWebSocketHandshakeResponse extends ScxHttpClientResponse {

    /// 握手是否已被接受.
    boolean handshakeAccepted();

    /// 完成 WebSocket 协议升级.
    ///
    /// - 首次调用: 验证 101 Switching Protocols, 完成升级, 创建并缓存 WebSocket 会话并返回
    /// - 再次调用: 不再产生任何 IO, 直接返回同一 WebSocket 实例.
    ScxWebSocket upgrade() throws ScxClientWebSocketHandshakeRejectedException;

}
