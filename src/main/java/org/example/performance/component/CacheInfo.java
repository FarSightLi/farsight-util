package org.example.performance.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 一些常用信息的缓存
 * @date 2023/12/7 17:47:31
 */
public class CacheInfo {
    private static final ConcurrentHashMap<String, List<String>> CONTAINER_MAP = new ConcurrentHashMap<>();

    private CacheInfo() {

    }

    public static ConcurrentMap<String, List<String>> getContainerMap() {
        return CONTAINER_MAP;
    }

    /**
     * 对缓存进行增量更新
     *
     * @param newData
     */
    public static void updateCache(Map<String, List<String>> newData) {
        newData.forEach((key, value) ->
                CONTAINER_MAP.compute(key, (existingKey, existingValue) -> {
                    if (existingValue == null) {
                        return new ArrayList<>(value);
                    } else {
                        // 删除新数据不存在的元素
                        existingValue.retainAll(value);
                        existingValue.addAll(value.stream().filter(e -> !existingValue.contains(e))
                                .collect(Collectors.toList()));
                        return existingValue;
                    }
                }));
        // 删除新map中不存在的ip数据
        CONTAINER_MAP.keySet().retainAll(newData.keySet());
    }

}
