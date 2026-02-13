package dev.scx.websocket.message;

import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketInvalidStateException;
import dev.scx.websocket.exception.WebSocketProtocolException;
import dev.scx.websocket.frame.ScxFrameWebSocket;
import dev.scx.websocket.frame.WebSocketFrame;
import dev.scx.websocket.op_code.WebSocketOpCode;

import java.util.ArrayList;
import java.util.List;

import static dev.scx.websocket.close_info.WebSocketCloseInfo.*;
import static dev.scx.websocket.message.ScxMessageWebSocketImplHelper.frameToMessage;
import static dev.scx.websocket.message.ScxMessageWebSocketImplHelper.messageToFrame;
import static dev.scx.websocket.message.WebSocketMessageType.CLOSE;
import static dev.scx.websocket.message.WebSocketMessageType.PONG;

/// ScxMessageWebSocketImpl
///
/// @author scx567888
/// @version 0.0.1
public final class ScxMessageWebSocketImpl implements ScxMessageWebSocket {

    private final ScxFrameWebSocket frameWebSocket; // 帧级别 websocket
    private final long maxWebSocketMessageSize; // 允许的最大消息长度, 只约束接收端.
    private final List<WebSocketFrame> fragmentFrames; // 聚合中的帧
    private int fragmentPayloadLength; // 聚合中的帧总长度.
    private boolean inContinuation; // 是否处于聚合中
    private boolean closeSent; // 是否发送过 close 帧
    private boolean closeReceived; // 是否接收过 close 帧

    public ScxMessageWebSocketImpl(ScxFrameWebSocket frameWebSocket, long maxWebSocketMessageSize) {
        this.frameWebSocket = frameWebSocket;
        this.maxWebSocketMessageSize = maxWebSocketMessageSize;
        this.fragmentFrames = new ArrayList<>();
        this.fragmentPayloadLength = 0;
        this.inContinuation = false;
        this.closeSent = false;
        this.closeReceived = false;
    }

    @Override
    public WebSocketMessage readMessage() throws WebSocketIOException, WebSocketProtocolException {
        var frame = readFrameUntilLast();
        // 针对一些特殊的帧做处理.
        switch (frame.opCode()) {
            case CLOSE -> handleCloseFrame(frame);
            case PING -> handlePingFrame(frame);
        }
        return frameToMessage(frame);
    }

    @Override
    public void sendMessage(WebSocketMessage message) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        if (closeSent) { // 如果已经发送过 close 帧
            if (message.type() != WebSocketMessageType.CLOSE) { // 不允许发送非 close 的其他帧.
                throw new WebSocketInvalidStateException("Cannot send " + message.type() + " after CLOSE has been sent");
            } else { // 直接忽略
                return;
            }
        }

        if (closeReceived) { // 如果已经接收过 close 帧
            if (message.type() != WebSocketMessageType.CLOSE) { // 不允许发送非 close 的其他帧.
                throw new WebSocketInvalidStateException("Cannot send " + message.type() + " after CLOSE has been received");
            }
        }

        var frame = messageToFrame(message);

        frameWebSocket.sendFrame(frame);

        // 发送成功才算
        if (frame.opCode() == WebSocketOpCode.CLOSE) {
            closeSent = true;
        }

    }

    @Override
    public void close() {
        frameWebSocket.close();  // 这里有可能已经被远端关闭 我们忽略异常
    }

    /// 读取一个完整消息帧.
    private WebSocketFrame readFrameUntilLast() throws WebSocketIOException, WebSocketProtocolException {
        while (true) {
            // 读取帧.
            var frame = this.frameWebSocket.readFrame();
            switch (frame.opCode()) {
                // 控制帧直接返回.
                case PING, PONG, CLOSE -> {
                    return frame;
                }
                // TEXT 或 BINARY 帧.
                case TEXT, BINARY -> {
                    if (frame.fin()) {
                        if (inContinuation) {
                            throw new WebSocketProtocolException(PROTOCOL_ERROR.code(), "Received " + frame.opCode() + " frame while a fragmented message is in progress");
                        } else {
                            return frame;
                        }
                    } else {
                        if (inContinuation) {
                            throw new WebSocketProtocolException(PROTOCOL_ERROR.code(), "Received " + frame.opCode() + " frame while a fragmented message is in progress");
                        } else {
                            // 保存消息 继续下一次
                            inContinuation = true;
                            appendFragment(frame);
                        }
                    }
                }
                // CONTINUATION 帧.
                case CONTINUATION -> {
                    if (frame.fin()) {
                        if (inContinuation) {
                            appendFragment(frame);
                            // 合并最后帧
                            return buildFinalFrame();
                        } else {
                            throw new WebSocketProtocolException(PROTOCOL_ERROR.code(), "Unexpected CONTINUATION frame (no fragmented message in progress)");
                        }
                    } else {
                        if (inContinuation) {
                            appendFragment(frame);
                        } else {
                            throw new WebSocketProtocolException(PROTOCOL_ERROR.code(), "Unexpected CONTINUATION frame (no fragmented message in progress)");
                        }
                    }
                }
            }
        }
    }

    private void appendFragment(WebSocketFrame frame) throws WebSocketProtocolException {
        var framePayloadLength = frame.payloadData().length;
        // 检查合并后的消息大小限制
        if (this.fragmentPayloadLength + framePayloadLength > maxWebSocketMessageSize) {
            throw new WebSocketProtocolException(TOO_BIG.code(), "Message too big");
        }
        this.fragmentPayloadLength += framePayloadLength;
        this.fragmentFrames.add(frame);
    }

    private WebSocketFrame buildFinalFrame() {
        // 用起始帧的 opCode 作为最终的 opCode.
        var opCode = fragmentFrames.get(0).opCode();
        // 合并 payloadData
        var finalPayloadData = new byte[fragmentPayloadLength];

        int offset = 0;
        for (var frame : fragmentFrames) {
            var payloadData = frame.payloadData();
            System.arraycopy(payloadData, 0, finalPayloadData, offset, payloadData.length);
            offset += payloadData.length;
        }

        // 构建完成重置状态
        this.inContinuation = false;
        this.fragmentFrames.clear();
        this.fragmentPayloadLength = 0;

        return new WebSocketFrame(opCode, finalPayloadData, true);
    }

    private void handleCloseFrame(WebSocketFrame frame) {
        this.closeReceived = true;
        // 收到 Close, 立即回 Close
        try {
            sendMessage(new WebSocketMessage(CLOSE, NORMAL_CLOSE.toPayload())); // 这里有可能无法发送 我们忽略异常
        } catch (Exception _) {

        }
    }

    private void handlePingFrame(WebSocketFrame frame) {
        // 收到 Ping, 立即回 Pong
        try {
            sendMessage(new WebSocketMessage(PONG, frame.payloadData())); // 这里有可能无法发送 我们忽略异常
        } catch (Exception _) {

        }
    }

}
