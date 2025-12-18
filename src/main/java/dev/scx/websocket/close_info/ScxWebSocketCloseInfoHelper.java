package dev.scx.websocket.close_info;

import java.nio.charset.StandardCharsets;

/// ScxWebSocketCloseInfoHelper
///
/// @author scx567888
/// @version 0.0.1
final class ScxWebSocketCloseInfoHelper {

    public static ScxWebSocketCloseInfo parseCloseInfo(byte[] frame) {
        int len = frame.length;
        int code = 1005; // 默认值（表示没有状态码）
        // 读取状态码（如果存在）
        if (len >= 2) {
            code = (frame[0] & 0b1111_1111) << 8 |
                    frame[1] & 0b1111_1111;
        } // 读取关闭原因（如果存在）
        String reason = null;
        if (len > 2) {
            reason = new String(frame, 2, len - 2, StandardCharsets.UTF_8);
        }
        return ScxWebSocketCloseInfo.of(code, reason);
    }

    public static byte[] toClosePayload(ScxWebSocketCloseInfo closeInfo) {
        var code = closeInfo.code();
        var reason = closeInfo.reason();
        byte[] reasonBytes = reason != null ? reason.getBytes(StandardCharsets.UTF_8) : new byte[0];
        byte[] payload = new byte[2 + reasonBytes.length];
        // 设置状态码
        payload[0] = (byte) (code >> 8);
        payload[1] = (byte) (code & 0b1111_1111);
        // 设置关闭原因
        System.arraycopy(reasonBytes, 0, payload, 2, reasonBytes.length);
        return payload;
    }

}
