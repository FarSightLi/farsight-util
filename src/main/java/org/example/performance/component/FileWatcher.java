package org.example.performance.component;

import cn.hutool.core.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import java.nio.file.*;
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
 * @description 监听文件变动
 * @date 2023/12/20 16:29:10
 */
@Slf4j
@Component
public class FileWatcher {
    private static final ConcurrentHashMap<String, List<String>> HOST_MAP = new ConcurrentHashMap<>();

    public void watch() throws Exception {
        Path path = Paths.get("src/main/resources/host/hosts.xml");

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            path.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        Path changed = (Path) event.context();
                        if (changed.endsWith("hosts.xml")) {
                            read();
                        }
                    }
                }
                key.reset();
            }
        }
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
