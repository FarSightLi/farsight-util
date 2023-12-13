package org.example.performance.cache;

import org.example.performance.component.CacheInfo;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 测试缓存信息
 * @date 2023/12/8 10:37:14
 */
class CacheInfoTest {
    @Resource
    private CacheInfo cacheInfo;

    @Test
    void testIpFix() {
        ConcurrentHashMap<String, List<String>> oldMap = new ConcurrentHashMap();
        List<String> ids1 = new ArrayList<>();
        List<String> ids2 = new ArrayList<>();
        ids1.add("a");
        ids1.add("b");
        ids1.add("c");
        ids2.add("1");
        ids2.add("2");
        ids2.add("3");
        oldMap.put("1.1.1.1", ids1);
        oldMap.put("2.2.2.2", ids2);
        cacheInfo.updateCache(oldMap);

        System.out.println();
        ConcurrentHashMap<String, List<String>> newMap = new ConcurrentHashMap();
        List<String> ids3 = new ArrayList<>();
        List<String> ids4 = new ArrayList<>();
        ids3.add("d");
        ids3.add("e");
        ids3.add("f");
        ids4.add("1");
        ids4.add("4");
        ids4.add("3");
        newMap.put("1.1.1.1", ids3);
        newMap.put("2.2.2.2", ids4);
        cacheInfo.updateCache(newMap);
        System.out.println(cacheInfo.getContainerMap());

    }

    @Test
    void testIpNoFix() {
        ConcurrentHashMap<String, List<String>> oldMap = new ConcurrentHashMap();
        List<String> ids1 = new ArrayList<>();
        List<String> ids2 = new ArrayList<>();
        ids1.add("a");
        ids1.add("b");
        ids1.add("c");
        ids2.add("1");
        ids2.add("2");
        ids2.add("3");
        oldMap.put("1.1.1.1", ids1);
        oldMap.put("2.2.2.2", ids2);
        cacheInfo.updateCache(oldMap);
        System.out.println(cacheInfo.getContainerMap());
        ConcurrentHashMap<String, List<String>> newMap = new ConcurrentHashMap();
        List<String> ids3 = new ArrayList<>();
        List<String> ids4 = new ArrayList<>();
        ids3.add("d");
        ids3.add("e");
        ids3.add("f");
        ids4.add("1");
        ids4.add("4");
        ids4.add("3");
        newMap.put("1.1.1.1", ids3);
        newMap.put("3.3.3.3", ids4);
        cacheInfo.updateCache(newMap);
        System.out.println(cacheInfo.getContainerMap());
    }
}
