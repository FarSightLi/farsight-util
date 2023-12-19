package org.example.performance.demo;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.pojo.bo.HostMetricsBO;
import org.example.performance.service.HostInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Resource;
import javax.xml.xpath.XPathConstants;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 随意测试
 * @date 2023/12/8 09:17:35
 */
@Slf4j
@SpringBootTest
public class DemoTest {
    @Resource
    private HostInfoService hostInfoService;

    @Test
    public void test1() {
        BigDecimal bigDecimal = BigDecimal.valueOf(0.00);
        bigDecimal.add(BigDecimal.valueOf(1.74));
        System.out.println(bigDecimal);
        System.out.println(bigDecimal.add(BigDecimal.valueOf(1.74)));
    }

    @Test
    public void test2() {
        Document document = XmlUtil.readXML("host/hosts.xml");
        NodeList nodeList = (NodeList) XmlUtil.getByXPath("//property[@name='hosts']/list/value", document, XPathConstants.NODESET);
        // 遍历所有的 value 元素
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String value = node.getTextContent().trim();
        }

    }

    @Test
    public void testTimeInterval() {
        String ip = "192.168.1.167";
        LocalDateTime startTime = LocalDateTime.of(2023, 12, 11, 11, 12);
        LocalDateTime endTime = LocalDateTime.of(2023, 12, 12, 12, 12);
        List<String> ipList = new ArrayList<>();
        ipList.add(ip);
        Map<String, Long> ip2IdMap = hostInfoService.getIp2IdMap(ipList);
        List<HostMetricsBO> hostMetricsBOList = new ArrayList<>();
        if (ObjectUtil.isEmpty(hostMetricsBOList)) {
            log.info("ip:{}在{}和{}时段没有性能信息", ip, startTime, endTime);
        }

        Duration between = Duration.between(startTime, endTime);
        log.info("day:{},hour:{},min:{}", between.toDays(), between.toHours(), between.toMinutes());
        if (between.toDays() >= 1) {
            System.out.println("几天");
        } else if (between.toHours() >= 12) {
            System.out.println("几小时");
        } else {
            System.out.println("几分钟");
        }

        // 指定时间间隔（秒）
        int timeIntervalInSeconds = 3600; // 一小时
        Map<Long, List<HostMetricsBO>> hourlyMetricsMap = hostMetricsBOList.stream()
                .collect(Collectors.groupingBy(
                        metrics -> metrics.getUpdateTime().atZone(ZoneId.systemDefault()).toEpochSecond() / 3600
                ));
        List<HostMetricsBO> collect = hourlyMetricsMap.values().stream()
                .map(metricsList -> metricsList.stream()
                        .max(Comparator.comparing(HostMetricsBO::getUpdateTime))
                        .orElse(null)
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
