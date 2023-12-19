package org.example.performance.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.pojo.bo.HostMetricsBO;
import org.example.performance.pojo.po.AlertRule;
import org.example.performance.pojo.vo.HostMetricsVO;
import org.example.performance.service.AlertRuleService;
import org.example.performance.service.HostInfoService;
import org.example.performance.service.HostMetricsService;
import org.example.performance.service.MetricRecordService;
import org.example.performance.util.MyUtil;
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
public class HostMetricsServiceImpl implements HostMetricsService {
    @Resource
    private HostInfoService hostInfoService;
    @Resource
    private AlertRuleService alertRuleService;
    @Resource
    private MetricRecordService metricRecordService;
    @Override
    public List<HostMetricsVO> getMetricsVO(String ip, LocalDateTime startTime, LocalDateTime endTime) {
        List<String> ipList = new ArrayList<>();
        ipList.add(ip);
        Map<String, Long> ip2IdMap = hostInfoService.getIp2IdMap(ipList);
        List<HostMetricsBO> hostMetricBOList = metricRecordService.getHostMetricBOList(ip2IdMap.get(ip), startTime, endTime);
        if (ObjectUtil.isEmpty(hostMetricBOList)) {
            log.info("ip:{}在{}和{}时段没有性能信息", ip, startTime, endTime);
            return Collections.emptyList();
        }

        Integer interval = MyUtil.getInterval(startTime, endTime);
        // 根据时间间隔过滤后的性能指标列表
        List<HostMetricsBO> filterHostMetricsBOList = MyUtil.filterListByTime(hostMetricBOList, interval);

        // 时间对应的性能信息map(极小概率会出现同一时间有多条性能数据)
        Map<LocalDateTime, HostMetricsBO> map = filterHostMetricsBOList.stream()
                .collect(Collectors.toMap(HostMetricsBO::getUpdateTime, Function.identity(), (old, replace) -> replace));
        List<AlertRule> alertRuleList = alertRuleService.list();
        // 告警信息map
        Map<String, AlertRule> alertMap = new HashMap<>(alertRuleList.size());
        if (ObjectUtil.isNotEmpty(alertRuleList)) {
            alertMap = alertRuleList.stream().collect(Collectors.toMap(AlertRule::getMetricName, Function.identity()));
        }
        List<HostMetricsVO> voList = new ArrayList<>();
        // 给lambda使用的map
        Map<String, AlertRule> finalAlertMap = alertMap;
        @lombok.Data
        @AllArgsConstructor
        class Data {
            String name;
            String type;
            String desc;
            HostMetricsBO.Type fieldType;
        }
        List<Data> dataList = new ArrayList<>();
        dataList.add(new Data("host.mem.sum", "mem", "主机内存使用量(MB)", HostMetricsBO.Type.MEM));
        dataList.add(new Data("host.network.bytin", "bytin", "主机网络流量IN(MB/s)", HostMetricsBO.Type.BYTIN));
        dataList.add(new Data("host.load.avg", "load", "主机负载(1min)", HostMetricsBO.Type.LOAD));
        dataList.add(new Data("host.cpu.rate", "cpu", "主机CPU使用率(%)", HostMetricsBO.Type.CPU));
        dataList.add(new Data("host.disk.rate", "disk", "主机磁盘使用率(/data，%)", HostMetricsBO.Type.DISK));
        dataList.add(new Data("host.network.tcp", "tcp", "主机TCP连接数", HostMetricsBO.Type.TCP));
        dataList.add(new Data("host.mem.rate", "mem_rate", "主机内存使用率(%)", HostMetricsBO.Type.MEM_RATE));
        dataList.add(new Data("host.disk.io.rate", "io", "主机磁盘IO使用率(/data，%)", HostMetricsBO.Type.MEM_RATE));
        dataList.add(new Data("host.disk.inode.rate", "inode", "主机磁盘INODE使用率(/data，%)", HostMetricsBO.Type.INODE));
        dataList.add(new Data("host.network.bytout", "bytout", "主机网络流量OUT(MB/s)", HostMetricsBO.Type.BYOUT));
        map.forEach((time, info) -> dataList.forEach(data -> voList.add(createHostMetricsVO(data.desc,
                data.name, data.type, info.getValueByType(data.fieldType), info.getUpdateTime(),
                finalAlertMap.getOrDefault(data.name, new AlertRule()).getWarningValue(),
                finalAlertMap.getOrDefault(data.name, new AlertRule()).getErrorValue()))));
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




