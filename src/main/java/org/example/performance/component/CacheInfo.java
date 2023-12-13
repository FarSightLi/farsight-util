package org.example.performance.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 一些常用信息的缓存
 * @date 2023/12/7 17:47:31
 */
@Component
@Slf4j
@Scope("singleton")
public class CacheInfo {

    private static List<String> IP_LIST;

    public static List<String> getIpList() {
        return IP_LIST;
    }

    public static void setIpList(List<String> ipList) {
        ipList = ipList;
    }

    private CacheInfo() {

    }

}
