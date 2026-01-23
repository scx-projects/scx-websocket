package dev.scx.websocket;

import dev.scx.io.exception.InputAlreadyClosedException;
import dev.scx.io.exception.OutputAlreadyClosedException;
import dev.scx.io.exception.ScxInputException;
import dev.scx.io.exception.ScxOutputException;
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

    WebSocketFrame readFrame() throws WebSocketParseException, NoMoreWebSocketFrameException, ScxInputException, InputAlreadyClosedException;

    ScxWebSocket sendFrame(WebSocketFrame frame) throws WebsocketAlreadySentCloseException, ScxOutputException, OutputAlreadyClosedException;

    /// 是否已经发送了 close 帧
    boolean closeSent();

    /// 关闭 "底层连接"
    @Override
    void close();

    /// 是否已经关闭 "底层连接"
    boolean isClosed();

    default ScxWebSocket send(String textMessage, boolean last) throws WebsocketAlreadySentCloseException, ScxOutputException, OutputAlreadyClosedException {
        var payload = textMessage != null ? textMessage.getBytes() : new byte[]{};
        var frame = WebSocketFrame.of(TEXT, payload, last);
        return sendFrame(frame);
    }

    default ScxWebSocket send(byte[] binaryMessage, boolean last) throws WebsocketAlreadySentCloseException, ScxOutputException, OutputAlreadyClosedException {
        var frame = WebSocketFrame.of(BINARY, binaryMessage, last);
        return sendFrame(frame);
    }

    default ScxWebSocket send(String textMessage) throws WebsocketAlreadySentCloseException, ScxOutputException, OutputAlreadyClosedException {
        return send(textMessage, true);
    }

    default ScxWebSocket send(byte[] binaryMessage) throws WebsocketAlreadySentCloseException, ScxOutputException, OutputAlreadyClosedException {
        return send(binaryMessage, true);
    }

    default ScxWebSocket sendPing(byte[] data) throws WebsocketAlreadySentCloseException, ScxOutputException, OutputAlreadyClosedException {
        var frame = WebSocketFrame.of(PING, data);
        return sendFrame(frame);
    }

    default ScxWebSocket sendPong(byte[] data) throws WebsocketAlreadySentCloseException, ScxOutputException, OutputAlreadyClosedException {
        var frame = WebSocketFrame.of(PONG, data);
        return sendFrame(frame);
    }

    default ScxWebSocket sendClose(ScxWebSocketCloseInfo closeInfo) throws WebsocketAlreadySentCloseException, ScxOutputException, OutputAlreadyClosedException {
        var closePayload = closeInfo.toPayload();
        var frame = WebSocketFrame.of(CLOSE, closePayload);
        return sendFrame(frame);
    }

    default ScxWebSocket sendClose(int code, String reason) throws WebsocketAlreadySentCloseException, ScxOutputException, OutputAlreadyClosedException {
        return sendClose(ScxWebSocketCloseInfo.of(code, reason));
    }

    default ScxWebSocket sendClose() throws WebsocketAlreadySentCloseException, ScxOutputException, OutputAlreadyClosedException {
        return sendClose(NORMAL_CLOSE);
    }

}
