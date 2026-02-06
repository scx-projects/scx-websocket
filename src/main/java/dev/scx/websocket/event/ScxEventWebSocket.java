package dev.scx.websocket.event;

import dev.scx.websocket.frame.ScxFrameWebSocket;
import dev.scx.websocket.message.ScxMessageWebSocket;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/// 事件形式的 websocket (本质是另一种风格的用法)
///
/// @author scx567888
/// @version 0.0.1
public interface ScxEventWebSocket  {

    static ScxEventWebSocket of(ScxFrameWebSocket scxWebSocket) {
        return new ScxEventWebSocketImpl(scxWebSocket);
    }

    /// 为了防止 回调之间互相阻塞 可以传递一个 callbackExecutor 来处理每个 callback
    static ScxEventWebSocket of(ScxFrameWebSocket scxWebSocket, Executor callbackExecutor) {
        return new ScxEventWebSocketImpl(scxWebSocket, callbackExecutor);
    }

    ScxEventWebSocket onTextMessage(TextMessageHandler textMessageHandler);

    ScxEventWebSocket onBinaryMessage(BinaryMessageHandler binaryMessageHandler);

    ScxEventWebSocket onContinuation(BinaryMessageHandler binaryMessageHandler);

    ScxEventWebSocket onPing(Consumer<byte[]> pingHandler);

    ScxEventWebSocket onPong(Consumer<byte[]> pongHandler);

    ScxEventWebSocket onClose(CloseHandler closeHandler);

    ScxEventWebSocket onError(Consumer<Throwable> errorHandler);

    /// 以上回调设置完成之后调用以便启动 websocket 监听 (这个方法是 阻塞的)
    void start();

    /// 终止监听
    void stop();

}
