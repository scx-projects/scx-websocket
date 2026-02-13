package dev.scx.websocket.test;

import dev.scx.io.ByteInput;
import dev.scx.io.ByteOutput;
import dev.scx.io.ScxIO;
import dev.scx.io.endpoint.ByteEndpoint;
import dev.scx.websocket.exception.WebSocketIOException;
import dev.scx.websocket.exception.WebSocketProtocolException;
import dev.scx.websocket.frame.WebSocketFrame;
import dev.scx.websocket.op_code.WebSocketOpCode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static dev.scx.websocket.ScxWebSocket.createFrameWebSocket;
import static dev.scx.websocket.op_code.WebSocketOpCode.CLOSE;

public class FrameWebSocketTest {

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

                var scxFrameWebSocket = createFrameWebSocket(new TestSocketEndpoint(socket), false);

                for (int i = 0; i < 1000; i++) {
                    scxFrameWebSocket.sendFrame(new WebSocketFrame(WebSocketOpCode.TEXT, ("Frame 测试文本" + i).getBytes(), true));
                }

                scxFrameWebSocket.sendFrame(new WebSocketFrame(CLOSE, new byte[0], true));

            } catch (IOException | WebSocketIOException | WebSocketProtocolException e) {
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

        var scxFrameWebSocket = createFrameWebSocket(new TestSocketEndpoint(socket), true);

        while (true) {
            var webSocketFrame = scxFrameWebSocket.readFrame();
            if (webSocketFrame.opCode() == CLOSE) {
                System.out.println("close 了");
                break;
            }
            System.out.println(new String(webSocketFrame.payloadData()));
        }

    }

    static class TestSocketEndpoint implements ByteEndpoint {

        private final Socket socket;
        private final ByteInput in;
        private final ByteOutput out;

        public TestSocketEndpoint(Socket socket) throws IOException {
            this.socket = socket;
            this.in = ScxIO.createByteInput(socket.getInputStream());
            this.out = ScxIO.createByteOutput(socket.getOutputStream());
        }

        @Override
        public ByteInput in() {
            return in;
        }

        @Override
        public ByteOutput out() {
            return out;
        }

        @Override
        public void close() throws Exception {
            socket.close();
        }

    }

}
