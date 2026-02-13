package dev.scx.websocket.test;

import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketInvalidStateException;
import dev.scx.websocket.exception.WebSocketProtocolException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static dev.scx.websocket.ScxWebSocket.createMessageWebSocket;
import static dev.scx.websocket.message.WebSocketMessageType.CLOSE;
import static dev.scx.websocket.test.FrameWebSocketTest.TestSocketEndpoint;

public class MessageWebSocketTest {

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

                var scxMessageWebSocket = createMessageWebSocket(new TestSocketEndpoint(socket), true);

                for (int i = 0; i < 1000; i++) {
                    scxMessageWebSocket.send("Message 测试文本" + i);
                }

                scxMessageWebSocket.sendClose();

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

        var scxMessageWebSocket = createMessageWebSocket(new TestSocketEndpoint(socket), false);

        while (true) {
            var webSocketMessage = scxMessageWebSocket.readMessage();
            if (webSocketMessage.type() == CLOSE) {
                System.out.println("close 了");
                break;
            }
            System.out.println(new String(webSocketMessage.payloadData()));
        }

    }

}
