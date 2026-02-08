package dev.scx.websocket.frame;

import dev.scx.io.endpoint.ByteEndpoint;
import dev.scx.io.exception.*;
import dev.scx.websocket.WebSocketOptions;
import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketProtocolException;
import dev.scx.websocket.protocol_frame.WebSocketProtocolFrame;

import java.util.concurrent.locks.ReentrantLock;

import static dev.scx.websocket.close_info.WebSocketCloseInfo.TOO_BIG;
import static dev.scx.websocket.frame.WebSocketFrameHelper.fromProtocolFrame;
import static dev.scx.websocket.frame.WebSocketFrameHelper.toProtocolFrame;
import static dev.scx.websocket.protocol_frame.WebSocketProtocolFrameHelper.*;

/// ScxFrameWebSocketImpl
///
/// @author scx567888
/// @version 0.0.1
public final class ScxFrameWebSocketImpl implements ScxFrameWebSocket {

    private final ByteEndpoint endpoint;
    private final WebSocketOptions options;
    // 为了防止底层的 ByteOutput 被乱序写入 此处需要加锁
    private final ReentrantLock lock;
    // 是否是客户端 客户端需要加掩码
    private final boolean isClient;

    public ScxFrameWebSocketImpl(ByteEndpoint endpoint, WebSocketOptions options, boolean isClient) {
        this.endpoint = endpoint;
        this.options = options;
        this.lock = new ReentrantLock();
        this.isClient = isClient;
    }

    @Override
    public WebSocketFrame readFrame() throws WebSocketIOException, WebSocketProtocolException {
        WebSocketProtocolFrame protocolFrame;
        try {
            // 读取 协议帧 头
            protocolFrame = readProtocolFrameHeader(endpoint.in());
            // 太大抛异常.
            if (protocolFrame.payloadLength > options.maxWebSocketFrameSize()) {
                throw new WebSocketProtocolException(TOO_BIG);
            }
            // 读取 body
            readProtocolFramePayload(protocolFrame, endpoint.in());
        } catch (NoMoreDataException | ScxInputException | InputAlreadyClosedException e) {
            throw new WebSocketIOException(e);
        }

        return fromProtocolFrame(protocolFrame, isClient);
    }

    @Override
    public void sendFrame(WebSocketFrame frame) throws WebSocketIOException {
        var protocolFrame = toProtocolFrame(frame, isClient);

        // 这里需要 锁.
        lock.lock();
        try {
            writeProtocolFrame(protocolFrame, endpoint.out());
        } catch (ScxOutputException | OutputAlreadyClosedException e) {
            throw new WebSocketIOException(e);
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void close() {
        try {
            endpoint.close();  // 这里有可能已经被远端关闭 我们忽略异常
        } catch (Exception _) {

        }
    }

}
