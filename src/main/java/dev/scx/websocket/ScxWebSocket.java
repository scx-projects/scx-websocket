package dev.scx.websocket;

import dev.scx.io.endpoint.ByteEndpoint;
import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketInvalidStateException;
import dev.scx.websocket.exception.WebSocketProtocolException;
import dev.scx.websocket.frame.ScxFrameWebSocket;

import static dev.scx.websocket.WebSocketCloseInfo.NORMAL_CLOSE;

/// ScxWebSocket
///
/// 消息级别的 websocket 接口.
/// 内部会组合帧或者分帧发送.
/// 内部会处理 Close 帧的协议约束, 例如收到 Close 后自动回复 Close.
///
/// 本接口刻意不提供 state() / isOpen() / isClosed() / isAlive() 等状态查询方法.
///
/// 原因是 WebSocket 是一个长期存在的双工连接, 本地对象能够给出的状态只能表示
/// "当前已经观察到的本地事实" , 而不能可靠表示远端连接的真实状态.
///
/// 例如, 即使某个状态查询方法返回 OPEN, 远端也可能已经断开、网络也可能已经失效,
/// 只是本地尚未通过 read() / send() / 超时检测观察到该事实.
/// 因此这类状态查询很容易诱导用户写出 check-then-act 代码：
/// ```java
/// if (webSocket.isOpen()) {
///    webSocket.send("message");
/// }
/// ```
/// 但这样的判断并不能保证随后的 send() 一定成功.
///
/// 因此, ScxWebSocket 采用 "动作即判断" 的模型：
///
/// - read() 成功返回消息, 失败抛出 WebSocketIOException 或 WebSocketProtocolException.
/// - send(...) 成功表示消息已交给底层发送, 失败通过异常表达.
/// - 如果当前本地协议状态已经不允许继续发送普通消息, 则抛出 WebSocketInvalidStateException.
/// - sendClose(...) 表示发送 WebSocket Close 帧.
/// - close() 只表示释放本地资源, 不表示远端仍然存活或已经完成协议关闭.
///
/// closeSent / closeReceived / closed 等状态仍然会在实现内部维护,
/// 但它们只用于约束协议行为, 不作为 public API 暴露.
///
/// 如果上层需要判断一批 WebSocket 是否 "仍然活着" , 应基于 Ping/Pong、最近读写时间、
/// 超时策略以及 read()/send() 的异常结果建立自己的会话健康判断, 而不是依赖瞬时状态查询.
///
/// @author scx567888
public interface ScxWebSocket extends AutoCloseable {

    static ScxWebSocket of(ScxFrameWebSocket frameWebSocket, long maxMessageSize) {
        return new ScxWebSocketImpl(frameWebSocket, maxMessageSize);
    }

    static ScxWebSocket of(ScxFrameWebSocket frameWebSocket) {
        return new ScxWebSocketImpl(frameWebSocket, 1024 * 1024 * 64); // 默认 64MB
    }

    static ScxWebSocket of(ByteEndpoint endpoint, boolean isClient, long maxMessageSize) {
        return new ScxWebSocketImpl(ScxFrameWebSocket.of(endpoint, isClient), maxMessageSize);
    }

    static ScxWebSocket of(ByteEndpoint endpoint, boolean isClient) {
        return new ScxWebSocketImpl(ScxFrameWebSocket.of(endpoint, isClient), 1024 * 1024 * 64); // 默认 64MB
    }

    WebSocketMessage read() throws WebSocketIOException, WebSocketProtocolException;

    void send(WebSocketMessage message) throws WebSocketIOException, WebSocketProtocolException, WebSocketInvalidStateException;

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
