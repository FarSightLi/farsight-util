package org.example.performance.config;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;
import org.example.performance.component.scheduled.HostXmlScheduledTask;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SessionConfig {
    private static final ConcurrentHashMap<String, Session> SESSION_MAP = new ConcurrentHashMap<>();


    private SessionConfig() {
    }

    public static Session getSession(String ip) {
        List<String> infoList = HostXmlScheduledTask.getHostMap().get(ip);
        if (ObjectUtil.isEmpty(infoList)) {
            log.error("{}没有主机账号密码信息", ip);
            throw new BusinessException(CodeMsg.SYSTEM_ERROR, "没有主机账号密码信息");
        }
        return SESSION_MAP.computeIfAbsent(ip, session -> JschUtil.getSession(ip, Integer.parseInt(infoList.get(1)), infoList.get(2), infoList.get(3)));
    }
}

