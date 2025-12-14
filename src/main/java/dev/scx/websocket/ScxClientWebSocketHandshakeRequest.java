package dev.scx.websocket;

import dev.scx.http.ScxHttpClientRequest;
import dev.scx.http.ScxHttpClientResponse;
import dev.scx.http.headers.ScxHttpHeaderName;
import dev.scx.http.headers.ScxHttpHeaders;
import dev.scx.http.headers.cookie.Cookie;
import dev.scx.http.media.MediaWriter;
import dev.scx.http.method.ScxHttpMethod;
import dev.scx.http.sender.HttpSendException;
import dev.scx.http.sender.IllegalSenderStateException;
import dev.scx.http.uri.ScxURI;

import static dev.scx.http.method.HttpMethod.GET;

/// ScxClientWebSocketHandshakeRequest
/// 1, WebSocket 协议中指定了 必须由 GET 方法 和 空请求体 所以我们这里屏蔽掉一些方法
/// 2, 重写一些方法的返回值 方便我们链式调用
///
/// @author scx567888
/// @version 0.0.1
public interface ScxClientWebSocketHandshakeRequest extends ScxHttpClientRequest {

    /// 发送握手请求
    ScxClientWebSocketHandshakeResponse sendHandshake();

    /// 发送握手然后等待远端接受握手并返回 websocket 对象
    default ScxWebSocket webSocket() {
        return sendHandshake().webSocket();
    }

    @Override
    ScxClientWebSocketHandshakeRequest uri(ScxURI uri);

    @Override
    ScxClientWebSocketHandshakeRequest headers(ScxHttpHeaders headers);

    @Override
    default ScxClientWebSocketHandshakeRequest uri(String uri) {
        return (ScxClientWebSocketHandshakeRequest) ScxHttpClientRequest.super.uri(uri);
    }

    @Override
    default ScxClientWebSocketHandshakeRequest setHeader(ScxHttpHeaderName headerName, String... values) {
        return (ScxClientWebSocketHandshakeRequest) ScxHttpClientRequest.super.setHeader(headerName, values);
    }

    @Override
    default ScxClientWebSocketHandshakeRequest addHeader(ScxHttpHeaderName headerName, String... values) {
        return (ScxClientWebSocketHandshakeRequest) ScxHttpClientRequest.super.setHeader(headerName, values);
    }

    @Override
    default ScxClientWebSocketHandshakeRequest setHeader(String headerName, String... values) {
        return (ScxClientWebSocketHandshakeRequest) ScxHttpClientRequest.super.setHeader(headerName, values);
    }

    @Override
    default ScxClientWebSocketHandshakeRequest addHeader(String headerName, String... values) {
        return (ScxClientWebSocketHandshakeRequest) ScxHttpClientRequest.super.addHeader(headerName, values);
    }

    @Override
    default ScxClientWebSocketHandshakeRequest addCookie(Cookie... cookie) {
        return (ScxClientWebSocketHandshakeRequest) ScxHttpClientRequest.super.addCookie(cookie);
    }

    @Override
    default ScxClientWebSocketHandshakeRequest removeCookie(String name) {
        return (ScxClientWebSocketHandshakeRequest) ScxHttpClientRequest.super.removeCookie(name);
    }

    //************ 屏蔽方法 ****************

    @Override
    default ScxHttpMethod method() {
        return GET;
    }

    @Override
    default ScxHttpClientRequest method(ScxHttpMethod method) {
        throw new UnsupportedOperationException("Not supported Custom HttpMethod.");
    }

    @Override
    default ScxHttpClientResponse send(MediaWriter writer) throws IllegalSenderStateException, HttpSendException {
        throw new UnsupportedOperationException("Not supported Custom HttpBody.");
    }

}
