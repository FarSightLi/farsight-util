package org.example.performance.component;

import cn.hutool.core.lang.SimpleCache;
import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.Session;

public class SessionConfig {
    private static final SimpleCache<String, Session> SESSION_MAP = new SimpleCache<>();

    private SessionConfig() {
    }

    public static Session getSession(String ip) {
        if (SESSION_MAP.get(ip) == null) {
            SESSION_MAP.put(ip, JschUtil.getSession(ip, 22, "farsight", "123456"));
        }
        return SESSION_MAP.get(ip);
    }
}

