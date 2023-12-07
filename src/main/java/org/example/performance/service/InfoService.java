package org.example.performance.service;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.ssh.JschUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.pojo.po.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class InfoService {

    public HostInfo getSysInfo(Session session, String ip) {
        HostInfo hostInfo = new HostInfo();
        hostInfo.setIp(ip);
        hostInfo.setCpuCores(Integer.valueOf(execCmd(session, "lscpu | awk '/^CPU\\(s\\):/ {print $2}'")));
        hostInfo.setCpuArch(execCmd(session, "lscpu | awk '/^Architecture:/ {print $2}'"));
        hostInfo.setSysVersion(execCmd(session, "cat /etc/redhat-release"));
        String memByteSize = execCmd(session, "tsar --mem -C -s total | awk -F= '{print $2}'");
        hostInfo.setMemSize(parseDataSize(memByteSize));
        hostInfo.setKernelRelease(execCmd(session, "uname -r"));
        hostInfo.setUpdateTime(LocalDateTime.now());
        hostInfo.setContainerIdList(Arrays.stream(JschUtil.exec(session, "docker ps -a | awk 'FNR>1 {print $1}'", null).split("\n")).collect(Collectors.toList()));

        try {
            log.info(ip + " 主机信息采集完毕:\n" + new ObjectMapper().writeValueAsString(hostInfo));
        } catch (JsonProcessingException e) {
            e.getMessage();
        }
        return hostInfo;
    }

    public HostMetrics getSysIndex(Session session, String ip) {
        HostMetrics hostmetrics = new HostMetrics();
        hostmetrics.setHostIp(ip);
        hostmetrics.setMemRate(string2Decimal(execCmd(session, "tsar --mem -C -s  util  | awk -F= '{print $2}'")));
        hostmetrics.setMem(parseDataSize(execCmd(session, "tsar --mem -C -s  used  | awk -F= '{print $2}'")));
        hostmetrics.setByteIn(parseDataSize(execCmd(session, "cat /proc/net/dev | grep ens | awk '{print $2}'")));
        hostmetrics.setHostLoad(string2Decimal(execCmd(session, "tsar --load -C -s load1 |  awk -F= '{print $2}'")));
        hostmetrics.setByteOut(parseDataSize(execCmd(session, "cat /proc/net/dev | grep ens | awk '{print $10} '")));
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
        try {
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
            containerInfo.setHostIp(ip);
            containerInfo.setContainerId(containerId);
            // 如果为0代表没有限制CPU
            String cpus = execCmd(session, String.format("docker inspect %s | grep -i nanocpus | awk '{print $2/1000000000}'", containerId));
            containerInfo.setCpus(string2Decimal(cpus));
            String imageSize = execCmd(session, String.format("docker images `docker system df -v | awk '/%s/ {print $2}'` | awk 'END{print $NF}'", containerId));
            containerInfo.setImageSize(parseDataSize(imageSize));
            String diskSize = execCmd(session, String.format("docker stats %s --no-stream | awk 'NR>1 {print $11} '", containerId));
            containerInfo.setDiskSize(parseDataSize(diskSize));
            String memSize = execCmd(session, String.format("docker stats %s --no-stream | awk 'NR>1 {print $6} '", containerId));
            containerInfo.setMemSize(parseDataSize(memSize));
            String creatTime = execCmd(session, String.format("docker inspect --format '{{.Created}}' %s", containerId));
            containerInfo.setCreateTime(getTime(creatTime));
        } catch (Exception e) {
            log.error(ip + "的" + containerInfo.getContainerName() + "信息出错了");
            log.error(e.getMessage());
        }
        try {
            log.info(ip + " 的 " + containerInfo.getContainerName() + ":\n" + new ObjectMapper().writeValueAsString(containerInfo));
        } catch (JsonProcessingException e) {
            log.error("json解析出错---" + e.getMessage());
        }
        return containerInfo;
    }

    public ContainerMetrics getContainerIndexInfo(Session session, String containerId, String ip) {
        ContainerMetrics containerMetrics = new ContainerMetrics();
        containerMetrics.setContainerId(containerId);
        try {
            String details = execCmd(session, String.format("docker ps -a --format json  --filter 'id=%s'  --size", containerId));
            HashMap<String, String> detailsMap = new ObjectMapper().readValue(details, HashMap.class);
            containerMetrics.setState(detailsMap.get("State"));
            String cpuRate = execCmd(session, String.format("docker stats %s --no-stream | awk 'NR>1 {print $3} '", containerId));
            containerMetrics.setCpuRate(Double.parseDouble(removePercent(cpuRate)));
            String onlineTime = execCmd(session, String.format("docker inspect --format '{{.State.StartedAt}}' %s", containerId));
            containerMetrics.setRestartTime(getTime(onlineTime));
            String memUsedSize = execCmd(session, String.format("docker stats %s --no-stream | awk 'NR>1 {print $4} '", containerId));
            String diskUsedSize = execCmd(session, String.format("docker stats %s --no-stream | awk 'NR>1 {print $11} '", containerId));
            containerMetrics.setMemUsedSize(parseDataSize(memUsedSize));
            containerMetrics.setDiskUsedSize(parseDataSize(diskUsedSize));
        } catch (Exception e) {
            log.error(ip + "的" + containerMetrics.getContainerId() + "信息出错了");
            log.error(e.getMessage());
        }
        try {
            log.info(ip + " 的 " + containerMetrics.getContainerId() + ":\n" + new ObjectMapper().writeValueAsString(containerMetrics));
        } catch (JsonProcessingException e) {
            log.error("json解析出错---" + e.getMessage());
        }
        return containerMetrics;
    }

    private String execCmd(Session session, String cmd) {
        return JschUtil.exec(session, cmd, CharsetUtil.CHARSET_UTF_8, System.err).trim();
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

    private BigDecimal string2Decimal(String value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        return bigDecimal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 移除百分号
     *
     * @param target
     * @return
     */
    private String removePercent(String target) {
        return target.replace("%", "").trim();
    }

    private LocalDateTime getTime(String stringTime) {
        return LocalDateTime.parse(stringTime, DateTimeFormatter.ISO_DATE_TIME);
    }

    /**
     * 根据传进的字符自动解析为MB大小
     *
     * @param dataSizeString
     * @return
     */
    private BigDecimal parseDataSize(String dataSizeString) {
        Pattern pattern = Pattern.compile("(\\d+\\.?\\d*)([BKMGTPEZYbkmgtpezy]B?)");
        Matcher matcher = pattern.matcher(dataSizeString);

        if (matcher.find()) {
            String valueStr = matcher.group(1);
            String unit = matcher.group(2).toUpperCase();
            BigDecimal value = new BigDecimal(valueStr);

            switch (unit) {
                case "B":
                    return value.divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP);
                case "KB":
                case "K":
                    return value.divide(BigDecimal.valueOf(1024), 2, RoundingMode.HALF_UP);
                case "MB":
                case "MIB":
                case "M":
                    return value;
                case "GB":
                case "G":
                case "GIB":
                    return value.multiply(BigDecimal.valueOf(1024));
                default:
                    throw new IllegalArgumentException("Unsupported unit: " + unit);
            }
        } else {
            return new BigDecimal(dataSizeString).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP);
        }
    }


}