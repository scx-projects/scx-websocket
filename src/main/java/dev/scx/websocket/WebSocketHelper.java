package dev.scx.websocket;

import dev.scx.websocket.WebSocketFrame;

import static dev.scx.random.ScxRandom.randomBytes;

/// WebSocketHelper
///
/// @author scx567888
/// @version 0.0.1
public final class WebSocketHelper {

    /// 注意此处创建的 WebSocketProtocolFrame 中的 payloadData 还没有被掩码计算.
    public static WebSocketProtocolFrame toProtocolFrame(WebSocketFrame frame, boolean isClient) {

        var protocolFrame = new WebSocketProtocolFrame();
        protocolFrame.fin = frame.fin();
        protocolFrame.rsv1 = false;
        protocolFrame.rsv2 = false;
        protocolFrame.rsv3 = false;
        protocolFrame.opCode = frame.opCode();

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

}
