package org.example.performance.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.pojo.bo.HostMetricsBO;
import org.example.performance.pojo.po.AlertRule;
import org.example.performance.pojo.vo.HostMetricsVO;
import org.example.performance.service.AlertRuleService;
import org.example.performance.service.HostInfoService;
import org.example.performance.service.HostMetricsService;
import org.example.performance.service.MetricRecordService;
import org.example.performance.util.ServiceUtil;
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

        Integer interval = ServiceUtil.getInterval(startTime, endTime);
        // 根据时间间隔过滤后的性能指标列表
        List<HostMetricsBO> filterHostMetricsBOList = ServiceUtil.filterListByTime(hostMetricBOList, interval);

        // 时间对应的性能信息map(极小概率会出现同一时间有多条性能数据)
        Map<LocalDateTime, HostMetricsBO> map = filterHostMetricsBOList.stream()
                .collect(Collectors.toMap(HostMetricsBO::getUpdateTime, Function.identity(), (old, replace) -> replace));
        // 告警信息map
        Map<String, AlertRule> alertMap = alertRuleService.getRuleMap();
        List<HostMetricsVO> voList = new ArrayList<>();
        List<HostMetricsBO.FiledType> filedTypeList = Arrays.stream(HostMetricsBO.FiledType.values()).collect(Collectors.toList());
        map.forEach((time, info) -> filedTypeList.forEach(fieldType -> voList.add(
                createHostMetricsVO(fieldType, info.getValueByType(fieldType), info.getUpdateTime(),
                        alertRuleService.getRuleValue(alertMap.get(fieldType.getName()), AlertRule::getWarningValue),
                        alertRuleService.getRuleValue(alertMap.get(fieldType.getName()), AlertRule::getErrorValue)))));

        return voList;
    }

    private HostMetricsVO createHostMetricsVO(HostMetricsBO.FiledType fieldType, BigDecimal value,
                                              LocalDateTime monitorTime,
                                              BigDecimal warnLimit, BigDecimal errorLimit) {
        HostMetricsVO vo = new HostMetricsVO();
        vo.setDesc(fieldType.getDesc());
        vo.setName(fieldType.getName());
        vo.setType(fieldType.getType());
        vo.setValue(value);
        vo.setMonitorTime(monitorTime);
        // 即该项指标有预警规则
        if (warnLimit != null && errorLimit != null) {
            warnLimit = warnLimit.setScale(1);
            errorLimit = errorLimit.setScale(1);
            vo.setTriggerWarnLimit(warnLimit);
            vo.setTriggerErrorLimit(errorLimit);
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




