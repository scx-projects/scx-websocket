package dev.scx.websocket.event;

import dev.scx.websocket.*;
import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketInvalidStateException;
import dev.scx.websocket.exception.WebSocketProtocolException;
import dev.scx.websocket.frame.ScxFrameWebSocket;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static dev.scx.websocket.WebSocketCloseInfo.NORMAL_CLOSE;

/// 事件形式的 WebSocket 接口 (本质是另一种风格的 ScxWebSocket 用法)
///
/// ScxEventWebSocket 是 ScxWebSocket 的事件式 facade, 用于将 read() 循环转换为
/// onText / onBinary / onPing / onPong / onClose / onError 等回调。
///
/// 本接口刻意不继承 ScxWebSocket。
///
/// ScxWebSocket 是 pull 模型, 用户通过 read() 主动读取下一个消息:
///
/// ```java
/// var message = webSocket.read();
/// ```
///
/// ScxEventWebSocket 是 push 模型, 实现内部会在 start() 中持续读取消息并分发给回调:
///
/// ```java
/// eventWebSocket.onText(...).start();
/// ```
///
/// 如果 ScxEventWebSocket 继承 ScxWebSocket, read() 将会暴露给事件模型用户。
/// 这会允许用户在事件循环运行期间手动调用 read(), 从而导致用户代码和事件循环同时消费
/// 同一个底层输入流, 破坏消息分发的一致性。
///
/// 因此, ScxEventWebSocket 应该组合 ScxWebSocket, 而不是继承 ScxWebSocket。
/// 它可以暴露 send(...) / sendClose(...) 等发送能力, 但不应该暴露 read()。
///
/// 本接口中的 send(String)、send(byte[])、sendPing(...)、sendPong(...)、sendClose(...)
/// 等便利方法会和 ScxWebSocket 中的同名方法保持少量重复。
/// 这是刻意的 API 取舍。
///
/// 不要为了消除这些简单转发方法的重复而抽取某种 "可发送能力" 接口, 如 WebSocketSender。
///
/// 这种接口看似可以复用 send(String)、send(byte[])、sendPing(...)、sendPong(...)、sendClose(...) 等默认方法,
/// 但它会把 "只能发送" 这一部分能力提升成 public API。
///
/// 一旦 WebSocketSender 成为 public 类型, 用户就可以把它作为方法参数、字段类型或返回值使用:
///
/// ```java
/// void broadcast(Collection<WebSocketSender> sockets) { ... }
/// ```
///
/// 这会产生一个不完整的 WebSocket 抽象:
///
/// - 它能发送, 但不能 read().
/// - 它能 sendClose(), 但不能 close().
/// - 它看起来像一个 WebSocket 角色, 但无法表达 ScxWebSocket 的 pull 模型.
/// - 它也无法表达 ScxEventWebSocket 的 push 模型.
/// - 它会让用户在 ScxWebSocket / ScxEventWebSocket / WebSocketSender 三个类型之间选择。
///
/// 因此, WebSocketSender 不是本库想暴露的领域概念。
/// 这里接受少量 facade convenience 方法重复, 是为了避免制造一个不完整但可见、可依赖的 public API 类型。
///
/// @author scx567888
public interface ScxEventWebSocket extends AutoCloseable {

    static ScxEventWebSocket of(ScxWebSocket messageWebSocket) {
        return new ScxEventWebSocketImpl(messageWebSocket);
    }

    /// 为了防止 回调之间互相阻塞 可以传递一个 callbackExecutor 来处理每个 callback
    static ScxEventWebSocket of(ScxWebSocket messageWebSocket, Executor callbackExecutor) {
        return new ScxEventWebSocketImpl(messageWebSocket, callbackExecutor);
    }

    ScxEventWebSocket onText(Consumer<String> textHandler);

    ScxEventWebSocket onBinary(Consumer<byte[]> binaryHandler);

    ScxEventWebSocket onPing(Consumer<byte[]> pingHandler);

    ScxEventWebSocket onPong(Consumer<byte[]> pongHandler);

    ScxEventWebSocket onClose(Consumer<WebSocketCloseInfo> closeHandler);

    ScxEventWebSocket onError(Consumer<Throwable> errorHandler);

    void send(WebSocketMessage message) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException;

    /// 以上回调设置完成之后调用以便启动 websocket 监听 (这个方法是 阻塞的)
    void start();

    /// close 语义等同于 [ScxFrameWebSocket#close()]
    @Override
    void close();

    default void send(String text) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        send(new TextMessage(text));
    }

    default void send(byte[] binary) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        send(new BinaryMessage(binary));
    }

    default void sendPing(byte[] data) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        send(new PingMessage(data));
    }

    default void sendPong(byte[] data) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        send(new PongMessage(data));
    }

    default void sendClose(WebSocketCloseInfo closeInfo) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        send(new CloseMessage(closeInfo));
    }

    default void sendClose(int code, String reason) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        sendClose(new WebSocketCloseInfo(code, reason));
    }

    default void sendClose() throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException {
        sendClose(NORMAL_CLOSE);
    }

}
