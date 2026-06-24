package dev.scx.websocket.frame;

import dev.scx.io.ByteChunk;
import dev.scx.io.ByteInput;
import dev.scx.io.ByteOutput;
import dev.scx.io.exception.*;

/// WebSocket 协议帧二进制编解码辅助工具.
///
/// ### 设计意图
///
/// 本类仅负责按照 RFC6455 定义的 WebSocket 帧二进制结构, 在字节流与 [WebSocketProtocolFrame] 结构之间进行转换.
///
/// 本类遵循:
///   - 只解析结构, 不判断语义(Parse, do not judge).
///
/// 明确不做的事情 (非目标):
///   - 不进行任何 WebSocket 协议语义校验 (允许非法帧存在)
///   - 不执行 payload 掩码或反掩码操作
///   - 不校验控制帧规则(FIN / payload 长度等)
///   - 不管理 WebSocket 连接状态(close / ping / pong)
///   - 不做消息分片重组
///
/// ### Payload 行为说明
///
/// 如果帧为 masked, 则 payloadData 中保存的是 掩码后的原始字节数据, 本类不会进行任何转换.
///
/// 本类不是安全 WebSocket 实现, 也不适用于直接业务使用.
///
/// @author scx567888
/// @version 0.0.1
/// @see <a href="https://www.rfc-editor.org/rfc/rfc6455">https://www.rfc-editor.org/rfc/rfc6455</a>
final class WebSocketProtocolFrameHelper {

    /// 完全原样读取 protocolFrame, 不涉及任何校验或掩码处理.
    public static WebSocketProtocolFrame readProtocolFrameHeader(ByteInput byteInput) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        var protocolFrame = new WebSocketProtocolFrame();

        byte b1 = byteInput.read();

        protocolFrame.fin = (b1 & 0b1000_0000) != 0;
        protocolFrame.rsv1 = (b1 & 0b0100_0000) != 0;
        protocolFrame.rsv2 = (b1 & 0b0010_0000) != 0;
        protocolFrame.rsv3 = (b1 & 0b0001_0000) != 0;
        protocolFrame.opCode = b1 & 0b0000_1111;

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

    /// 完全原样读取 protocolFrame, 不涉及任何校验或掩码处理.
    public static WebSocketProtocolFrame readProtocolFramePayload(WebSocketProtocolFrame protocolFrame, ByteInput byteInput) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException {

        // 这里我们假设 payloadLength 小于 int 值. 此处强转.
        protocolFrame.payloadData = byteInput.readFully((int) protocolFrame.payloadLength);

        return protocolFrame;
    }

    /// 完全原样写出 protocolFrame, 不涉及任何校验或掩码处理.
    public static void writeProtocolFrame(WebSocketProtocolFrame protocolFrame, ByteOutput byteOutput) throws ScxOutputException, OutputAlreadyClosedException {
        // 创建 header 防止频繁写入底层.
        byte[] header = new byte[14];

        // 头部
        header[0] = (byte) ((protocolFrame.fin ? 0b1000_0000 : 0) |
            (protocolFrame.rsv1 ? 0b0100_0000 : 0) |
            (protocolFrame.rsv2 ? 0b0010_0000 : 0) |
            (protocolFrame.rsv3 ? 0b0001_0000 : 0) |
            protocolFrame.opCode);

        long length = protocolFrame.payloadLength;
        var masked = protocolFrame.masked ? 0b1000_0000 : 0;

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
        if (protocolFrame.masked) {
            byte[] maskingKey = protocolFrame.maskingKey;
            header[s] = maskingKey[0];
            header[s + 1] = maskingKey[1];
            header[s + 2] = maskingKey[2];
            header[s + 3] = maskingKey[3];
            s = s + 4;
        }

        // 写出头.
        byteOutput.write(ByteChunk.of(header, 0, s));

        byte[] payloadData = protocolFrame.payloadData;

        // 写入有效负载数据
        byteOutput.write(payloadData);
        byteOutput.flush();
    }

}
