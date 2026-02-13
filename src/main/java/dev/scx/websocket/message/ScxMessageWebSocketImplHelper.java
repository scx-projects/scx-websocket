package dev.scx.websocket.message;

import dev.scx.websocket.frame.WebSocketFrame;
import dev.scx.websocket.op_code.WebSocketOpCode;

import static dev.scx.websocket.op_code.WebSocketOpCode.*;

/// ScxMessageWebSocketImplHelper
///
/// @author scx567888
/// @version 0.0.1
final class ScxMessageWebSocketImplHelper {

    public static WebSocketOpCode messageTypeToOpCode(WebSocketMessageType messageType) {
        return switch (messageType) {
            case TEXT -> TEXT;
            case BINARY -> BINARY;
            case CLOSE -> CLOSE;
            case PING -> PING;
            case PONG -> PONG;
        };
    }

    public static WebSocketMessageType opCodeToMessageType(WebSocketOpCode opCode) {
        return switch (opCode) {
            case CONTINUATION -> throw new IllegalArgumentException("can not support CONTINUATION");
            case TEXT -> WebSocketMessageType.TEXT;
            case BINARY -> WebSocketMessageType.BINARY;
            case CLOSE -> WebSocketMessageType.CLOSE;
            case PING -> WebSocketMessageType.PING;
            case PONG -> WebSocketMessageType.PONG;
        };
    }

    public static WebSocketMessage frameToMessage(WebSocketFrame frame) {
        if (!frame.fin()) {
            throw new IllegalArgumentException("frame is not fin");
        }
        return new WebSocketMessage(opCodeToMessageType(frame.opCode()), frame.payloadData());
    }

    public static WebSocketFrame messageToFrame(WebSocketMessage message) {
        return new WebSocketFrame(messageTypeToOpCode(message.type()), message.payloadData(), true);
    }

}
