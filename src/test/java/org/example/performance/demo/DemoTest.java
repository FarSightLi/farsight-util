package org.example.performance.demo;

import org.example.performance.config.SessionConfig;
import org.example.performance.service.InfoService;
import org.junit.jupiter.api.Test;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 随意测试
 * @date 2023/12/8 09:17:35
 */
public class DemoTest {
    @Test
    public void test1() {
        InfoService infoService = new InfoService();
        infoService.getContainerInfo(SessionConfig.getSession("192.168.1.167"), "12341231", "192.168.1.167");
        infoService.getContainerInfo(SessionConfig.getSession("192.168.1.167"), "3f9022724014", "192.168.1.167");
        infoService.getContainerIndexInfo(SessionConfig.getSession("192.168.1.167"), "12341231", "192.168.1.167");
        infoService.getContainerIndexInfo(SessionConfig.getSession("192.168.1.167"), "3f9022724014", "192.168.1.167");
    }
}
