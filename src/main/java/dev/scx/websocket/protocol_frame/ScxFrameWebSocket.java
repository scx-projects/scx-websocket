package dev.scx.websocket.protocol_frame;

import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketProtocolException;
import dev.scx.websocket.frame.WebSocketFrame;

/// ScxFrameWebSocket
///
/// @author scx567888
/// @version 0.0.1
public interface ScxFrameWebSocket extends AutoCloseable {

    WebSocketFrame readFrame() throws WebSocketIOException, WebSocketProtocolException;

    void sendFrame(WebSocketFrame frame) throws WebSocketIOException, WebSocketProtocolException;

    /// 立即关闭底层连接并释放相关资源.
    @Override
    void close();

}
