package dev.scx.websocket.close_info;

/// WebSocket 关闭信息
///
/// @author scx567888
/// @version 0.0.1
public interface ScxWebSocketCloseInfo {

    static ScxWebSocketCloseInfo of(int code, String reason) {
        return new ScxWebSocketCloseInfoImpl(code, reason);
    }

    int code();

    String reason();

}
