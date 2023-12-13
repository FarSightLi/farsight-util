package org.example.performance.component.scheduled;

import cn.hutool.core.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.CacheInfo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;


/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 定时读取xml中主机账号密码
 * @date 2023/12/11 17:49:30
 */
@Component
@Slf4j
public class HostXmlScheduledTask {
    private static final ConcurrentHashMap<String, List<String>> HOST_MAP = new ConcurrentHashMap<>();

    @Scheduled(fixedRate = 30 * 60 * 1000) // 30min
    public void readXml() {
        read();
    }

    public static ConcurrentMap<String, List<String>> getHostMap() {
        return HOST_MAP;
    }

    public void read() {
        Document document = XmlUtil.readXML("host/hosts.xml");
        NodeList nodeList = (NodeList) XmlUtil.getByXPath("//property[@name='hosts']/list/value", document, XPathConstants.NODESET);
        // 遍历所有的 value 元素
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            List<String> infoList = Arrays.stream(node.getTextContent().trim().split(":")).collect(Collectors.toList());
            HOST_MAP.put(infoList.get(0), infoList);
        }
        log.info("主机账号密码信息读取完毕");
        CacheInfo.setIpList(new ArrayList<>(HOST_MAP.keySet()));
        log.info("ipList刷新完毕");
    }
}
