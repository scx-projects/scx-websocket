package dev.scx.websocket.test;

import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketInvalidStateException;
import dev.scx.websocket.exception.WebSocketProtocolException;
import dev.scx.websocket.test.FrameWebSocketTest.TestSocketEndpoint;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static dev.scx.websocket.ScxWebSocket.createEventWebSocket;

public class EventWebSocketTest {

    static void main() throws IOException, WebSocketIOException, WebSocketProtocolException {
        startServer();
        startClient();
    }

    static void startServer() throws IOException {
        var serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(8899));
        Thread.ofPlatform().start(() -> {
            try {
                var socket = serverSocket.accept();

                var scxEventWebSocket = createEventWebSocket(new TestSocketEndpoint(socket), false);

                scxEventWebSocket.onClose(c -> {
                    System.out.println("server closed " + c);
                });

                Thread.ofVirtual().start(scxEventWebSocket::start);

                for (int i = 0; i < 1000; i++) {
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
            socket.connect(new InetSocketAddress(8899));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var scxEventWebSocket = createEventWebSocket(new TestSocketEndpoint(socket), true);

        scxEventWebSocket.onText((text) -> {
            System.out.println(text);
        }).onClose(c -> {
            System.out.println("close 了 " + c);
        });

        scxEventWebSocket.start();

    }


}
