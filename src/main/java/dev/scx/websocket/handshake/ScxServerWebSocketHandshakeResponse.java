package dev.scx.websocket.handshake;

import dev.scx.exception.ScxWrappedException;
import dev.scx.http.ScxHttpServerResponse;
import dev.scx.http.sender.IllegalSenderStateException;
import dev.scx.http.sender.ScxHttpReceiveException;
import dev.scx.http.sender.ScxHttpSendException;
import dev.scx.websocket.frame.ScxFrameWebSocket;

/// ScxServerWebSocketHandshakeResponse
///
/// @author scx567888
/// @version 0.0.1
public interface ScxServerWebSocketHandshakeResponse extends ScxHttpServerResponse {

    @Override
    ScxServerWebSocketHandshakeRequest request();

    /// 执行 WebSocket 协议升级.
    ///
    /// - 首次调用: 提交 101 Switching Protocols, 完成升级, 创建并缓存 WebSocket 会话并返回
    /// - 再次调用: 不再产生任何 IO, 直接返回同一 WebSocket 实例.
    /// - 与普通 HTTP send(...) 提交互斥.
    ScxFrameWebSocket upgrade() throws IllegalSenderStateException, ScxHttpSendException, ScxWrappedException, ScxHttpReceiveException;

}
