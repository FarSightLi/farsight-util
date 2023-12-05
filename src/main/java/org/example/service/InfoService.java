package org.example.service;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.extra.ssh.JschUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.example.component.InfoCache;
import org.example.po.ContainerInfo;
import org.example.po.DiskInfo;
import org.example.po.SysTemChartInfo;
import org.example.po.SystemInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    public void getSysInfo(Session session, String ip) {
        SystemInfo systemInfo = new SystemInfo();
        systemInfo.setIp(ip);
        systemInfo.setCpuCores(Integer.valueOf(execCmd(session, "lscpu | awk '/^CPU\\(s\\):/ {print $2}'")));
        systemInfo.setCpuArch(execCmd(session, "lscpu | awk '/^Architecture:/ {print $2}'"));
        systemInfo.setSysVersion(execCmd(session, "cat /etc/redhat-release"));
        String memByteSize = execCmd(session, "tsar --mem -C -s total | awk -F= '{print $2}'");
        systemInfo.setMemSize(parseDataSize(memByteSize));
        systemInfo.setKernelRelease(execCmd(session, "uname -r"));
        List<String> containerIds = Arrays.stream(JschUtil.exec(session, "docker ps -a | awk 'FNR>1 {print $1}'", null).split("\n")).collect(Collectors.toList());
        InfoCache.CONTAINER_MAP.put(ip, containerIds);
        String diskInfo = execCmd(session, "df -h | awk '$NF == \"/\" || $NF ~ /^\\/[^\\/]+$/ {print $2,$3,$6}'");
        String diskInodeInfo = execCmd(session, "df -h -i | awk '$NF == \"/\" || $NF ~ /^\\/[^\\/]+$/ {print $5,$6}'");
        systemInfo.setDiskInfo(getDiskInfo(diskInfo, diskInodeInfo));
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
        sysTemChartInfo.setMem(parseDataSize(execCmd(session, "tsar --mem -C -s  used  | awk -F= '{print $2}'")));
        sysTemChartInfo.setByteIn(parseDataSize(execCmd(session, "cat /proc/net/dev | grep ens | awk '{print $2}'")));
        sysTemChartInfo.setLoad(string2Decimal(execCmd(session, "tsar --load -C -s load1 |  awk -F= '{print $2}'")));
        sysTemChartInfo.setByteOut(parseDataSize(execCmd(session, "cat /proc/net/dev | grep ens | awk '{print $10} '")));
        sysTemChartInfo.setIo(string2Decimal(execCmd(session, "iostat -x | awk '/%util/ {getline; print $NF}'")));
        sysTemChartInfo.setCpu(string2Decimal(execCmd(session, "tsar --cpu -C -s util |  awk -F= '{print $2}'")));
        sysTemChartInfo.setDisk(execCmd(session, "df -h | awk '$NF == \"/\" {print $5}'"));
        sysTemChartInfo.setInode(execCmd(session, "df -h -i| awk '$NF == \"/\" {print $5}'"));
        sysTemChartInfo.setTcp(string2Decimal(execCmd(session, "netstat -nat | grep tcp | wc -l")));
        try {
            log.info(ip + "性能指标：\n" + new ObjectMapper().writeValueAsString(sysTemChartInfo));
        } catch (JsonProcessingException e) {
            log.info(e.getMessage());
        }
    }


    public void getContainerInfo(Session session, String containerId, String ip) {
        ContainerInfo containerInfo = new ContainerInfo();
        try {
            String details = execCmd(session, String.format("docker ps -a --format json  --filter 'id=%s'  --size", containerId));
            HashMap<String, String> detailsMap = new ObjectMapper().readValue(details, HashMap.class);
            containerInfo.setState(detailsMap.get("State"));
            String names = detailsMap.get("Names");
            if (names.contains("_")) {
                String[] split = names.split("_");
                containerInfo.setVersion(split[1]);
                containerInfo.setContainerName(split[0]);
            } else {
                log.error("容器名称不符合规范");
            }
            containerInfo.setIp(ip);
            containerInfo.setContainerId(containerId);
            String diskSize = execCmd(session, String.format("docker stats %s --no-stream | awk 'NR>1 {print $11} '", containerId));
            BigDecimal mbDiskSize = parseDataSize(diskSize);
            containerInfo.setDiskSize(mbDiskSize);
            String diskUsed = execCmd(session, String.format("docker system df -v | grep %s | awk '{print $5}'", containerId));
            BigDecimal mbDiskUsed = parseDataSize(diskUsed);
            if (mbDiskSize.compareTo(BigDecimal.ZERO) == 0) {
                containerInfo.setDiskUsedRate(string2Decimal("0"));
            } else {
                containerInfo.setDiskUsedRate(mbDiskUsed.divide(mbDiskSize, 2, RoundingMode.HALF_UP));
            }
            // 如果为0代表没有限制CPU
            String cpus = execCmd(session, String.format("docker inspect %s | grep -i nanocpus | awk '{print $2/1000000000}'", containerId));
            containerInfo.setCpus(Double.parseDouble(cpus));
            String cpuRate = execCmd(session, String.format("docker stats %s --no-stream | awk 'NR>1 {print $3} '", containerId));
            containerInfo.setCpuRate(Double.parseDouble(getNum(cpuRate)));
            String memSize = execCmd(session, String.format("docker stats %s --no-stream | awk 'NR>1 {print $6} '", containerId));
            BigDecimal mbMemSize = parseDataSize(memSize);
            containerInfo.setMemSize(mbMemSize);
            // 这里已经是百分比
            String memUsed = execCmd(session, String.format("docker stats %s --no-stream | awk 'NR>1 {print $7} '", containerId));
            containerInfo.setMemUsedRate(string2Decimal(getNum(memUsed)));
            String imageSize = execCmd(session, String.format("docker images `docker system df -v | awk '/%s/ {print $2}'` | awk 'END{print $NF}'", containerId));
            containerInfo.setImageSize(parseDataSize(imageSize));
            String onlineTime = execCmd(session, String.format("docker inspect --format '{{.State.StartedAt}}' %s", containerId));
            containerInfo.setRestartTime(getTime(onlineTime));
            String creatTime = execCmd(session, String.format("docker inspect --format '{{.Created}}' %s", containerId));
            containerInfo.setCreateTime(getTime(creatTime));
            if ("running".equals(containerInfo.getState())) {
                containerInfo.setOnlineTime(calculateDurationMillis(getTime(onlineTime)));
            } else  {
                containerInfo.setOnlineTime(0L);
            }

        } catch (Exception e) {
            log.error(ip + "的" + containerInfo.getContainerName() + "信息出错了");
            log.error(e.getMessage());
        }
        try {
            log.info(ip + " 的 " + containerInfo.getContainerName() + ":\n" + new ObjectMapper().writeValueAsString(containerInfo));
        } catch (JsonProcessingException e) {
            log.error("json解析出错---" + e.getMessage());
        }
    }

    private String execCmd(Session session, String cmd) {
        return JschUtil.exec(session, cmd, CharsetUtil.CHARSET_UTF_8, System.err).trim();
    }

    private DiskInfo getDiskInfo(String strInfo, String diskInodeInfo) {
        Pattern pattern1 = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\S+)");
        Pattern pattern2 = Pattern.compile("(\\S+)\\s+(\\S+)");
        Matcher matcher1 = pattern1.matcher(strInfo);
        Matcher matcher2 = pattern2.matcher(diskInodeInfo);
        DiskInfo diskInfo = new DiskInfo();
        List<DiskInfo.DiskDetail> diskDetailList = new ArrayList<>();
        BigDecimal totalSize = string2Decimal("0");
        BigDecimal usedRate = string2Decimal("0");
        BigDecimal usedSize = string2Decimal("0");
        while (matcher1.find() && matcher2.find()) {
            DiskInfo.DiskDetail diskDetail = new DiskInfo.DiskDetail();
            String size = matcher1.group(1);
            String used = matcher1.group(2);
            String name = matcher1.group(3);
            String inodeUsed = matcher2.group(1);
            diskDetail.setInodeUsedRate(string2Decimal(getNum(inodeUsed)));
            diskDetail.setDfName(name);
            BigDecimal mbDiskSize = parseDataSize(size);
            diskDetail.setDfSize(mbDiskSize);
            BigDecimal mbDiskUsedSize = parseDataSize(used);
            diskDetail.setDiskUsedSize(mbDiskUsedSize);
            if (mbDiskUsedSize.compareTo(BigDecimal.ZERO) == 0) {
                diskDetail.setDiskUsedRate(string2Decimal("0"));
            } else {
                diskDetail.setDiskUsedRate(mbDiskUsedSize.divide(mbDiskUsedSize, RoundingMode.HALF_UP));
            }
            diskDetailList.add(diskDetail);
            totalSize = totalSize.add(mbDiskSize);
            usedSize = usedSize.add(mbDiskUsedSize);
        }
        if (usedSize.compareTo(BigDecimal.ZERO) == 0) {
            usedRate = string2Decimal("0");
        } else {
            usedRate = usedSize.divide(totalSize, RoundingMode.HALF_UP);
        }
        diskInfo.setPartitions(diskDetailList);
        diskInfo.setUsedRate(usedRate);
        diskInfo.setTotalSize(totalSize);
        diskInfo.setUsedSize(usedSize);
        return diskInfo;
    }

    private BigDecimal string2Decimal(String value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        return bigDecimal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 提取数字部分
     *
     * @param target
     * @return
     */
    private String getNum(String target) {
        String regEx = "[^0-9]";
        return Pattern.compile(regEx).matcher(target).replaceAll("").trim();
    }

    private LocalDateTime getTime(String stringTime) {
        return LocalDateTime.parse(stringTime, DateTimeFormatter.ISO_DATE_TIME);
    }

    private long calculateDurationMillis(LocalDateTime target) {
        Instant providedInstant = target.atZone(ZoneId.systemDefault()).toInstant();
        Instant currentInstant = Instant.now();
        return currentInstant.toEpochMilli() - providedInstant.toEpochMilli();
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
                    return value.divide(BigDecimal.valueOf(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP);
                case "KB":
                case "K":
                    return value.divide(BigDecimal.valueOf(1024), 2, BigDecimal.ROUND_HALF_UP);
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
            return new BigDecimal(dataSizeString).divide(BigDecimal.valueOf(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP);
        }
    }


}
