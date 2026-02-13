package dev.scx.websocket.close_info;

import static java.nio.charset.StandardCharsets.UTF_8;

/// ScxWebSocketCloseInfoHelper
///
/// @author scx567888
/// @version 0.0.1
final class ScxWebSocketCloseInfoHelper {

    public static ScxWebSocketCloseInfo parseCloseInfo(byte[] frame) throws IllegalArgumentException {
        int len = frame.length;

        // 1, 允许空帧
        if (len == 0) {
            // 空 CloseInfo
            return ScxWebSocketCloseInfo.empty();
        }

        // 2, 否则至少 2 字节.
        if (len < 2) {
            throw new IllegalArgumentException("Invalid frame length: " + len);
        }

        // 3, 将前两字节 转换为 code
        int code = (frame[0] & 0b1111_1111) << 8 |
            frame[1] & 0b1111_1111;

        // 4, 读取关闭原因 (如果存在)
        String reason = null;
        if (len > 2) {
            reason = new String(frame, 2, len - 2, UTF_8);
        }
        return ScxWebSocketCloseInfo.of(code, reason);
    }

    public static byte[] toClosePayload(ScxWebSocketCloseInfo closeInfo) throws IllegalArgumentException {
        var code = closeInfo.code();
        var reason = closeInfo.reason();

        // 1, code 为空 可以认为 closeInfo 为空, 返回 空 payload
        if (code == null) {
            return new byte[0];
        }

        // 2, 计算 reasonBytes
        byte[] reasonBytes = reason != null ? reason.getBytes(UTF_8) : new byte[0];
        // 大小 不能超过 123 (125 - 2)
        if (reasonBytes.length > 123) {
            throw new IllegalArgumentException("Invalid reason length: " + reasonBytes.length);
        }

        // 3, 创建 payload.
        byte[] payload = new byte[2 + reasonBytes.length];

        // 4, 设置状态码
        payload[0] = (byte) (code >> 8);
        payload[1] = (byte) (code & 0b1111_1111);

        // 5, 设置关闭原因
        System.arraycopy(reasonBytes, 0, payload, 2, reasonBytes.length);

        return payload;
    }

}
