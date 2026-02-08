package dev.scx.websocket.frame;

import dev.scx.websocket.exception.WebSocketProtocolException;
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

    /// 根据 WebSocketFrame 创建 WebSocketProtocolFrame.
    /// 这里我们无需校验 WebSocketFrame, 因为其构造函数已经保证一定是一个正确的语义.
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

        byte[] maskingKey = protocolFrame.maskingKey;
        byte[] payloadData = frame.payloadData();

        // 此处为了性能我们就地更改数组.
        if (protocolFrame.masked) {
            for (int i = 0; i < payloadData.length; i = i + 1) {
                payloadData[i] = (byte) (payloadData[i] ^ maskingKey[i % 4]);
            }
        }

        protocolFrame.payloadLength = payloadData.length;
        protocolFrame.payloadData = payloadData;
        return protocolFrame;
    }

    // todo 这里的校验太薄弱了. 需要加强.
    public static WebSocketFrame fromProtocolFrame(WebSocketProtocolFrame protocolFrame, boolean isClient) throws WebSocketProtocolException {
        // 这里可能找不到报错.
        var opCode = WebSocketOpCode.of(protocolFrame.opCode);

        var fin = protocolFrame.fin;

        var masked = protocolFrame.masked;
        var maskingKey = protocolFrame.maskingKey;
        var payloadData = protocolFrame.payloadData;

        // 掩码计算
        if (masked) {
            for (int i = 0; i < payloadData.length; i = i + 1) {
                payloadData[i] = (byte) (payloadData[i] ^ maskingKey[i % 4]);
            }
        }

        return WebSocketFrame.of(opCode, payloadData, fin);
    }

}
