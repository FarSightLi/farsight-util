package org.example.performance.service;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.ssh.JschUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;
import org.example.performance.pojo.bo.ContainerMetricsBO;
import org.example.performance.pojo.bo.HostMetricsBO;
import org.example.performance.pojo.po.ContainerInfo;
import org.example.performance.pojo.po.DiskInfo;
import org.example.performance.pojo.po.HostInfo;
import org.example.performance.util.DataUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.example.performance.util.DataUtil.*;

@Slf4j
public class InfoService {

    public HostInfo getSysInfo(Session session, String ip) {
        HostInfo hostInfo = new HostInfo();
        hostInfo.setIp(ip);
        hostInfo.setHostName(execCmd(session, "hostname"));
        hostInfo.setCpuCores(Integer.valueOf(execCmd(session, "lscpu | awk '/^CPU\\(s\\):/ {print $2}'")));
        hostInfo.setCpuArch(execCmd(session, "lscpu | awk '/^Architecture:/ {print $2}'"));
        hostInfo.setSysVersion(execCmd(session, "cat /etc/redhat-release"));
        String memByteSize = execCmd(session, "tsar --mem -C -s total | awk -F= '{print $2}'");
        hostInfo.setMemSize(parseDataSize(memByteSize));
        hostInfo.setKernelRelease(execCmd(session, "uname -r"));
        hostInfo.setContainerIdList(Arrays.stream(JschUtil.exec(session, "docker ps -a | awk 'FNR>1 {print $1}'", null).split("\n")).collect(Collectors.toList()));

        try {
            log.info(ip + " 主机信息采集完毕:\n" + new ObjectMapper().writeValueAsString(hostInfo));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        return hostInfo;
    }

    public HostMetricsBO getSysIndex(Session session, String ip) {
        HostMetricsBO hostmetrics = new HostMetricsBO();
        hostmetrics.setHostIp(ip);
        hostmetrics.setMemRate(string2Decimal(execCmd(session, "tsar --mem -C -s  util  | awk -F= '{print $2}'")));
        hostmetrics.setMem(parseDataSize(execCmd(session, "tsar --mem -C -s  used  | awk -F= '{print $2}'")));
        hostmetrics.setByteIn(parseDataSize(execCmd(session, "tsar --traffic -C -s bytout | awk -F= '{print$2}'")));
        hostmetrics.setHostLoad(string2Decimal(execCmd(session, "tsar --load -C -s load1 |  awk -F= '{print $2}'")));
        hostmetrics.setByteOut(parseDataSize(execCmd(session, "tsar --traffic -C -s bytin | awk -F= '{print$2}'")));
        hostmetrics.setIo(string2Decimal(execCmd(session, "iostat -x | awk '/%util/ {getline; print $NF}'")));
        hostmetrics.setCpu(string2Decimal(execCmd(session, "tsar --cpu -C -s util |  awk -F= '{print $2}'")));
        hostmetrics.setDisk(string2Decimal(removePercent(execCmd(session, "df -h | awk '$NF == \"/\" {print $5}'"))));
        hostmetrics.setInode(string2Decimal(removePercent(execCmd(session, "df -h -i| awk '$NF == \"/\" {print $5}'"))));
        hostmetrics.setTcp(string2Decimal(execCmd(session, "netstat -nat | grep tcp | wc -l")));

        //获得磁盘信息
        String diskInfo = execCmd(session, "df -h | awk '$NF == \"/\" || $NF ~ /^\\/[^\\/]+$/ {print $2,$3,$5,$6}'");
        String diskInodeInfo = execCmd(session, "df -h -i | awk '$NF == \"/\" || $NF ~ /^\\/[^\\/]+$/ {print $5,$6}'");
        hostmetrics.setDiskInfoList(getDiskInfo(diskInfo, diskInodeInfo, ip));

        try {
            log.info(ip + "性能指标：\n" + new ObjectMapper().writeValueAsString(hostmetrics));
        } catch (JsonProcessingException e) {
            log.info(e.getMessage());
        }
        return hostmetrics;
    }


    public ContainerInfo getContainerInfo(Session session, String containerId, String ip) {
        ContainerInfo containerInfo = new ContainerInfo();
        // 先设置信息，方便日志定位错误
        containerInfo.setContainerId(containerId);
        containerInfo.setHostIp(ip);
        try {
            // 如果为0代表没有限制CPU
            String cpus = execCmd(session, String.format("docker inspect %s | grep -i nanocpus | awk '{print $2/1000000000}'", containerId));
            containerInfo.setCpus(string2Decimal(cpus));
            String imageSize = execCmd(session, String.format("docker images `docker system df -v | awk '/%s/ {print $2}'` | awk 'END{print $NF}'", containerId));
            containerInfo.setImageSize(parseDataSize(imageSize));
            String diskSizeStr = execCmd(session, String.format("docker stats %s --no-stream | awk 'NR>1 {print $11} '", containerId));
            BigDecimal diskSize = parseDataSize(diskSizeStr);
            // diskSize>0，即当前容器在运行
            if (diskSize.compareTo(BigDecimal.ZERO) == 1) {
                containerInfo.setDiskSize(diskSize);
            }
            containerInfo.setDiskSize(diskSize);
            String memSizeStr = execCmd(session, String.format("docker stats %s --no-stream | awk 'NR>1 {print $6} '", containerId));
            BigDecimal memSize = parseDataSize(memSizeStr);
            // memSize>0，即当前容器在运行
            if (memSize.compareTo(BigDecimal.ZERO) == 1) {
                containerInfo.setMemSize(memSize);
            }
            String creatTime = execCmd(session, String.format("docker inspect --format '{{.Created}}' %s", containerId));
            containerInfo.setCreateTime(getTime(creatTime));
            String details = execCmd(session, String.format("docker ps -a --format json  --filter 'id=%s'  --size", containerId));

            HashMap<String, String> detailsMap = new ObjectMapper().readValue(details, HashMap.class);
            String names = detailsMap.get("Names");
            if (names.contains("_")) {
                String[] split = names.split("_");
                containerInfo.setVersion(split[1]);
                containerInfo.setContainerName(split[0]);
            } else {
                log.error("容器名称不符合规范");
            }

            log.info(ip + " 的 " + containerInfo.getContainerName() + ":\n" + new ObjectMapper().writeValueAsString(containerInfo));
        } catch (JsonProcessingException e) {
            log.error("json解析出错---" + e.getMessage());
        } catch (Exception e) {
            log.error("{}的{}容器信息出错了", ip, containerInfo.getContainerId());
            log.error(e.getMessage(), e);
        }
        return containerInfo;
    }

    public ContainerMetricsBO getContainerIndexInfo(Session session, String containerId, String ip) {
        ContainerMetricsBO containerMetricsBO = new ContainerMetricsBO();
        containerMetricsBO.setContainerId(containerId);
        try {
            String details = execCmd(session, String.format("docker ps -a --format json  --filter 'id=%s'  --size", containerId));
            HashMap<String, String> detailsMap = new ObjectMapper().readValue(details, HashMap.class);
            containerMetricsBO.setState(detailsMap.get("State"));
            String cpuRate = execCmd(session, String.format("docker stats %s --no-stream | awk 'NR>1 {print $3} '", containerId));
            containerMetricsBO.setCpuRate(DataUtil.string2Decimal(removePercent(cpuRate)));
            String onlineTime = execCmd(session, String.format("docker inspect --format '{{.State.StartedAt}}' %s", containerId));
            containerMetricsBO.setRestartTime(getTime(onlineTime));
            String memUsedSize = execCmd(session, String.format("docker stats %s --no-stream | awk 'NR>1 {print $4} '", containerId));
            String diskUsedSize = execCmd(session, String.format("docker stats %s --no-stream | awk 'NR>1 {print $11} '", containerId));
            containerMetricsBO.setMemUsedSize(parseDataSize(memUsedSize));
            containerMetricsBO.setDiskUsedSize(parseDataSize(diskUsedSize));
            log.info(ip + " 的 " + containerMetricsBO.getContainerId() + ":\n" + new ObjectMapper().writeValueAsString(containerMetricsBO));
        } catch (JsonProcessingException e) {
            log.error("json解析出错---" + e.getMessage());
        } catch (Exception e) {
            log.error("{}的{}容器性能指标出错了", ip, containerMetricsBO.getContainerId());
            log.error(e.toString(), e);
        }
        return containerMetricsBO;
    }


    private String execCmd(Session session, String cmd) {
        if (session == null) {
            throw new BusinessException(CodeMsg.SYSTEM_ERROR, "session为空了");
        }
        return JschUtil.exec(session, cmd, CharsetUtil.CHARSET_UTF_8).trim();
    }

    /**
     * 获得主机对应的磁盘信息（包括根目录和一级目录的磁盘大小，使用量，io速度，磁盘名，inode使用率）
     *
     * @param strInfo       采集到的磁盘信息
     * @param diskInodeInfo 采集到的磁盘inode信息
     * @param ip            ip
     * @return 磁盘信息列表
     */
    private List<DiskInfo> getDiskInfo(String strInfo, String diskInodeInfo, String ip) {
        Pattern pattern1 = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)");
        Pattern pattern2 = Pattern.compile("(\\S+)\\s+(\\S+)");
        Matcher matcher1 = pattern1.matcher(strInfo);
        Matcher matcher2 = pattern2.matcher(diskInodeInfo);
        List<DiskInfo> diskInfoList = new ArrayList<>();
        while (matcher1.find() && matcher2.find()) {
            DiskInfo diskInfo = new DiskInfo();
            diskInfo.setHostIp(ip);
            String size = matcher1.group(1);
            String used = matcher1.group(2);
            String ioRate = matcher1.group(3);
            String name = matcher1.group(4);
            String inodeUsed = matcher2.group(1);
            diskInfo.setIoRate(string2Decimal(removePercent(ioRate)));
            diskInfo.setInodeUsedRate(string2Decimal(removePercent(inodeUsed)));
            diskInfo.setDfName(name);
            BigDecimal mbDiskSize = parseDataSize(size);
            diskInfo.setDfSize(mbDiskSize);
            BigDecimal mbDiskUsedSize = parseDataSize(used);
            diskInfo.setDiskUsedSize(mbDiskUsedSize);
            diskInfoList.add(diskInfo);
        }
        return diskInfoList;
    }


}
