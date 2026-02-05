package dev.scx.websocket.close_info;

/// WebSocket 关闭信息
///
/// @author scx567888
/// @version 0.0.1
public sealed interface ScxWebSocketCloseInfo permits ScxWebSocketCloseInfoImpl, WebSocketCloseInfo {

    static ScxWebSocketCloseInfo of(int code) {
        return new ScxWebSocketCloseInfoImpl(code, null);
    }

    static ScxWebSocketCloseInfo of(int code, String reason) {
        return new ScxWebSocketCloseInfoImpl(code, reason);
    }

    static ScxWebSocketCloseInfo empty() {
        return new ScxWebSocketCloseInfoImpl(null, null);
    }

    static ScxWebSocketCloseInfo ofPayload(byte[] payload) {
        return ScxWebSocketCloseInfoHelper.parseCloseInfo(payload);
    }

    Integer code();

    String reason();

    default byte[] toPayload() {
        return ScxWebSocketCloseInfoHelper.toClosePayload(this);
    }

}
