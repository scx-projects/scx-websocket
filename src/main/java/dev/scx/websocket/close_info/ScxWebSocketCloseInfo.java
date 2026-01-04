package dev.scx.websocket.close_info;

/// WebSocket 关闭信息
///
/// @author scx567888
/// @version 0.0.1
public sealed interface ScxWebSocketCloseInfo permits ScxWebSocketCloseInfoImpl, WebSocketCloseInfo {

    static ScxWebSocketCloseInfo of(int code, String reason) {
        return new ScxWebSocketCloseInfoImpl(code, reason);
    }

    static ScxWebSocketCloseInfo ofPayload(byte[] payload) {
        return ScxWebSocketCloseInfoHelper.parseCloseInfo(payload);
    }

    int code();

    String reason();

    default byte[] toPayload() {
        return ScxWebSocketCloseInfoHelper.toClosePayload(this);
    }

}
