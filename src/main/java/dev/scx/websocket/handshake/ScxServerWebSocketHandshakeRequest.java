package dev.scx.websocket.handshake;

import dev.scx.exception.ScxWrappedException;
import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.sender.IllegalSenderStateException;
import dev.scx.http.sender.ScxHttpReceiveException;
import dev.scx.http.sender.ScxHttpSendException;
import dev.scx.websocket.ScxFrameWebSocket;

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

    default ScxFrameWebSocket upgrade() throws IllegalSenderStateException, ScxHttpSendException, ScxWrappedException, ScxHttpReceiveException {
        return response().upgrade();
    }

}
