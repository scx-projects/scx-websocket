package dev.scx.websocket;

/// WebSocketOptions
///
/// @author scx567888
/// @version 0.0.1
public final class WebSocketOptions {

    // 最大单个 WebSocket 帧长度
    private int maxWebSocketFrameSize;

    public WebSocketOptions() {
        this.maxWebSocketFrameSize = 1024 * 1024 * 16; // 默认 16 MB
    }

    public WebSocketOptions(WebSocketOptions oldOptions) {
        maxWebSocketFrameSize(oldOptions.maxWebSocketFrameSize());
    }

    public int maxWebSocketFrameSize() {
        return maxWebSocketFrameSize;
    }

    public WebSocketOptions maxWebSocketFrameSize(int maxWebSocketFrameSize) {
        this.maxWebSocketFrameSize = maxWebSocketFrameSize;
        return this;
    }

}
