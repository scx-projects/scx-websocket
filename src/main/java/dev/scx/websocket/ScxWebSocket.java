package dev.scx.websocket;

import dev.scx.io.exception.AlreadyClosedException;
import dev.scx.io.exception.ScxIOException;
import dev.scx.websocket.close_info.ScxWebSocketCloseInfo;
import dev.scx.websocket.exception.NoMoreWebSocketFrameException;
import dev.scx.websocket.exception.WebSocketParseException;
import dev.scx.websocket.exception.WebsocketAlreadySentCloseException;

import static dev.scx.websocket.WebSocketOpCode.*;
import static dev.scx.websocket.close_info.WebSocketCloseInfo.NORMAL_CLOSE;

/// ScxWebSocket
///
/// @author scx567888
/// @version 0.0.1
public interface ScxWebSocket extends AutoCloseable {

    WebSocketFrame readFrame() throws WebSocketParseException, NoMoreWebSocketFrameException, ScxIOException, AlreadyClosedException;

    ScxWebSocket sendFrame(WebSocketFrame frame) throws WebsocketAlreadySentCloseException, ScxIOException, AlreadyClosedException;

    /// 是否已经发送了 close 帧
    boolean closeSent();

    /// 关闭 "底层连接"
    @Override
    void close() throws ScxIOException;

    /// 是否已经关闭 "底层连接"
    boolean isClosed();

    default ScxWebSocket send(String textMessage, boolean last) throws WebsocketAlreadySentCloseException, ScxIOException, AlreadyClosedException {
        var payload = textMessage != null ? textMessage.getBytes() : new byte[]{};
        var frame = WebSocketFrame.of(TEXT, payload, last);
        return sendFrame(frame);
    }

    default ScxWebSocket send(byte[] binaryMessage, boolean last) throws WebsocketAlreadySentCloseException, ScxIOException, AlreadyClosedException {
        var frame = WebSocketFrame.of(BINARY, binaryMessage, last);
        return sendFrame(frame);
    }

    default ScxWebSocket send(String textMessage) throws WebsocketAlreadySentCloseException, ScxIOException, AlreadyClosedException {
        return send(textMessage, true);
    }

    default ScxWebSocket send(byte[] binaryMessage) throws WebsocketAlreadySentCloseException, ScxIOException, AlreadyClosedException {
        return send(binaryMessage, true);
    }

    default ScxWebSocket sendPing(byte[] data) throws WebsocketAlreadySentCloseException, ScxIOException, AlreadyClosedException {
        var frame = WebSocketFrame.of(PING, data);
        return sendFrame(frame);
    }

    default ScxWebSocket sendPong(byte[] data) throws WebsocketAlreadySentCloseException, ScxIOException, AlreadyClosedException {
        var frame = WebSocketFrame.of(PONG, data);
        return sendFrame(frame);
    }

    default ScxWebSocket sendClose(ScxWebSocketCloseInfo closeInfo) throws WebsocketAlreadySentCloseException, ScxIOException, AlreadyClosedException {
        var closePayload = closeInfo.toPayload();
        var frame = WebSocketFrame.of(CLOSE, closePayload);
        return sendFrame(frame);
    }

    default ScxWebSocket sendClose(int code, String reason) throws WebsocketAlreadySentCloseException, ScxIOException, AlreadyClosedException {
        return sendClose(ScxWebSocketCloseInfo.of(code, reason));
    }

    default ScxWebSocket sendClose() throws WebsocketAlreadySentCloseException, ScxIOException, AlreadyClosedException {
        return sendClose(NORMAL_CLOSE);
    }

}
