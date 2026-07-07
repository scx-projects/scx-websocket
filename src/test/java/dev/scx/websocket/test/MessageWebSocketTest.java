package dev.scx.websocket.test;

import dev.scx.websocket.CloseMessage;
import dev.scx.websocket.ScxWebSocket;
import dev.scx.websocket.TextMessage;
import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketInvalidStateException;
import dev.scx.websocket.exception.WebSocketProtocolException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static dev.scx.websocket.test.FrameWebSocketTest.TestSocketEndpoint;

public class MessageWebSocketTest {

    public static void main(String[] args) throws IOException, WebSocketIOException, WebSocketProtocolException {
        test1();
    }

    @Test
    public static void test1() throws IOException {
        startServer();
        startClient();
    }

    static void startServer() throws IOException {
        var serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(8882));
        Thread.ofPlatform().start(() -> {
            try {
                var socket = serverSocket.accept();

                var scxWebSocket = ScxWebSocket.of(new TestSocketEndpoint(socket), true);

                for (int i = 0; i < 1000; i = i + 1) {
                    scxWebSocket.send("Message 测试文本" + i);
                }

                scxWebSocket.sendClose();

            } catch (IOException | WebSocketIOException | WebSocketProtocolException |
                     WebSocketInvalidStateException e) {
                throw new RuntimeException(e);
            }

        });
    }

    static void startClient() throws IOException, WebSocketIOException, WebSocketProtocolException {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(8882));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var scxWebSocket = ScxWebSocket.of(new TestSocketEndpoint(socket), false);

        while (true) {
            var webSocketMessage = scxWebSocket.read();
            if (webSocketMessage instanceof CloseMessage) {
                System.out.println("close 了");
                break;
            }
            if (webSocketMessage instanceof TextMessage textMessage) {
                System.out.println(textMessage.text());
            }
        }

    }

}
