package dev.scx.websocket.test;

import dev.scx.websocket.ScxWebSocket;
import dev.scx.websocket.event.ScxEventWebSocket;
import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketInvalidStateException;
import dev.scx.websocket.exception.WebSocketProtocolException;
import dev.scx.websocket.test.FrameWebSocketTest.TestSocketEndpoint;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class EventWebSocketTest {

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
        serverSocket.bind(new InetSocketAddress(8880));
        Thread.ofPlatform().start(() -> {
            try {
                var socket = serverSocket.accept();

                var scxWebSocket = ScxWebSocket.of(new TestSocketEndpoint(socket), false);
                var scxEventWebSocket = ScxEventWebSocket.of(scxWebSocket);

                scxEventWebSocket.onClose(c -> {
                    System.out.println("server closed " + c);
                });

                Thread.ofVirtual().start(scxEventWebSocket::start);

                for (int i = 0; i < 1000; i = i + 1) {
                    scxEventWebSocket.send("Event 测试文本" + i);
                }

                scxEventWebSocket.sendClose();

            } catch (IOException | WebSocketIOException | WebSocketProtocolException |
                     WebSocketInvalidStateException e) {
                throw new RuntimeException(e);
            }

        });
    }

    static void startClient() throws IOException, WebSocketIOException, WebSocketProtocolException {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(8880));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var scxWebSocket = ScxWebSocket.of(new TestSocketEndpoint(socket), true);
        var scxEventWebSocket = ScxEventWebSocket.of(scxWebSocket);

        scxEventWebSocket.onText((text) -> {
            System.out.println(text);
        }).onClose(c -> {
            System.out.println("close 了 " + c);
        });

        scxEventWebSocket.start();

    }


}
