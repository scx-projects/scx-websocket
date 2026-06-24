package dev.scx.websocket;

import dev.scx.websocket.frame.WebSocketFrame;
import dev.scx.websocket.frame.WebSocketOpCode;

import static dev.scx.websocket.WebSocketCloseInfoHelper.parseCloseInfo;
import static dev.scx.websocket.WebSocketCloseInfoHelper.toClosePayload;
import static dev.scx.websocket.frame.WebSocketOpCode.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/// ScxWebSocketImplHelper
///
/// @author scx567888
/// @version 0.0.1
final class ScxWebSocketImplHelper {

    public static WebSocketMessage frameToMessage(WebSocketFrame frame) {
        if (!frame.fin()) {
            throw new IllegalArgumentException("frame is not fin");
        }
        return switch (frame.opCode()) {
            case CONTINUATION -> throw new IllegalArgumentException("can not support CONTINUATION");
            case TEXT -> new TextMessage(new String(frame.payloadData(), UTF_8));
            case BINARY -> new BinaryMessage(frame.payloadData());
            case PING -> new PingMessage(frame.payloadData());
            case PONG -> new PongMessage(frame.payloadData());
            case CLOSE -> new CloseMessage(parseCloseInfo(frame.payloadData()));
        };
    }

    public static WebSocketFrame messageToFrame(WebSocketMessage message) {
        return switch (message) {
            case TextMessage textMessage -> new WebSocketFrame(TEXT, textMessage.text().getBytes(UTF_8), true);
            case BinaryMessage binaryMessage -> new WebSocketFrame(BINARY, binaryMessage.binary(), true);
            case PingMessage pingMessage -> new WebSocketFrame(PING, pingMessage.data(), true);
            case PongMessage pongMessage -> new WebSocketFrame(PONG, pongMessage.data(), true);
            case CloseMessage closeMessage -> new WebSocketFrame(CLOSE, toClosePayload(closeMessage.closeInfo()), true);
        };
    }

    public static WebSocketOpCode messageToOpCode(WebSocketMessage message) {
        return switch (message) {
            case TextMessage _ -> TEXT;
            case BinaryMessage _ -> BINARY;
            case PingMessage _ -> PING;
            case PongMessage _ -> PONG;
            case CloseMessage _ -> CLOSE;
        };
    }

}
