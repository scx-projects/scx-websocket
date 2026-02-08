package dev.scx.websocket.frame;

import dev.scx.websocket.op_code.WebSocketOpCode;
import dev.scx.websocket.protocol_frame.WebSocketProtocolFrame;

import static dev.scx.random.ScxRandom.randomBytes;

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
            case CLOSE -> checkCloseFrame(opCode, payloadData, fin);
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

    /// 注意此处创建的 WebSocketProtocolFrame 中的 payloadData 还没有被掩码计算.
    public static WebSocketProtocolFrame toProtocolFrame(WebSocketFrame frame, boolean isClient) {

        var protocolFrame = new WebSocketProtocolFrame();
        protocolFrame.fin = frame.fin();
        protocolFrame.rsv1 = false;
        protocolFrame.rsv2 = false;
        protocolFrame.rsv3 = false;
        protocolFrame.opCode = frame.opCode().code();

        // 和服务器端不同, 客户端的是需要发送掩码的
        if (isClient) {
            protocolFrame.masked = true;
            protocolFrame.maskingKey = randomBytes(4);
        } else {
            protocolFrame.masked = false;
            protocolFrame.maskingKey = null;
        }

        byte[] payloadData = frame.payloadData();
        protocolFrame.payloadLength = payloadData.length;
        protocolFrame.payloadData = payloadData;
        return protocolFrame;
    }

    public static WebSocketFrame fromProtocolFrame(WebSocketProtocolFrame protocolFrame, boolean isClient) {
        return WebSocketFrame.of(null,null,false);
    }

}
