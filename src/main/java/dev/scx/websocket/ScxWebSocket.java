package dev.scx.websocket;

import dev.scx.io.endpoint.ByteEndpoint;
import dev.scx.websocket.event.ScxEventWebSocket;
import dev.scx.websocket.event.ScxEventWebSocketImpl;
import dev.scx.websocket.frame.ScxFrameWebSocket;
import dev.scx.websocket.frame.ScxFrameWebSocketImpl;
import dev.scx.websocket.message.ScxMessageWebSocket;
import dev.scx.websocket.message.ScxMessageWebSocketImpl;

import java.util.concurrent.Executor;

/// ScxWebSocket 入口类.
///
/// @author scx567888
/// @version 0.0.1
public final class ScxWebSocket {

    public static ScxFrameWebSocket createFrameWebSocket(ByteEndpoint endpoint, boolean isClient, long maxWebSocketFrameSize) {
        return new ScxFrameWebSocketImpl(endpoint, isClient, maxWebSocketFrameSize);
    }

    public static ScxFrameWebSocket createFrameWebSocket(ByteEndpoint endpoint, boolean isClient) {
        return createFrameWebSocket(endpoint, isClient, 1024 * 1024 * 16); // 默认 16 MB
    }

    public static ScxMessageWebSocket createMessageWebSocket(ScxFrameWebSocket frameWebSocket, long maxWebSocketMessageSize) {
        return new ScxMessageWebSocketImpl(frameWebSocket, maxWebSocketMessageSize);
    }

    public static ScxMessageWebSocket createMessageWebSocket(ByteEndpoint endpoint, boolean isClient, long maxWebSocketMessageSize) {
        return createMessageWebSocket(createFrameWebSocket(endpoint, isClient, maxWebSocketMessageSize), maxWebSocketMessageSize);
    }

    public static ScxMessageWebSocket createMessageWebSocket(ByteEndpoint endpoint, boolean isClient) {
        return createMessageWebSocket(endpoint, isClient, 1024 * 1024 * 64); // 默认 64MB
    }

    public static ScxEventWebSocket createEventWebSocket(ScxMessageWebSocket messageWebSocket) {
        return new ScxEventWebSocketImpl(messageWebSocket);
    }

    /// 为了防止 回调之间互相阻塞 可以传递一个 callbackExecutor 来处理每个 callback
    public static ScxEventWebSocket createEventWebSocket(ScxMessageWebSocket messageWebSocket, Executor callbackExecutor) {
        return new ScxEventWebSocketImpl(messageWebSocket, callbackExecutor);
    }

    public static ScxEventWebSocket createEventWebSocket(ByteEndpoint endpoint, boolean isClient) {
        return createEventWebSocket(createMessageWebSocket(endpoint, isClient));
    }

    public static ScxEventWebSocket createEventWebSocket(ByteEndpoint endpoint, boolean isClient, Executor callbackExecutor) {
        return createEventWebSocket(createMessageWebSocket(endpoint, isClient), callbackExecutor);
    }

}
