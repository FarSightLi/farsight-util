package org.example.component;

import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.Session;

public class SshSession {
    private static volatile Session SESSION;

    private SshSession() {
    }

    public static Session getSession() {
        if (SESSION == null) {
            synchronized (SshSession.class) {
                if (SESSION == null) {
                    SESSION = JschUtil.getSession("192.168.1.167", 22, "farsight", "123456");
                }
            }
        }
        return SESSION;
    }
}

