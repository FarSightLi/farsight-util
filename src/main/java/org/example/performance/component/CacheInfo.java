package org.example.performance.component;

import cn.hutool.core.lang.SimpleCache;

import java.util.List;
import java.util.Map;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 一些常用信息的缓存
 * @date 2023/12/7 17:47:31
 */
public class CacheInfo {
    private static final SimpleCache<String, List<String>> CONTAINER_MAP = new SimpleCache<>();

    private CacheInfo() {

    }

    public static SimpleCache<String, List<String>> getContainerMap() {
        return CONTAINER_MAP;
    }


    public static void resetContainerMap(Map<String, List<String>> containerMap) {
        CONTAINER_MAP.clear();
    }
}
