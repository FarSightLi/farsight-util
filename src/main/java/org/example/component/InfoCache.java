package org.example.component;

import cn.hutool.core.lang.SimpleCache;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class InfoCache {
    public static final ConcurrentHashMap<String, List<String>> CONTAINER_MAP = new ConcurrentHashMap<>();
}
