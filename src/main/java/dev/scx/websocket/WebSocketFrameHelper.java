package dev.scx.websocket;

/// WebSocketFrameHelper
///
/// @author scx567888
/// @version 0.0.1
final class WebSocketFrameHelper {

    public static void checkFrame(WebSocketOpCode opCode, byte[] payloadData, boolean fin) throws NullPointerException, IllegalArgumentException {
        if (opCode == null) {
            throw new NullPointerException("opCode must not be null");
        }
        if (payloadData == null) {
            throw new NullPointerException("payloadData must not be null");
        }
        switch (opCode) {
            case PING, PONG -> checkControlFrame(opCode, payloadData, fin);
            case CLOSE -> checkCloseFrame(opCode,payloadData,fin);
        }
    }

    private static void checkControlFrame(WebSocketOpCode opCode, byte[] payloadData, boolean fin) throws IllegalArgumentException {
        if (!fin) {
            throw new IllegalArgumentException(opCode + " frame must have fin = true");
        }
        if (payloadData.length > 125) {
            throw new IllegalArgumentException(opCode + " frame payload length must be <= 125 bytes");
        }
    }

    private static void checkCloseFrame(WebSocketOpCode opCode, byte[] payloadData, boolean fin) throws IllegalArgumentException {
        checkControlFrame(opCode, payloadData, fin);
        if (payloadData.length == 1) {
            throw new IllegalArgumentException("close frame payload length must not be 1");
        }
    }

}
