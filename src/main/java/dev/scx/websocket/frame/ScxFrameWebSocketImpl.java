package dev.scx.websocket.frame;

import dev.scx.io.endpoint.ByteEndpoint;
import dev.scx.io.exception.*;
import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketProtocolException;

import java.util.concurrent.locks.ReentrantLock;

import static dev.scx.websocket.close_info.WebSocketCloseInfo.TOO_BIG;
import static dev.scx.websocket.frame.ScxFrameWebSocketImplHelper.fromProtocolFrame;
import static dev.scx.websocket.frame.ScxFrameWebSocketImplHelper.toProtocolFrame;
import static dev.scx.websocket.frame.WebSocketProtocolFrameHelper.*;

/// ScxFrameWebSocketImpl
///
/// @author scx567888
/// @version 0.0.1
public final class ScxFrameWebSocketImpl implements ScxFrameWebSocket {

    private final ByteEndpoint endpoint; // 端点
    private final boolean isClient; // 是否是客户端 客户端需要加掩码
    private final long maxWebSocketFrameSize; // 最大 帧大小. 只约束接受帧.
    private final ReentrantLock lock; // 为了防止底层的 ByteOutput 被乱序写入 此处需要加锁

    public ScxFrameWebSocketImpl(ByteEndpoint endpoint, boolean isClient, long maxWebSocketFrameSize) {
        this.endpoint = endpoint;
        this.isClient = isClient;
        this.maxWebSocketFrameSize = maxWebSocketFrameSize;
        this.lock = new ReentrantLock();
    }

    @Override
    public WebSocketFrame readFrame() throws WebSocketIOException, WebSocketProtocolException {
        WebSocketProtocolFrame protocolFrame;
        try {
            // 读取 协议帧 头
            protocolFrame = readProtocolFrameHeader(endpoint.in());
            // 太大抛异常.
            if (protocolFrame.payloadLength > maxWebSocketFrameSize) {
                throw new WebSocketProtocolException(TOO_BIG.code(), "frame too large");
            }
            // 读取 body
            readProtocolFramePayload(protocolFrame, endpoint.in());
        } catch (NoMoreDataException | ScxInputException | InputAlreadyClosedException e) {
            throw new WebSocketIOException(e);
        }

        // fromProtocolFrame 中会进行 协议级别的单帧校验.
        return fromProtocolFrame(protocolFrame, isClient);
    }

    @Override
    public void sendFrame(WebSocketFrame frame) throws WebSocketIOException, WebSocketProtocolException {
        // toProtocolFrame 中会进行 协议级别的单帧校验.
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
