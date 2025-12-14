package dev.scx.websocket;

import dev.scx.http.ScxHttpServerResponse;

/// ScxServerWebSocketHandshakeResponse
///
/// @author scx567888
/// @version 0.0.1
public interface ScxServerWebSocketHandshakeResponse extends ScxHttpServerResponse {

    @Override
    ScxServerWebSocketHandshakeRequest request();

    /// 获取 webSocket, 调用表示接受连接
    ScxWebSocket webSocket();

}
