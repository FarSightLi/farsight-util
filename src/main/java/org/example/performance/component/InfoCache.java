package org.example.performance.component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class InfoCache {
    public static final ConcurrentHashMap<String, List<String>> CONTAINER_MAP = new ConcurrentHashMap<>();
}
