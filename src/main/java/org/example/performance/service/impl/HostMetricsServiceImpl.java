package org.example.performance.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.mapper.HostMetricsMapper;
import org.example.performance.pojo.po.AlertRule;
import org.example.performance.pojo.po.HostMetrics;
import org.example.performance.pojo.vo.HostMetricsVO;
import org.example.performance.service.AlertRuleService;
import org.example.performance.service.HostInfoService;
import org.example.performance.service.HostMetricsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author bonree
 * @description 针对表【host_metrics(主机指标表)】的数据库操作Service实现
 * @createDate 2023-12-06 14:14:04
 */
@Service
@Slf4j
public class HostMetricsServiceImpl extends ServiceImpl<HostMetricsMapper, HostMetrics>
        implements HostMetricsService {
    @Resource
    private HostInfoService hostInfoService;
    @Resource
    private AlertRuleService alertRuleService;

    @Override
    public void insertBatch(List<HostMetrics> hostMetricsList) {
        Map<String, Integer> ip2IdMap = hostInfoService.getIp2IdMap(hostMetricsList.stream().map(HostMetrics::getHostIp).collect(Collectors.toList()));
        hostMetricsList.forEach(hostMetrics -> {
            hostMetrics.setHostId(ip2IdMap.get(hostMetrics.getHostIp()));
            hostMetrics.setUpdateTime(LocalDateTime.now());
        });
        baseMapper.insertBatch(hostMetricsList);
    }

    @Override
    public List<HostMetricsVO> getMetricsVO(String ip, LocalDateTime startTime, LocalDateTime endTime) {
        // TODO 可以针对时间间隔做优化
        List<String> ipList = new ArrayList<>();
        ipList.add(ip);
        Map<String, Integer> ip2IdMap = hostInfoService.getIp2IdMap(ipList);
        List<HostMetrics> hostMetricsList = baseMapper.selectByHostId(ip2IdMap.get(ip), startTime, endTime);
        if (ObjectUtil.isEmpty(hostMetricsList)) {
            log.info("ip:{}在{}和{}时段没有性能信息", ip, startTime, endTime);
            return Collections.emptyList();
        }
        // 时间对应的性能信息map(极小概率会出现同一时间有多条性能数据)
        Map<LocalDateTime, HostMetrics> map = hostMetricsList.stream()
                .collect(Collectors.toMap(HostMetrics::getUpdateTime, Function.identity(), (old, replace) -> replace));
        List<AlertRule> alertRuleList = alertRuleService.list();
        // 告警信息map
        Map<String, AlertRule> alertMap = new HashMap<>(alertRuleList.size());
        if (ObjectUtil.isNotEmpty(alertRuleList)) {
            alertMap = alertRuleList.stream().collect(Collectors.toMap(AlertRule::getMetricName, Function.identity()));
        }
        List<HostMetricsVO> voList = new ArrayList<>();
        // 给lambda使用的map
        Map<String, AlertRule> finalAlertMap = alertMap;
        map.forEach((time, info) -> {
            // TODO 优化硬编码
            String memSumName = "host.mem.sum";
            voList.add(createHostMetricsVO("主机内存使用量(MB)", memSumName, "mem",
                    info.getMem(),
                    info.getUpdateTime(),
                    finalAlertMap.getOrDefault(memSumName, new AlertRule()).getWarningValue(),
                    finalAlertMap.getOrDefault(memSumName, new AlertRule()).getErrorValue()));

            String bytinName = "host.network.bytin";
            voList.add(createHostMetricsVO("主机网络流量IN(MB/s)", bytinName, "bytin",
                    info.getByteIn(),
                    info.getUpdateTime(),
                    finalAlertMap.getOrDefault(bytinName, new AlertRule()).getWarningValue(),
                    finalAlertMap.getOrDefault(bytinName, new AlertRule()).getErrorValue()));

            String loadName = "host.load.avg";
            voList.add(createHostMetricsVO("主机内存使用量(1min)", loadName, "load",
                    info.getHostLoad(),
                    info.getUpdateTime(),
                    finalAlertMap.getOrDefault(loadName, new AlertRule()).getWarningValue(),
                    finalAlertMap.getOrDefault(loadName, new AlertRule()).getErrorValue()));

            String cpuRateName = "host.cpu.rate";
            voList.add(createHostMetricsVO("主机CPU使用率(%)", cpuRateName, "cpu",
                    info.getHostLoad(),
                    info.getUpdateTime(),
                    finalAlertMap.getOrDefault(cpuRateName, new AlertRule()).getWarningValue(),
                    finalAlertMap.getOrDefault(cpuRateName, new AlertRule()).getErrorValue()));

            String diskRateName = "host.disk.rate";
            voList.add(createHostMetricsVO("主机磁盘使用率(/data，%)", diskRateName, "disk",
                    info.getDisk(),
                    info.getUpdateTime(),
                    finalAlertMap.getOrDefault(diskRateName, new AlertRule()).getWarningValue(),
                    finalAlertMap.getOrDefault(diskRateName, new AlertRule()).getErrorValue()));

            String tcpName = "host.network.tcp";
            voList.add(createHostMetricsVO("主机TCP连接数", tcpName, "tcp",
                    info.getTcp(),
                    info.getUpdateTime(),
                    finalAlertMap.getOrDefault(tcpName, new AlertRule()).getWarningValue(),
                    finalAlertMap.getOrDefault(tcpName, new AlertRule()).getErrorValue()));

            String memRateName = "host.mem.rate";
            voList.add(createHostMetricsVO("主机内存使用率(%)", memRateName, "mem_rate",
                    info.getMemRate(),
                    info.getUpdateTime(),
                    finalAlertMap.getOrDefault(memRateName, new AlertRule()).getWarningValue(),
                    finalAlertMap.getOrDefault(memRateName, new AlertRule()).getErrorValue()));

            String ioRateName = "host.disk.io.rate";
            voList.add(createHostMetricsVO("主机磁盘IO使用率(/data，%)", ioRateName, "io",
                    info.getIo(),
                    info.getUpdateTime(),
                    finalAlertMap.getOrDefault(ioRateName, new AlertRule()).getWarningValue(),
                    finalAlertMap.getOrDefault(ioRateName, new AlertRule()).getErrorValue()));

            String inodeRateName = "host.disk.inode.rate";
            voList.add(createHostMetricsVO("主机磁盘INODE使用率(/data，%)", inodeRateName, "io",
                    info.getInode(),
                    info.getUpdateTime(),
                    finalAlertMap.getOrDefault(inodeRateName, new AlertRule()).getWarningValue(),
                    finalAlertMap.getOrDefault(inodeRateName, new AlertRule()).getErrorValue()));

            String bytoutName = "host.disk.inode.rate";
            voList.add(createHostMetricsVO("主机网络流量OUT(MB/s)", bytoutName, "io",
                    info.getByteOut(),
                    info.getUpdateTime(),
                    finalAlertMap.getOrDefault(bytoutName, new AlertRule()).getWarningValue(),
                    finalAlertMap.getOrDefault(bytoutName, new AlertRule()).getErrorValue()));
        });
        return voList;
    }

    private HostMetricsVO createHostMetricsVO(String desc, String name,
                                              String type, BigDecimal value,
                                              LocalDateTime monitorTime,
                                              BigDecimal warnLimit, BigDecimal errorLimit) {
        HostMetricsVO vo = new HostMetricsVO();
        vo.setDesc(desc);
        vo.setName(name);
        vo.setType(type);
        vo.setValue(value);
        vo.setMonitorTime(monitorTime);
        vo.setTriggerWarnLimit(warnLimit);
        vo.setTriggerErrorLimit(errorLimit);
        if (warnLimit != null && errorLimit != null) {
            if (value.compareTo(errorLimit) > 0) {
                vo.setState(3);
            } else if (value.compareTo(warnLimit) > 0) {
                vo.setState(2);
            } else {
                vo.setState(1);
            }
        }
        return vo;
    }
}




