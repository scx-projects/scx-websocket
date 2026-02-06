package dev.scx.websocket.event;

import dev.scx.websocket.ScxFrameWebSocket;
import dev.scx.websocket.WebSocketFrame;
import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketInvalidStateException;
import dev.scx.websocket.exception.WebSocketProtocolException;

import java.lang.System.Logger;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static dev.scx.websocket.close_info.WebSocketCloseInfo.*;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.getLogger;

// todo 这个类 待重构.

/// ScxEventWebSocket 默认实现
///
/// @author scx567888
/// @version 0.0.1
class ScxEventWebSocketImpl implements ScxEventWebSocket {

    private static final Logger LOGGER = getLogger(ScxEventWebSocketImpl.class.getName());

    private final ScxFrameWebSocket ws;
    private final Executor callbackExecutor;// 回调执行器
    protected ContinuationType continuationType;
    private TextMessageHandler textMessageHandler;
    private BinaryMessageHandler binaryMessageHandler;
    private Consumer<byte[]> pingHandler;
    private Consumer<byte[]> pongHandler;
    private CloseHandler closeHandler;
    private Consumer<Throwable> errorHandler;
    private boolean running;

    public ScxEventWebSocketImpl(ScxFrameWebSocket websocket) {
        this(websocket, null);
    }

    public ScxEventWebSocketImpl(ScxFrameWebSocket websocket, Executor callbackExecutor) {
        this.ws = websocket;
        this.callbackExecutor = callbackExecutor;
        this.textMessageHandler = null;
        this.binaryMessageHandler = null;
        this.pingHandler = null;
        this.pongHandler = null;
        this.closeHandler = null;
        this.errorHandler = null;
        this.running = false;
    }

    @Override
    public WebSocketFrame readFrame() throws WebSocketIOException, WebSocketProtocolException {
        return ws.readFrame();
    }

    @Override
    public void sendFrame(WebSocketFrame frame) throws WebSocketIOException, WebSocketInvalidStateException {
        ws.sendFrame(frame);
    }

    @Override
    public void close() {
        ws.close();
    }

    @Override
    public ScxEventWebSocket onTextMessage(TextMessageHandler textMessageHandler) {
        this.textMessageHandler = textMessageHandler;
        return this;
    }

    @Override
    public ScxEventWebSocket onBinaryMessage(BinaryMessageHandler binaryMessageHandler) {
        this.binaryMessageHandler = binaryMessageHandler;
        return this;
    }

    @Override
    public ScxEventWebSocket onContinuation(BinaryMessageHandler binaryMessageHandler) {
        return null;
    }

    @Override
    public ScxEventWebSocket onPing(Consumer<byte[]> pingHandler) {
        this.pingHandler = pingHandler;
        return this;
    }

    @Override
    public ScxEventWebSocket onPong(Consumer<byte[]> pongHandler) {
        this.pongHandler = pongHandler;
        return this;
    }

    @Override
    public ScxEventWebSocket onClose(CloseHandler closeHandler) {
        this.closeHandler = closeHandler;
        return this;
    }

    @Override
    public ScxEventWebSocket onError(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        running = true;
        while (running) {
            try {
                //尝试读取 帧
                var frame = readFrame();
                //处理帧
                handleFrame(frame);
            } catch (WebSocketException e) {
//                _handleCloseByException(NORMAL_CLOSE.code(), NORMAL_CLOSE.reason(), e.closeCode(), e.getMessage());
                _handleCloseByException(NORMAL_CLOSE.code(), NORMAL_CLOSE.reason(), 1001, e.getMessage());
            } catch (Exception e) {
                _handleError(e);
//                _handleCloseByException(CLOSED_ABNORMALLY.code(), CLOSED_ABNORMALLY.reason(), UNEXPECTED_CONDITION.code(), e.getMessage());
            }
        }
    }

    @Override
    public void stop() {
        this.running = false;
    }

    private void handleFrame(WebSocketFrame frame) throws WebSocketException {
        switch (frame.opCode()) {
            case CONTINUATION -> _handleContinuation(frame);
            case TEXT -> _handleText(frame);
            case BINARY -> _handleBinary(frame);
            case PING -> _handlePing(frame);
            case PONG -> _handlePong(frame);
            case CLOSE -> _handleClose(frame);
        }
    }

