package dev.scx.websocket.protocol_frame;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteInput;
import dev.scx.io.ByteOutput;
import dev.scx.io.exception.*;
import dev.scx.websocket.close_info.ScxWebSocketCloseInfo;
import dev.scx.websocket.exception.WebSocketProtocolException;
import dev.scx.websocket.op_code.WebSocketOpCode;

import static dev.scx.websocket.close_info.WebSocketCloseInfo.TOO_BIG;

/// WebSocketProtocolFrameHelper
///
/// @author scx567888
/// @version 0.0.1
/// @see <a href="https://www.rfc-editor.org/rfc/rfc6455">https://www.rfc-editor.org/rfc/rfc6455</a>
public final class WebSocketProtocolFrameHelper {

    private static WebSocketProtocolFrame readProtocolFrameHeader(ByteInput byteInput) throws IllegalArgumentException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        var protocolFrame = new WebSocketProtocolFrame();

        byte b1 = byteInput.read();

        protocolFrame.fin = (b1 & 0b1000_0000) != 0;
        protocolFrame.rsv1 = (b1 & 0b0100_0000) != 0;
        protocolFrame.rsv2 = (b1 & 0b0010_0000) != 0;
        protocolFrame.rsv3 = (b1 & 0b0001_0000) != 0;
        protocolFrame.opCode = WebSocketOpCode.of(b1 & 0b0000_1111);

        byte b2 = byteInput.read();

        protocolFrame.masked = (b2 & 0b1000_0000) != 0;
        long payloadLength = b2 & 0b0111_1111;

        // 读取扩展长度
        if (payloadLength == 126) {
            byte[] extendedPayloadLength = byteInput.readFully(2);
            payloadLength = (extendedPayloadLength[0] & 0b1111_1111L) << 8 |
                extendedPayloadLength[1] & 0b1111_1111L;
        } else if (payloadLength == 127) {
            byte[] extendedPayloadLength = byteInput.readFully(8);
            payloadLength = (extendedPayloadLength[0] & 0b1111_1111L) << 56 |
                (extendedPayloadLength[1] & 0b1111_1111L) << 48 |
                (extendedPayloadLength[2] & 0b1111_1111L) << 40 |
                (extendedPayloadLength[3] & 0b1111_1111L) << 32 |
                (extendedPayloadLength[4] & 0b1111_1111L) << 24 |
                (extendedPayloadLength[5] & 0b1111_1111L) << 16 |
                (extendedPayloadLength[6] & 0b1111_1111L) << 8 |
                extendedPayloadLength[7] & 0b1111_1111L;
        }

        protocolFrame.payloadLength = payloadLength;

        if (protocolFrame.masked) {
            protocolFrame.maskingKey = byteInput.readFully(4);
        }

        return protocolFrame;
    }

    private static WebSocketProtocolFrame readProtocolFramePayload(WebSocketProtocolFrame frame, ByteInput byteInput) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        var payloadLength = frame.payloadLength;
        var masked = frame.masked;
        var maskingKey = frame.maskingKey;

        // 这里我们假设 payloadLength 小于 int 值. 此处强转.
        var payloadData = byteInput.readFully((int) payloadLength);

        // 掩码计算
        if (masked) {
            for (int i = 0; i < payloadData.length; i = i + 1) {
                payloadData[i] = (byte) (payloadData[i] ^ maskingKey[i % 4]);
            }
        }

        frame.payloadData = payloadData;

        return frame;
    }

    /// 读取单个帧
    public static WebSocketProtocolFrame readProtocolFrame(ByteInput byteInput, long maxWebSocketFrameSize) throws IllegalArgumentException, NoMoreDataException, ScxInputException, InputAlreadyClosedException, WebSocketProtocolException {
        var webSocketFrame = readProtocolFrameHeader(byteInput);

        // 这里检查 最大帧大小
        if (webSocketFrame.payloadLength > maxWebSocketFrameSize) {
            throw new WebSocketProtocolException(ScxWebSocketCloseInfo.of(TOO_BIG.code(),"Frame too big"));
        }

        return readProtocolFramePayload(webSocketFrame, byteInput);
    }

    /// 注意这里会直接就地 对 frame 进行掩码. 如果上层需要复用 请自行 copy.
    public static void writeProtocolFrame(WebSocketProtocolFrame frame, ByteOutput byteOutput) throws ScxOutputException, OutputAlreadyClosedException {
        // 创建 header 防止频繁写入底层.
        byte[] header = new byte[14];

        // 头部
        header[0] = (byte) ((frame.fin ? 0b1000_0000 : 0) |
            (frame.rsv1 ? 0b0100_0000 : 0) |
            (frame.rsv2 ? 0b0010_0000 : 0) |
            (frame.rsv3 ? 0b0001_0000 : 0) |
            frame.opCode.code());

        long length = frame.payloadLength;
        var masked = frame.masked ? 0b1000_0000 : 0;

        var s = 0;
        if (length < 126L) {
            header[1] = (byte) (length | masked);
            s = 2;
        } else if (length < 65536L) {
            header[1] = (byte) (126 | masked);
            header[2] = (byte) (length >>> 8 & 0b1111_1111);
            header[3] = (byte) (length & 0b1111_1111);
            s = 4;
        } else {
            header[1] = (byte) (127 | masked);
            header[2] = (byte) (length >>> 56 & 0b1111_1111);
            header[3] = (byte) (length >>> 48 & 0b1111_1111);
            header[4] = (byte) (length >>> 40 & 0b1111_1111);
            header[5] = (byte) (length >>> 32 & 0b1111_1111);
            header[6] = (byte) (length >>> 24 & 0b1111_1111);
            header[7] = (byte) (length >>> 16 & 0b1111_1111);
            header[8] = (byte) (length >>> 8 & 0b1111_1111);
            header[9] = (byte) (length & 0b1111_1111);
            s = 10;
        }

        // 写入掩码键 (如果有)
        if (frame.masked) {
            byte[] maskingKey = frame.maskingKey;
            header[s] = maskingKey[0];
            header[s + 1] = maskingKey[1];
            header[s + 2] = maskingKey[2];
            header[s + 3] = maskingKey[3];
            s = s + 4;
        }

        // 写出头.
        byteOutput.write(ByteChunk.of(header, 0, s));

        // 处理掩码 (如果有)
        byte[] payloadData = frame.payloadData;
        byte[] maskingKey = frame.maskingKey;
        // 此处为了性能我们就地更改数组.
        if (frame.masked) {
            for (int i = 0; i < payloadData.length; i = i + 1) {
                payloadData[i] = (byte) (payloadData[i] ^ maskingKey[i % 4]);
            }
        }

        // 写入有效负载数据
        byteOutput.write(payloadData);
        byteOutput.flush();
    }

}
