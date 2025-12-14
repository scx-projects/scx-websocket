package dev.scx.websocket;

import dev.scx.websocket.close_info.ScxWebSocketCloseInfo;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static java.security.MessageDigest.getInstance;

/// WebSocketHelper
///
/// @author scx567888
/// @version 0.0.1
public final class WebSocketHelper {

    // 生成 Sec-WebSocket-Accept 的方法
    public static String generateSecWebSocketAccept(String key) {
        // 根据 WebSocket 协议生成接受密钥
        return Base64.getEncoder().encodeToString(sha1(key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"));
    }

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

    public static byte[] createClosePayload(int code, String reason) {
        byte[] reasonBytes = reason != null ? reason.getBytes(StandardCharsets.UTF_8) : new byte[0];
        byte[] payload = new byte[2 + reasonBytes.length];
        // 设置状态码
        payload[0] = (byte) (code >> 8);
        payload[1] = (byte) (code & 0b1111_1111);
        // 设置关闭原因
        System.arraycopy(reasonBytes, 0, payload, 2, reasonBytes.length);
        return payload;
    }

    public static byte[] sha1(String data) {
        try {
            var messageDigest = getInstance("SHA-1");
            messageDigest.update(data.getBytes());
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
