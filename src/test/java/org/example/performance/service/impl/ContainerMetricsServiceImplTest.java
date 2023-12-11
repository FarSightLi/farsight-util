package org.example.performance.service.impl;

import org.example.performance.service.ContainerMetricsService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description
 * @date 2023/12/11 09:29:55
 */
@SpringBootTest
class ContainerMetricsServiceImplTest {
    @Resource
    private ContainerMetricsService containerMetricsService;

    @Test
    void getContainerMetricsByIp() {
        containerMetricsService.getContainerMetricsByIp("192.168.1.167",
                LocalDateTime.of(2023, 12, 7, 10, 0, 1),
                LocalDateTime.of(2023, 12, 8, 10, 0, 1));
    }
}