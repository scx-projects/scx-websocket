package dev.scx.websocket;

import dev.scx.http.ScxHttpServerRequest;

import static dev.scx.http.headers.HttpHeaderName.SEC_WEBSOCKET_KEY;
import static dev.scx.http.headers.HttpHeaderName.SEC_WEBSOCKET_VERSION;

/// ScxServerWebSocketHandshakeRequest
///
/// @author scx567888
/// @version 0.0.1
public interface ScxServerWebSocketHandshakeRequest extends ScxHttpServerRequest {

    @Override
    ScxServerWebSocketHandshakeResponse response();

    default String secWebSocketKey() {
        return getHeader(SEC_WEBSOCKET_KEY);
    }

    default String secWebSocketVersion() {
        return getHeader(SEC_WEBSOCKET_VERSION);
    }

    default ScxWebSocket webSocket() {
        return response().webSocket();
    }

}
