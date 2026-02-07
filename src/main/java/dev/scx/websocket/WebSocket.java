package dev.scx.websocket;

import dev.scx.http.x.SocketIO;
import dev.scx.websocket.ScxWebSocket;
import dev.scx.websocket.WebSocketFrame;
import dev.scx.websocket.close_info.ScxWebSocketCloseInfo;
import dev.scx.websocket.exception.WebSocketException;

import java.util.concurrent.locks.ReentrantLock;

import static dev.scx.websocket.WebSocketOpCode.CLOSE;
import static dev.scx.websocket.x.WebSocketHelper.toProtocolFrame;
import static dev.scx.websocket.x.WebSocketProtocolFrameHelper.readProtocolFrame;
import static dev.scx.websocket.x.WebSocketProtocolFrameHelper.writeProtocolFrame;

/// WebSocket
///
/// @author scx567888
/// @version 0.0.1
public class WebSocket implements ScxWebSocket {

    private final SocketIO socketIO;
    private final WebSocketOptions options;
    // 为了防止底层的 ByteOutput 被乱序写入 此处需要加锁
    private final ReentrantLock lock;
    // 是否是客户端 客户端需要加掩码
    private final boolean isClient;
    // 限制只发送一次 close 帧
    protected boolean closeSent;

    public WebSocket(SocketIO socketIO, WebSocketOptions options, boolean isClient) {
        this.socketIO = socketIO;
        this.options = options;
        this.lock = new ReentrantLock();
        this.isClient = isClient;
    }

    @Override
    public WebSocketFrame readFrame() throws WebSocketException {
        try {
            // 读取 协议帧
            var protocolFrame = readProtocolFrame(socketIO.in, options.maxWebSocketFrameSize());
            // 当我们接收到了 close 帧, 我们应该发送 close 帧并关闭.
            if (protocolFrame.opCode == CLOSE) {
                handleCloseFrame(protocolFrame);
            }
            return WebSocketFrame.of(protocolFrame.opCode, protocolFrame.payloadData, protocolFrame.fin);
        } catch (Throwable e) {
            throw new WebSocketException("读取错误.");
        }
    }

    @Override
    public void sendFrame(WebSocketFrame frame) throws WebSocketException {
        if (socketIO.tcpSocket.isClosed()) {
            throw new WebSocketException("Cannot send frame: WebSocket is already closed");
        }

        if (closeSent) {
            if (frame.opCode() == CLOSE) { // 允许 用户多次发送 close 我们直接忽略
                return;
            } else {// 其余则抛出异常
                throw new WebSocketException("Cannot send non-close frames after a Close frame has been sent");
            }
        }

        var protocolFrame = toProtocolFrame(frame, isClient);

        // 这里需要 锁.
        lock.lock();
        try {
            writeProtocolFrame(protocolFrame, socketIO.out);
        } finally {
            lock.unlock();
        }

        // 发送成功才算.
        if (frame.opCode() == CLOSE) {
            closeSent = true;
        }
    }

    @Override
    public void close() {
        socketIO.closeQuietly();  // 这里有可能已经被远端关闭 我们忽略异常
    }

    public void handleCloseFrame(WebSocketProtocolFrame protocolFrame) {
        var closeInfo = ScxWebSocketCloseInfo.ofPayload(protocolFrame.payloadData);
        // 1, 发送关闭响应帧
        try {
            sendClose(closeInfo); // 这里有可能无法发送 我们忽略异常
        } catch (Exception _) {

        }
        // 2, 关闭底层 tcp 连接
        this.close();
    }

}
