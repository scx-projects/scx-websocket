package dev.scx.websocket.message;

import dev.scx.websocket.frame.ScxFrameWebSocket;
import dev.scx.websocket.frame.WebSocketFrame;
import dev.scx.websocket.op_code.WebSocketOpCode;
import dev.scx.websocket.close_info.WebSocketCloseInfo;
import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketInvalidStateException;
import dev.scx.websocket.exception.WebSocketProtocolException;

import java.io.ByteArrayOutputStream;

import static dev.scx.websocket.op_code.WebSocketOpCode.*;

// todo 未完成.
/// ScxMessageWebSocket
///
/// readFrame 会聚合 CONTINUATION.
public final class ScxMessageWebSocketImpl implements ScxMessageWebSocket {

    private final ScxFrameWebSocket scxWebSocket;
    private final int maxWebSocketMessageSize;

    // fragmentation state
    private boolean inFragment = false;
    private WebSocketOpCode messageOpCode = null; // TEXT or BINARY
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private long bufferedSize = 0;

    public ScxMessageWebSocketImpl(ScxFrameWebSocket scxWebSocket) {
        this(scxWebSocket, 1024 * 1024 * 16);// 默认 16 MB
    }

    public ScxMessageWebSocketImpl(ScxFrameWebSocket scxWebSocket, int maxWebSocketMessageSize) {
        this.scxWebSocket = scxWebSocket;
        this.maxWebSocketMessageSize = maxWebSocketMessageSize;
    }

    private void beginFragment(WebSocketOpCode opCode, byte[] payload) throws WebSocketProtocolException {
        this.inFragment = true;
        this.messageOpCode = opCode; // TEXT or BINARY
        this.buffer.reset();
        this.bufferedSize = 0;
        appendFragment(payload);
    }

    private void appendFragment(byte[] payload) throws WebSocketProtocolException {
        long nextSize = bufferedSize + payload.length;
        if (nextSize > maxWebSocketMessageSize) {
            resetFragmentState();
            throw new WebSocketProtocolException(WebSocketCloseInfo.TOO_BIG);
        }
        buffer.writeBytes(payload);
        bufferedSize = nextSize;
    }

    private void resetFragmentState() {
        inFragment = false;
        messageOpCode = null;
        buffer.reset();
        bufferedSize = 0;
    }

    @Override
    public WebSocketMessage readMessage() throws WebSocketIOException, WebSocketProtocolException {
        while (true) {
            WebSocketFrame f = this.scxWebSocket.readFrame();
            WebSocketOpCode op = f.opCode();

            // 控制帧：不吞，直接返回（并保留聚合状态）
            if (op == PING || op == PONG || op == CLOSE) {
                // 如果收到 CLOSE，连接即将结束；丢弃未完成聚合，避免内存泄漏
                if (op == CLOSE) {
                    resetFragmentState();
                }
                return new WebSocketMessage(null,null);
            }

            // 数据帧 / continuation
            if (!inFragment) {
                // 未处于分片中
                if (op == CONTINUATION) {
                    throw new WebSocketProtocolException(WebSocketCloseInfo.PROTOCOL_ERROR);
                }

                if (op == TEXT || op == BINARY) {
                    if (f.fin()) {
                        // 单帧完整消息：直接返回
                        return new WebSocketMessage(null,null);
                    } else {
                        // 开始分片：缓存首帧 payload，继续读取后续帧
                        beginFragment(op, f.payloadData());
                        // 注意：这里不返回，继续循环读下一帧；如果下一帧是控制帧会被原样返回
                        continue;
                    }
                }

                // 其他 opcode（如果你未来扩展）：按协议属于错误
                throw new WebSocketProtocolException(WebSocketCloseInfo.PROTOCOL_ERROR);

            } else {
                // 处于分片中：必须是 CONTINUATION，直到 fin=true 结束
                if (op == CONTINUATION) {
                    appendFragment(f.payloadData());

                    if (f.fin()) {
                        byte[] merged = buffer.toByteArray();
                        WebSocketOpCode finalOp = messageOpCode;
                        resetFragmentState();
                        // 合并后返回一个 fin=true 的 TEXT/BINARY
                        return new WebSocketMessage(null,null);
                    }

                    // 分片还没结束，继续读下一帧
                    continue;
                }

                // 分片中又出现 TEXT/BINARY（新消息起始） => 协议错误
                if (op == TEXT || op == BINARY) {
                    throw new WebSocketProtocolException(WebSocketCloseInfo.PROTOCOL_ERROR);
                }

                // 其他 opcode 也视为协议错误
                throw new WebSocketProtocolException(WebSocketCloseInfo.PROTOCOL_ERROR);
            }
        }
    }

    @Override
    public void sendMessage(WebSocketMessage message) throws WebSocketIOException, WebSocketInvalidStateException {
        this.scxWebSocket.sendFrame(null);
    }

    @Override
    public void close() {
        this.scxWebSocket.close();
    }

}
