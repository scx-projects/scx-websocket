package dev.scx.websocket.close_info;

/// WebSocketCloseInfo
///
/// @author scx567888
/// @version 0.0.1
public enum WebSocketCloseInfo implements ScxWebSocketCloseInfo {

    NORMAL_CLOSE(1000, "normal close"),
    GOING_AWAY(1001, "going away"),
    PROTOCOL_ERROR(1002, "protocol error"),
    CANNOT_ACCEPT(1003, "cannot accept message"),
    NO_STATUS_CODE(1005, "no status code"),
    CLOSED_ABNORMALLY(1006, "closed abnormally"),
    NOT_CONSISTENT(1007, "not consistent"),
    VIOLATED_POLICY(1008, "violated policy"),
    TOO_BIG(1009, "too big"),
    NO_EXTENSION(1010, "no extension"),
    UNEXPECTED_CONDITION(1011, "unexpected condition"),
    SERVICE_RESTART(1012, "service restart"),
    TRY_AGAIN_LATER(1013, "try again later"),
    BAD_GATEWAY(1014, "bad gateway"),
    TLS_HANDSHAKE_FAIL(1015, "tls handshake fail"),
    RESERVED_FOR_LATER_MIN(1016, "reserved for later min"),
    RESERVED_FOR_LATER_MAX(1999, "reserved for later max"),
    RESERVED_FOR_EXTENSIONS_MIN(2000, "reserved for extensions min"),
    RESERVED_FOR_EXTENSIONS_MAX(2999, "reserved for extensions max"),
    REGISTERED_AT_IANA_MIN(3000, "registered at iana min"),
    REGISTERED_AT_IANA_MAX(3999, "registered at iana max"),
    APPLICATION_MIN(4000, "application min"),
    APPLICATION_MAX(4999, "application max");

    private final int code;

    private final String reason;

    WebSocketCloseInfo(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String reason() {
        return reason;
    }

}
