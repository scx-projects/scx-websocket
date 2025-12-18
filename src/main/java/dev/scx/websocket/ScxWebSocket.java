package dev.scx.websocket;

import dev.scx.websocket.close_info.ScxWebSocketCloseInfo;
import dev.scx.websocket.exception.WebSocketException;

import static dev.scx.websocket.WebSocketOpCode.*;
import static dev.scx.websocket.close_info.WebSocketCloseInfo.NORMAL_CLOSE;

/// ScxWebSocket
///
/// @author scx567888
/// @version 0.0.1
public interface ScxWebSocket extends AutoCloseable {

    WebSocketFrame readFrame() throws WebSocketException;

    ScxWebSocket sendFrame(WebSocketFrame frame);

    /// 直接 终止连接
    @Override
    void close();

    boolean isClosed();

    default ScxWebSocket send(String textMessage, boolean last) {
        var payload = textMessage != null ? textMessage.getBytes() : new byte[]{};
        var frame = WebSocketFrame.of(TEXT, payload, last);
        return sendFrame(frame);
    }

    default ScxWebSocket send(byte[] binaryMessage, boolean last) {
        var frame = WebSocketFrame.of(BINARY, binaryMessage, last);
        return sendFrame(frame);
    }

    default ScxWebSocket send(String textMessage) {
        return send(textMessage, true);
    }

    default ScxWebSocket send(byte[] binaryMessage) {
        return send(binaryMessage, true);
    }

    default ScxWebSocket sendPing(byte[] data) {
        var frame = WebSocketFrame.of(PING, data);
        return sendFrame(frame);
    }

    default ScxWebSocket sendPong(byte[] data) {
        var frame = WebSocketFrame.of(PONG, data);
        return sendFrame(frame);
    }

    default ScxWebSocket sendClose(ScxWebSocketCloseInfo closeInfo) {
        var closePayload = closeInfo.toPayload();
        var frame = WebSocketFrame.of(CLOSE, closePayload);
        return sendFrame(frame);
    }

    default ScxWebSocket sendClose(int code, String reason) {
        return sendClose(ScxWebSocketCloseInfo.of(code, reason));
    }

    default ScxWebSocket sendClose() {
        return sendClose(NORMAL_CLOSE);
    }

}
