package dev.scx.websocket.op_code;

/// WebSocket 操作码
///
/// @author scx567888
/// @version 0.0.1
public enum WebSocketOpCode {

    CONTINUATION(0x0),
    TEXT(0x1),
    BINARY(0x2),
    CLOSE(0x8),
    PING(0x9),
    PONG(0xA);

    private static final WebSocketOpCode[] MAP = initMap();

    private final int code;

    WebSocketOpCode(int code) {
        this.code = code;
    }

    private static WebSocketOpCode[] initMap() {
        var m = new WebSocketOpCode[11];
        var values = WebSocketOpCode.values();
        for (var v : values) {
            m[v.code] = v;
        }
        return m;
    }

    /// @param code code
    /// @return 未找到时 抛出异常
    public static WebSocketOpCode of(int code) throws IllegalArgumentException {
        var c = find(code);
        if (c == null) {
            throw new IllegalArgumentException("Invalid WebSocket OpCode: " + code);
        }
        return c;
    }

    /// @param code code
    /// @return 未找到时 返回 null
    public static WebSocketOpCode find(int code) {
        if (code < 0 || code > 10) {
            return null;
        }
        return MAP[code];
    }

    public int code() {
        return code;
    }

}
