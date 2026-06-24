package dev.scx.websocket;

/// WebSocketCloseInfo
///
/// @param code   允许为 null, 但同时 reason 必须也是 null
/// @param reason 允许为 null
/// @author scx567888
/// @version 0.0.1
public record WebSocketCloseInfo(Integer code, String reason) {

    public static final WebSocketCloseInfo NORMAL_CLOSE = new WebSocketCloseInfo(1000, "normal close");
    public static final WebSocketCloseInfo GOING_AWAY = new WebSocketCloseInfo(1001, "going away");
    public static final WebSocketCloseInfo PROTOCOL_ERROR = new WebSocketCloseInfo(1002, "protocol error");
    public static final WebSocketCloseInfo CANNOT_ACCEPT = new WebSocketCloseInfo(1003, "cannot accept message");
    public static final WebSocketCloseInfo NO_STATUS_CODE = new WebSocketCloseInfo(1005, "no status code");
    public static final WebSocketCloseInfo CLOSED_ABNORMALLY = new WebSocketCloseInfo(1006, "closed abnormally");
    public static final WebSocketCloseInfo NOT_CONSISTENT = new WebSocketCloseInfo(1007, "not consistent");
    public static final WebSocketCloseInfo VIOLATED_POLICY = new WebSocketCloseInfo(1008, "violated policy");
    public static final WebSocketCloseInfo TOO_BIG = new WebSocketCloseInfo(1009, "too big");
    public static final WebSocketCloseInfo NO_EXTENSION = new WebSocketCloseInfo(1010, "no extension");
    public static final WebSocketCloseInfo UNEXPECTED_CONDITION = new WebSocketCloseInfo(1011, "unexpected condition");
    public static final WebSocketCloseInfo SERVICE_RESTART = new WebSocketCloseInfo(1012, "service restart");
    public static final WebSocketCloseInfo TRY_AGAIN_LATER = new WebSocketCloseInfo(1013, "try again later");

}
