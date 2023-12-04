package org.example.service;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.ssh.JschUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.example.component.InfoCache;
import org.example.po.ContainerInfo;
import org.example.po.SystemInfo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class InfoService{

    public void getSysInfo(Session session, String ip) {
        try {
            SystemInfo systemInfo = new SystemInfo();
            systemInfo.setIp(ip);
            systemInfo.setCpuCores(Integer.valueOf(execCmd(session, "lscpu | awk '/^CPU\\(s\\):/ {print $2}'")));
            systemInfo.setCpuArch(execCmd(session, "lscpu | awk '/^Architecture:/ {print $2}'"));
            systemInfo.setSysVersion(execCmd(session, "cat /etc/redhat-release"));
            Double memByteSize = Double.valueOf(execCmd(session, "tsar --mem -C -s total | awk -F= '{print $2}'"));
            String memGbSize = String.valueOf(Math.round(memByteSize / 1024 / 1024 / 1024));
            systemInfo.setMemSize(memGbSize + "GB");
            systemInfo.setKernelRelease(execCmd(session, "uname -r"));
            List<String> containerIds = Arrays.stream(JschUtil.exec(session, "docker ps -a | awk 'FNR>1 {print $1}'", null).split("\n")).collect(Collectors.toList());
            InfoCache.CONTAINER_MAP.put(ip, containerIds);
            ObjectMapper objectMapper = new ObjectMapper();
            log.info(ip + "主机信息采集完毕:\n" + objectMapper.writeValueAsString(systemInfo));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void getSysIndex(Session session, String ip) {

    }

    public void getContainerInfo(Session session, String containerId, String ip) {
        try {
            ContainerInfo containerInfo = new ContainerInfo();
            containerInfo.setIp(ip);
            containerInfo.setAvgCpuUsage(execCmd(session, String.format("docker stats %s --no-stream | awk 'NR>1 {print $3} '", containerId)));
            containerInfo.setContainerId(containerId);
            containerInfo.setContainerName(execCmd(session, String.format("docker ps -a | awk '/%s/ FNR>1 {print $NF}'", containerId)));
            containerInfo.setCpus(Double.parseDouble(execCmd(session, String.format("docker inspect %s | grep -i nanocpus | awk '{print $2/1000000000}'", containerId))));
            log.info(ip+"的" +containerInfo.getContainerName()+ "容器信息采集完毕:\n" + new ObjectMapper().writeValueAsString(containerInfo));
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    private String execCmd(Session session, String cmd) {
        return JschUtil.exec(session, cmd, CharsetUtil.CHARSET_UTF_8, System.err).trim();
    }
}
