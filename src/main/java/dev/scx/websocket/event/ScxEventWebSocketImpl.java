package dev.scx.websocket.event;

import dev.scx.websocket.close_info.ScxWebSocketCloseInfo;
import dev.scx.websocket.close_info.WebSocketCloseInfo;
import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketInvalidStateException;
import dev.scx.websocket.exception.WebSocketProtocolException;
import dev.scx.websocket.message.ScxMessageWebSocket;
import dev.scx.websocket.message.WebSocketMessage;

import java.lang.System.Logger;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.getLogger;

/// ScxEventWebSocket 默认实现
///
/// @author scx567888
/// @version 0.0.1
public final class ScxEventWebSocketImpl implements ScxEventWebSocket {

    private static final Logger LOGGER = getLogger(ScxEventWebSocketImpl.class.getName());

    private final ScxMessageWebSocket messageWebSocket;
    private final Executor callbackExecutor; // 回调执行器
    private Consumer<String> textHandler;
    private Consumer<byte[]> binaryHandler;
    private Consumer<byte[]> pingHandler;
    private Consumer<byte[]> pongHandler;
    private Consumer<ScxWebSocketCloseInfo> closeHandler;
    private Consumer<Throwable> errorHandler;
    private volatile boolean running;

    public ScxEventWebSocketImpl(ScxMessageWebSocket messageWebSocket) {
        this(messageWebSocket, null);
    }

    public ScxEventWebSocketImpl(ScxMessageWebSocket messageWebSocket, Executor callbackExecutor) {
        this.messageWebSocket = messageWebSocket;
        this.callbackExecutor = callbackExecutor;
        this.textHandler = null;
        this.binaryHandler = null;
        this.pingHandler = null;
        this.pongHandler = null;
        this.closeHandler = null;
        this.errorHandler = null;
        this.running = false;
    }

    @Override
    public ScxEventWebSocket onText(Consumer<String> textHandler) {
        this.textHandler = textHandler;
        return this;
    }

    @Override
    public ScxEventWebSocket onBinary(Consumer<byte[]> binaryHandler) {
        this.binaryHandler = binaryHandler;
        return this;
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
    public ScxEventWebSocket onClose(Consumer<ScxWebSocketCloseInfo> closeHandler) {
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
                // 尝试读取 帧
                var message = messageWebSocket.readMessage();
                // 处理帧
                _handleMessage(message);
            } catch (Exception e) {
                _handleException(e);
            }
        }
    }

    @Override
    public void sendMessage(WebSocketMessage message) throws WebSocketIOException, WebSocketInvalidStateException, WebSocketProtocolException {
        this.messageWebSocket.sendMessage(message);
    }

    @Override
    public void close() {
        // 关于主动调用 close 如何触发 onClose
        // close 会打断阻塞的 start 循环中的 readMessage, 同时触发异常路径. 间接调用 onClose.
        this.messageWebSocket.close();
    }

    private void _handleException(Exception exception) {
        // 1, 调用错误处理器.
        _handleError(exception);

        // 根据 不同的错误类型 创建 closeInfo
        ScxWebSocketCloseInfo localCloseInfo;
        ScxWebSocketCloseInfo peerCloseInfo;
        if (exception instanceof WebSocketProtocolException we) {
            // 此处假设 we.getMessage() 永远小于 125 字节.
            localCloseInfo = ScxWebSocketCloseInfo.of(we.closeCode(), we.getMessage());
            peerCloseInfo = ScxWebSocketCloseInfo.of(we.closeCode(), we.getMessage());
        } else {
            localCloseInfo = WebSocketCloseInfo.CLOSED_ABNORMALLY;
            peerCloseInfo = WebSocketCloseInfo.GOING_AWAY;
        }

        // 2, 调用 close 处理器.
        try {
            _callOnClose(localCloseInfo);
        } catch (Exception e) {
            LOGGER.log(ERROR, "Error while call onClose : ", e);
        }

        // 3, 发送关闭响应帧
        try {
            sendClose(peerCloseInfo); // 这里有可能无法发送 我们忽略异常
        } catch (Exception _) {

        }

        // 4, 终止连接
        close();

        running = false;
    }

    private void _handleMessage(WebSocketMessage message) {
        switch (message.type()) {
            case TEXT -> _handleText(message);
            case BINARY -> _handleBinary(message);
            case PING -> _handlePing(message);
            case PONG -> _handlePong(message);
            case CLOSE -> _handleClose(message);
        }
    }

    //******************* _handleXXX **********************

    private void _handleText(WebSocketMessage frame) {
        try {
            _callOnText(new String(frame.payloadData(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.log(ERROR, "Error while calling onTextMessage : ", e);
        }
    }

    private void _handleBinary(WebSocketMessage frame) {
        try {
            _callOnBinary(frame.payloadData());
        } catch (Exception e) {
            LOGGER.log(ERROR, "Error while call onBinaryMessage : ", e);
        }
    }

    private void _handlePing(WebSocketMessage frame) {
        try {
            _callOnPing(frame.payloadData());
        } catch (Exception e) {
            LOGGER.log(ERROR, "Error while call onPing : ", e);
        }
    }

    private void _handlePong(WebSocketMessage frame) {
        try {
            _callOnPong(frame.payloadData());
        } catch (Exception e) {
            LOGGER.log(ERROR, "Error while call onPong : ", e);
        }
    }

    private void _handleClose(WebSocketMessage frame) {
        try {
            _callOnClose(ScxWebSocketCloseInfo.ofPayload(frame.payloadData()));
        } catch (Exception e) {
            LOGGER.log(ERROR, "Error while call onClose : ", e);
        }

        // 终止连接
        close();

        running = false;
    }

    private void _handleError(Exception e) {
        try {
            _callOnError(e);
        } catch (Exception ex) {
            LOGGER.log(ERROR, "Error while call onError : ", ex);
        }
    }

    //******************* _callOnXXX **********************

    private void _callOnText(String text) {
        if (textHandler == null) {
            return;
        }
        if (callbackExecutor == null) {
            textHandler.accept(text);
        } else {
            callbackExecutor.execute(() -> textHandler.accept(text));
        }
    }

    private void _callOnBinary(byte[] binary) {
        if (binaryHandler == null) {
            return;
        }
        if (callbackExecutor == null) {
            binaryHandler.accept(binary);
        } else {
            callbackExecutor.execute(() -> binaryHandler.accept(binary));
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

    private void _callOnClose(ScxWebSocketCloseInfo closeInfo) {
        if (closeHandler == null) {
            return;
        }
        if (callbackExecutor == null) {
            closeHandler.accept(closeInfo);
        } else {
            callbackExecutor.execute(() -> closeHandler.accept(closeInfo));
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

}
