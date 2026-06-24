package dev.scx.websocket.frame;

/// WebSocketProtocolFrame
///
/// @author scx567888
/// @version 0.0.1
/// @see <a href="https://www.rfc-editor.org/rfc/rfc6455">https://www.rfc-editor.org/rfc/rfc6455</a>
final class WebSocketProtocolFrame {

    public boolean fin;
    public boolean rsv1;
    public boolean rsv2;
    public boolean rsv3;
    public int opCode;
    public boolean masked;
    public long payloadLength;
    public byte[] maskingKey;
    public byte[] payloadData;

}