    private void _handleContinuation(WebSocketFrame frame) throws WebSocketException {
        boolean finalFrame = frame.fin();
        var ct = continuationType;
        if (finalFrame) {
            continuationType = ContinuationType.NONE;
        }
        switch (ct) {
            case TEXT -> {
                try {
                    _callOnTextMessage(new String(frame.payloadData()), finalFrame);
                } catch (Exception e) {
                    LOGGER.log(ERROR, "Error while calling onTextMessage", e);
                }
            }
            case BINARY -> {
                try {
                    _callOnBinaryMessage(frame.payloadData(), finalFrame);
                } catch (Exception e) {
                    LOGGER.log(ERROR, "Error while calling onBinaryMessage", e);
                }
            }
            default -> {
                throw new WebSocketException(PROTOCOL_ERROR.code() + "Unexpected continuation received");
            }
        }
    }

    private void _handleText(WebSocketFrame frame) {
        continuationType = ContinuationType.TEXT;
        try {
            _callOnTextMessage(new String(frame.payloadData()), frame.fin());
        } catch (Exception e) {
            LOGGER.log(ERROR, "Error while calling onTextMessage : ", e);
        }
    }

    private void _handleBinary(WebSocketFrame frame) {
        continuationType = ContinuationType.BINARY;
        try {
            _callOnBinaryMessage(frame.payloadData(), frame.fin());
        } catch (Exception e) {
            LOGGER.log(ERROR, "Error while call onBinaryMessage : ", e);
        }
    }

    private void _handlePing(WebSocketFrame frame) {
        try {
            _callOnPing(frame.payloadData());
        } catch (Exception e) {
            LOGGER.log(ERROR, "Error while call onPing : ", e);
        }
    }

    private void _handlePong(WebSocketFrame frame) {
        try {
            _callOnPong(frame.payloadData());
        } catch (Exception e) {
            LOGGER.log(ERROR, "Error while call onPong : ", e);
        }
    }

    private void _handleClose(WebSocketFrame frame) {
//        var closeInfo = frame.getCloseInfo();
//        _handleCloseByFrame(closeInfo.code(), closeInfo.reason(), NORMAL_CLOSE.code(), NORMAL_CLOSE.reason());
    }

    public void _handleCloseByFrame(int code, String reason, int peerCode, String peerReason) {
        //1, 调用用户处理器
        try {
            _callOnClose(code, reason);
        } catch (Exception e) {
            LOGGER.log(ERROR, "Error while call onClose : ", e);
        }
        //4, 停止监听
        stop();
    }

    public void _handleCloseByException(int code, String reason, int peerCode, String peerReason) {
        //1, 调用用户处理器
        try {
            _callOnClose(code, reason);
        } catch (Exception e) {
            LOGGER.log(ERROR, "Error while call onClose : ", e);
        }
        //2, 发送关闭响应帧
        try {
            sendClose(peerCode, peerReason); // 这里有可能无法发送 我们忽略异常
        } catch (Exception _) {

        }
        //3, 关闭 socket
        ws.close();
        //4, 停止监听
        stop();
    }

    private void _handleError(Exception e) {
        try {
            _callOnError(e);
        } catch (Exception ex) {
            LOGGER.log(ERROR, "Error while call onError : ", ex);
        }
    }

    private void _callOnTextMessage(String text, boolean last) {
        if (textMessageHandler == null) {
            return;
        }
        if (callbackExecutor == null) {
            textMessageHandler.handle(text, last);
        } else {
            callbackExecutor.execute(() -> textMessageHandler.handle(text, last));
        }
    }

    private void _callOnBinaryMessage(byte[] binary, boolean last) {
        if (binaryMessageHandler == null) {
            return;
        }
        if (callbackExecutor == null) {
            binaryMessageHandler.handle(binary, last);
        } else {
            callbackExecutor.execute(() -> binaryMessageHandler.handle(binary, last));
        }
    }

    private void _callOnPing(byte[] bytes) {
        if (pingHandler == null) {
            return;
        }
        if (callbackExecutor == null) {
            pingHandler.accept(bytes);
        } else {
            callbackExecutor.execute(() -> pingHandler.accept(bytes));
        }
    }

    private void _callOnPong(byte[] bytes) {
        if (pongHandler == null) {
            return;
        }
        if (callbackExecutor == null) {
            pongHandler.accept(bytes);
        } else {
            callbackExecutor.execute(() -> pongHandler.accept(bytes));
        }
    }

    private void _callOnClose(int code, String reason) {
        if (closeHandler == null) {
            return;
        }
        if (callbackExecutor == null) {
            closeHandler.handle(code, reason);
        } else {
            callbackExecutor.execute(() -> closeHandler.handle(code, reason));
        }
    }

    private void _callOnError(Exception e) {
        if (errorHandler == null) {
            return;
        }
        if (callbackExecutor == null) {
            errorHandler.accept(e);
        } else {
            callbackExecutor.execute(() -> errorHandler.accept(e));
        }
    }

    public enum ContinuationType {
        NONE,
        TEXT,
        BINARY
    }

}
