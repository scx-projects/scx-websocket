package dev.scx.websocket.exception;

import dev.scx.io.exception.*;

/// WebSocketIoException 表示 WebSocket 底层 I/O 不可继续使用, 包括但不限于:
///
/// - 对端关闭连接 (EOF) [NoMoreDataException].
/// - 本地输入/输出已关闭 [InputAlreadyClosedException] / [OutputAlreadyClosedException].
/// - 网络或传输错误 [ScxInputException] / [ScxOutputException]
///
/// 出现该异常后, 连接生命周期结束, 调用 close() 释放资源是安全且幂等的.
///
/// @author scx567888
/// @version 0.0.1
public class WebSocketIOException extends RuntimeException {

    public WebSocketIOException(Throwable cause) {
        super(cause);
    }

}
