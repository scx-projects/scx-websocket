package dev.scx.websocket.test;

import dev.scx.websocket.close_info.ScxWebSocketCloseInfo;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WebSocketCloseInfoTest {

    public static void main(String[] args) {
        test1();
    }

    @Test
    public static void test1() {
        var closeInfo = ScxWebSocketCloseInfo.of(200, "测试😍🌴👩🥀✅");
        byte[] payload = closeInfo.toPayload();
        var closeInfo1 = ScxWebSocketCloseInfo.ofPayload(payload);
        Assert.assertEquals(closeInfo1, closeInfo);
    }

}
