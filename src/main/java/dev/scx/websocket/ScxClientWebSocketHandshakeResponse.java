package dev.scx.websocket;

import dev.scx.http.ScxHttpClientResponse;

/// ScxClientWebSocketHandshakeResponse
///
/// @author scx567888
/// @version 0.0.1
public interface ScxClientWebSocketHandshakeResponse extends ScxHttpClientResponse {

    /// 握手是否成功
    boolean handshakeSucceeded();

    /// 获取 websocket.
    ScxWebSocket webSocket();

}
