package org.example.service;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.ssh.JschUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.example.component.InfoCache;
import org.example.po.ContainerInfo;
import org.example.po.SysTemChartInfo;
import org.example.po.SystemInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class InfoService {

    public void getSysInfo(Session session, String ip) {
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setIp(ip);
        systemInfo.setCpuCores(Integer.valueOf(execCmd(session, "lscpu | awk '/^CPU\\(s\\):/ {print $2}'")));
        systemInfo.setCpuArch(execCmd(session, "lscpu | awk '/^Architecture:/ {print $2}'"));
        systemInfo.setSysVersion(execCmd(session, "cat /etc/redhat-release"));
        Double memByteSize = Double.valueOf(execCmd(session, "tsar --mem -C -s total | awk -F= '{print $2}'"));
        String memGbSize = String.valueOf(Math.round(b2Gb(memByteSize)));
        systemInfo.setMemSize(memGbSize + "GB");
        systemInfo.setKernelRelease(execCmd(session, "uname -r"));
        List<String> containerIds = Arrays.stream(JschUtil.exec(session, "docker ps -a | awk 'FNR>1 {print $1}'", null).split("\n")).collect(Collectors.toList());
        InfoCache.CONTAINER_MAP.put(ip, containerIds);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            log.info(ip + " 主机信息采集完毕:\n" + objectMapper.writeValueAsString(systemInfo));
        } catch (JsonProcessingException e) {
            e.getMessage();
        }
    }

    public void getSysIndex(Session session, String ip) {
        SysTemChartInfo sysTemChartInfo = new SysTemChartInfo();
        sysTemChartInfo.setIp(ip);
        sysTemChartInfo.setMemRate(string2Decimal(execCmd(session, "tsar --mem -C -s  util  | awk -F= '{print $2}'")));
        sysTemChartInfo.setMem(b2Mb(string2Decimal(execCmd(session, "tsar --mem -C -s  used  | awk -F= '{print $2}'")
        )));
        sysTemChartInfo.setByteIn(b2Mb(string2Decimal(execCmd(session, "cat /proc/net/dev | grep ens | awk '{print $2}'"))));
        sysTemChartInfo.setLoad(string2Decimal(execCmd(session, "tsar --load -C -s load1 |  awk -F= '{print $2}'")));
        sysTemChartInfo.setByteOut(b2Mb(string2Decimal(execCmd(session, "cat /proc/net/dev | grep ens | awk '{print $10} '"))));
        sysTemChartInfo.setIo(string2Decimal(execCmd(session, "iostat -x | awk '/%util/ {getline; print $NF}'")));
        sysTemChartInfo.setCpu(string2Decimal(execCmd(session, "tsar --cpu -C -s util |  awk -F= '{print $2}'")));
        sysTemChartInfo.setDisk(execCmd(session, "df -h | awk '$NF == \"/\" {print $5}'"));
        sysTemChartInfo.setInode(execCmd(session, "df -h -i| awk '$NF == \"/\" {print $5}'"));
        sysTemChartInfo.setTcp(string2Decimal(execCmd(session, "netstat -nat | grep tcp | wc -l")));
        try {
            log.info(new ObjectMapper().writeValueAsString(sysTemChartInfo));
        } catch (JsonProcessingException e) {
            log.info(e.getMessage());
        }
    }


    public void getContainerInfo(Session session, String containerId, String ip) {
        ContainerInfo containerInfo = new ContainerInfo();
        containerInfo.setIp(ip);
        containerInfo.setAvgCpuUsage(execCmd(session, String.format("docker stats %s --no-stream | awk 'NR>1 {print $3} '", containerId)));
        containerInfo.setContainerId(containerId);
        containerInfo.setContainerName(execCmd(session, String.format("docker ps -a | awk '/%s/ FNR>1 {print $NF}'", containerId)));
        containerInfo.setCpus(Double.parseDouble(execCmd(session, String.format("docker inspect %s | grep -i nanocpus | awk '{print $2/1000000000}'", containerId))));
        try {
            log.info(ip + " 的 " + containerInfo.getContainerName() + ":\n" + new ObjectMapper().writeValueAsString(containerInfo));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    private String execCmd(Session session, String cmd) {
        return JschUtil.exec(session, cmd, CharsetUtil.CHARSET_UTF_8, System.err).trim();
    }

    private BigDecimal b2Mb(BigDecimal kb) {
        BigDecimal bigDecimal = new BigDecimal("1024");
        return kb.divide(bigDecimal).divide(bigDecimal).setScale(2,RoundingMode.HALF_UP);
    }

    private double b2Gb(double kb) {
        BigDecimal bigDecimal = new BigDecimal("1024");
        return kb / 1024 / 1024 / 1024;
    }

    private BigDecimal string2Decimal(String value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        return bigDecimal.setScale(2, RoundingMode.HALF_UP);
    }
}
