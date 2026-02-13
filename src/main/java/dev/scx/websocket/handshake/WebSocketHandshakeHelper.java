package dev.scx.websocket.handshake;

import java.util.Base64;

import static dev.scx.digest.ScxDigest.sha1;
import static dev.scx.random.ScxRandom.randomBytes;

/// WebSocketHandshakeHelper
///
/// @author scx567888
/// @version 0.0.1
public final class WebSocketHandshakeHelper {

    /// 生成 Sec-Websocket-Key, 一般用于客户端.
    public static String generateSecWebsocketKey() {
        return Base64.getEncoder().encodeToString(randomBytes(16));
    }

    /// 计算 Sec-WebSocket-Accept, 一般用于服务端.
    public static String computeSecWebSocketAccept(String secWebsocketKey) {
        return Base64.getEncoder().encodeToString(sha1(secWebsocketKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"));
    }

}
