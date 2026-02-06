package dev.scx.websocket.message;

import dev.scx.websocket.ScxWebSocket;
import dev.scx.websocket.WebSocketFrame;
import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketInvalidStateException;
import dev.scx.websocket.exception.WebSocketProtocolException;

/// ScxMessageWebSocket
///
/// readFrame 会聚合 CONTINUATION.
///
public final class ScxMessageWebSocket implements ScxWebSocket {

    private final ScxWebSocket scxWebSocket;

    public ScxMessageWebSocket(ScxWebSocket scxWebSocket) {
        this.scxWebSocket = scxWebSocket;
    }

    @Override
    public WebSocketFrame readFrame() throws WebSocketIOException, WebSocketProtocolException {
        // 这里怎么写.
        return null;
    }

    @Override
    public void sendFrame(WebSocketFrame frame) throws WebSocketIOException, WebSocketInvalidStateException {
        this.scxWebSocket.sendFrame(frame);
    }

    @Override
    public void close() {
        this.scxWebSocket.close();
    }

}
