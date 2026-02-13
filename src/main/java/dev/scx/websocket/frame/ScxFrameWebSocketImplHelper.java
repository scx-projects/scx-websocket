package dev.scx.websocket.frame;

import dev.scx.websocket.exception.WebSocketProtocolException;
import dev.scx.websocket.op_code.WebSocketOpCode;

import static dev.scx.random.ScxRandom.randomBytes;
import static dev.scx.websocket.close_info.WebSocketCloseInfo.PROTOCOL_ERROR;
import static dev.scx.websocket.op_code.WebSocketOpCode.*;

/// ScxFrameWebSocketImplHelper
///
/// @author scx567888
/// @version 0.0.1
final class ScxFrameWebSocketImplHelper {

    private static void checkFramePayloadAndFin(WebSocketOpCode opCode, byte[] payloadData, boolean fin) throws WebSocketProtocolException {
        if (opCode == PING || opCode == PONG || opCode == CLOSE) {
            checkControlFramePayloadAndFin(opCode, payloadData, fin);
            // close 需要进一步校验.
            if (opCode == CLOSE) {
                checkCloseFramePayloadAndFin(opCode, payloadData, fin);
            }
        }
    }

    private static void checkControlFramePayloadAndFin(WebSocketOpCode opCode, byte[] payloadData, boolean fin) throws WebSocketProtocolException {
        if (!fin) {
            throw new WebSocketProtocolException(PROTOCOL_ERROR.code(), opCode + " frame must have fin = true");
        }
        if (payloadData.length > 125) {
            throw new WebSocketProtocolException(PROTOCOL_ERROR.code(), opCode + " frame payload length must be <= 125 bytes");
        }
    }

    private static void checkCloseFramePayloadAndFin(WebSocketOpCode opCode, byte[] payloadData, boolean fin) throws WebSocketProtocolException {
        if (payloadData.length == 1) {
            throw new WebSocketProtocolException(PROTOCOL_ERROR.code(), "close frame payload length must not be 1");
        }
    }

    /// 根据 WebSocketFrame 创建 WebSocketProtocolFrame. 要同时校验.
    public static WebSocketProtocolFrame toProtocolFrame(WebSocketFrame frame, boolean isClient) throws WebSocketProtocolException {
        // 校验 frame
        checkFramePayloadAndFin(frame.opCode(), frame.payloadData(), frame.fin());

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

    /// 转换的同时要校验.
    public static WebSocketFrame fromProtocolFrame(WebSocketProtocolFrame protocolFrame, boolean isClient) throws WebSocketProtocolException {
        // 此处在转换的同时需要进行校验

        // 1, 先校验 opCode 是不是合法的.
        var opCode = WebSocketOpCode.find(protocolFrame.opCode);
        if (opCode == null) {
            throw new WebSocketProtocolException(PROTOCOL_ERROR.code(), "unknown op code");
        }

        // 2, 校验所有的 rsv 是否全部为 false.
        if (protocolFrame.rsv1 || protocolFrame.rsv2 || protocolFrame.rsv3) {
            throw new WebSocketProtocolException(PROTOCOL_ERROR.code(), "unsupported rsv");
        }

        // 3, 校验 mask 和所属角色是否正确.
        // 注意这里的 isClient 表示的是接收方, 也就是说 对于帧来讲 应该是相反的角色
        // 我们无需校验 maskKey 因为这个是上层根据 protocolFrame.masked 读取出来的. 所以必然是正确的.
        if (isClient) {// 如果为 true 表达这个帧是 服务端发送的, masked 应为 false
            if (protocolFrame.masked) {
                throw new WebSocketProtocolException(PROTOCOL_ERROR.code(), "server can not has masked");
            }
        } else {
            if (!protocolFrame.masked) {
                throw new WebSocketProtocolException(PROTOCOL_ERROR.code(), "client must has masked");
            }
        }

        // 4, 校验控制帧. payload 长度 以及 fin.
        // 我们无需额外校验 payloadLength == payloadData,
        // 因为 payloadData 是上层根据 protocolFrame.payloadLength 读取出来的. 所以必然是正确的.
        checkFramePayloadAndFin(opCode, protocolFrame.payloadData, protocolFrame.fin);

        // 以上全部通过 表示 帧正确.

        // 5, 处理掩码.
        var masked = protocolFrame.masked;
        var maskingKey = protocolFrame.maskingKey;
        var payloadData = protocolFrame.payloadData;

        // 掩码计算, 此处就地计算.
        if (masked) {
            for (int i = 0; i < payloadData.length; i = i + 1) {
                payloadData[i] = (byte) (payloadData[i] ^ maskingKey[i % 4]);
            }
        }

        // 6, 包装为 WebSocketFrame 返回
        return new WebSocketFrame(opCode, payloadData, protocolFrame.fin);
    }

}
