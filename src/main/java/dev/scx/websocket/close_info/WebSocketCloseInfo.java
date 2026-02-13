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
    TRY_AGAIN_LATER(1013, "try again later");

    private final int code;
    private final String reason;

    WebSocketCloseInfo(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    @Override
    public Integer code() {
        return code;
    }

    @Override
    public String reason() {
        return reason;
    }

}
