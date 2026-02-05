package dev.scx.websocket.close_info;

/// ScxWebSocketCloseInfoImpl (不允许继承 和 外部 new 创建)
///
/// @author scx567888
/// @version 0.0.1
record ScxWebSocketCloseInfoImpl(Integer code, String reason) implements ScxWebSocketCloseInfo {

}
