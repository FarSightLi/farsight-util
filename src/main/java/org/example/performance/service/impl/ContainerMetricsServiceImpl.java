package org.example.performance.service.impl;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;
import org.example.performance.pojo.bo.ContainerMetricsBO;
import org.example.performance.pojo.po.AlertRule;
import org.example.performance.pojo.po.ContainerInfo;
import org.example.performance.pojo.po.HostInfo;
import org.example.performance.pojo.vo.ContainerInfoVO;
import org.example.performance.pojo.vo.ContainerTrendVO;
import org.example.performance.service.*;
import org.example.performance.util.DataUtil;
import org.example.performance.util.ServiceUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author bonree
 * @description 针对表【container_metrics(容器性能表)】的数据库操作Service实现
 * @createDate 2023-12-06 14:14:04
 */
@Service
@Slf4j
public class ContainerMetricsServiceImpl implements ContainerMetricsService {
    @Resource
    private ContainerInfoService containerInfoService;
    @Resource
    private HostInfoService hostInfoService;
    @Resource
    private AlertRuleService alertRuleService;
    @Resource
    private MetricRecordService metricRecordService;

    @Override
    public List<ContainerInfoVO> getContainerMetricsByIp(String ip, LocalDateTime startTime, LocalDateTime endTime) {
        List<Object> infoAndMetricsList = getInfoAndMetricsList(ip, startTime, endTime);
        List<ContainerInfo> containerInfoList = (List<ContainerInfo>) infoAndMetricsList.get(0);
        List<ContainerMetricsBO> metricsList = (List<ContainerMetricsBO>) infoAndMetricsList.get(1);
        // 获得容器CPU为零的主机ip
        Set<String> hostIpList = containerInfoList.stream().filter(containerInfo -> containerInfo.getCpus().compareTo(BigDecimal.ZERO) == 0).map(ContainerInfo::getHostIp).collect(Collectors.toSet());
        // 主机id对应cpu数的map
        Map<String, Integer> hostCpuMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(hostIpList)) {
            List<HostInfo> hostInfoList = hostInfoService.lambdaQuery().in(HostInfo::getIp, hostIpList).select(HostInfo::getCpuCores, HostInfo::getIp).list();
            hostCpuMap = hostInfoList.stream().collect(Collectors.toMap(HostInfo::getIp, HostInfo::getCpuCores));
        }

        if (ObjectUtil.isEmpty(metricsList)) {
            throw new BusinessException(CodeMsg.PARAMETER_ERROR, "该时段没有相关的信息");
        }
        // 容器code对应的所有性能指标
        Map<Long, List<ContainerMetricsBO>> containerMetricsMap = metricsList.stream().collect(Collectors.groupingBy(ContainerMetricsBO::getCode));
        List<ContainerInfoVO> voList = new ArrayList<>();
        // 提供给Lambda使用
        Map<String, Integer> finalHostCpuMap = hostCpuMap;
        containerInfoList.forEach(containerInfo -> {
            ContainerInfoVO vo = new ContainerInfoVO();
            // 所有历史数据
            List<ContainerMetricsBO> containerMetricsBOList = containerMetricsMap.get(containerInfo.getId());
            if (ObjectUtil.isNotEmpty(containerMetricsBOList)) {
                // 拿到最新的数据
                ContainerMetricsBO containerMetricsBO = containerMetricsBOList.stream()
                        .filter(Objects::nonNull)
                        .max(Comparator.comparing(ContainerMetricsBO::getUpdateTime))
                        .orElseThrow(() -> new BusinessException(CodeMsg.SYSTEM_ERROR, "容器性能指标数据有错"));
                BeanUtils.copyProperties(containerMetricsBO, vo);
                BeanUtils.copyProperties(containerInfo, vo);
                // 计算三个指标的最大值及相关信息
                getThreeMaxIndex(containerMetricsBOList, vo, containerInfo, finalHostCpuMap);
                // 计算三个指标的平均值
                getThreeAvgIndex(containerMetricsBOList, vo, containerInfo);
                // 计算在线时长
                vo.setOnlineTime(calculateDurationMillis(vo.getRestartTime()));
                // 计算预警状态
                calculateState(vo);
                voList.add(vo);
            } else {
                log.warn("在{}到{}内主机{}没有{}的性能信息", startTime, endTime, ip, containerInfo.getContainerName());
            }
        });
        return voList;
    }

    @Override
    public List<ContainerTrendVO> getMetricTrend(String ip, LocalDateTime startTime, LocalDateTime endTime) {
        List<Object> infoAndMetricsList = getInfoAndMetricsList(ip, startTime, endTime);
        List<ContainerInfo> containerInfoList = (List<ContainerInfo>) infoAndMetricsList.get(0);
        List<ContainerMetricsBO> metricsList = (List<ContainerMetricsBO>) infoAndMetricsList.get(1);
        if (ObjectUtil.isEmpty(metricsList) || ObjectUtil.isEmpty(containerInfoList)) {
            return Collections.emptyList();
        }
        // 容器code对应的容器信息
        Map<Long, ContainerInfo> infoMap = containerInfoList.stream().collect(Collectors.toMap(ContainerInfo::getId, Function.identity()));
        // 容器code对应的指标数据
        Map<Long, List<ContainerMetricsBO>> metricsMap = metricsList.stream().collect(Collectors.groupingBy(ContainerMetricsBO::getCode));
        List<ContainerTrendVO> voList = new ArrayList<>();
        Map<String, AlertRule> ruleMap = alertRuleService.getRuleMap();
        infoMap.forEach((id, info) -> {
            voList.add(getContainerTrendVO(ContainerMetricsBO.MetricType.MEM, ruleMap, info, metricsMap.get(id)));
            voList.add(getContainerTrendVO(ContainerMetricsBO.MetricType.DISK, ruleMap, info, metricsMap.get(id)));
            voList.add(getContainerTrendVO(ContainerMetricsBO.MetricType.CPU, ruleMap, info, metricsMap.get(id)));
            voList.add(getContainerTrendVO(ContainerMetricsBO.MetricType.MEM_RATE, ruleMap, info, metricsMap.get(id)));
        });
        return voList;
    }

    private ContainerTrendVO getContainerTrendVO(ContainerMetricsBO.MetricType metricType,
                                                 Map<String, AlertRule> ruleMap,
                                                 ContainerInfo info,
                                                 List<ContainerMetricsBO> metricsList) {
        String desc = metricType.getDesc();
        String name = metricType.getName();
        String ruleKey = metricType.getRuleKey();

        ContainerTrendVO vo = new ContainerTrendVO();
        vo.setMetricDesc(desc);
        vo.setMetricName(name);
        vo.setTriggerWarnLimit(alertRuleService.getRuleValue(ruleMap.get(ruleKey), AlertRule::getWarningValue));
        vo.setTriggerErrorLimit(alertRuleService.getRuleValue(ruleMap.get(ruleKey), AlertRule::getErrorValue));

        List<ContainerTrendVO.MetricDataEntry> metricDataEntryList = new ArrayList<>();
        ContainerTrendVO.MetricDataEntry metricDataEntry = new ContainerTrendVO.MetricDataEntry();
        metricDataEntry.setTarget(info.getContainerName());
        metricDataEntry.setMetrics(getMetricValueList(metricsList, metricType, info.getMemSize()));
        metricDataEntryList.add(metricDataEntry);
        vo.setList(metricDataEntryList);
        return vo;
    }

    private void getThreeMaxIndex(List<ContainerMetricsBO> containerMetricsBOList, ContainerInfoVO vo, ContainerInfo containerInfo, Map<String, Integer> hostCpuMap) {
        BigDecimal maxCpuRate = containerMetricsBOList.stream()
                .filter(containerMetrics -> containerMetrics.getCpuRate() != null)
                .max(Comparator.comparing(ContainerMetricsBO::getCpuRate))
                .orElseThrow(() -> new BusinessException(CodeMsg.SYSTEM_ERROR, "容器性能指标数据有错")).getCpuRate();
        BigDecimal cpus = containerInfo.getCpus();
        // 代表没有限制,容器cpu即是主机cpu数
        if (cpus.compareTo(BigDecimal.ZERO) == 0) {
            Integer hostCpuCores = hostCpuMap.getOrDefault(containerInfo.getHostIp(), null);
            if (hostCpuCores == null) {
                log.error("没有获得容器的主机CPU信息，容器name：{}，主机id：{}", containerInfo.getContainerName(), containerInfo.getHostIp());
                throw new BusinessException(CodeMsg.SYSTEM_ERROR);
            }
            cpus = BigDecimal.valueOf(hostCpuCores);
            vo.setCpus(cpus);
        }
        BigDecimal maxCpuUsage = maxCpuRate.multiply(cpus);
        vo.setMaxCpuRate(maxCpuRate);
        vo.setMaxCpuUsage(maxCpuUsage);
        BigDecimal maxMemUsedSize = containerMetricsBOList.stream()
                .filter(e -> e.getMemUsedSize() != null)
                .max(Comparator.comparing(ContainerMetricsBO::getMemUsedSize))
                .orElseThrow(() -> new BusinessException(CodeMsg.SYSTEM_ERROR, "容器性能指标数据有错")).getMemUsedSize();
        BigDecimal memSize = containerInfo.getMemSize();
        if (memSize != null && memSize.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal maxMemUsedRate = maxMemUsedSize.divide(memSize, 1, RoundingMode.HALF_UP);
            vo.setMaxMemUsedRate(maxMemUsedRate);
        }
        vo.setMaxMemUsage(maxMemUsedSize);
        BigDecimal maxDiskUsedSize = containerMetricsBOList.stream()
                .filter(e -> e.getDiskUsedSize() != null)
                .max(Comparator.comparing(ContainerMetricsBO::getDiskUsedSize))
                .orElseThrow(() -> new BusinessException(CodeMsg.SYSTEM_ERROR, "容器性能指标数据有错")).getDiskUsedSize();
        BigDecimal diskSize = containerInfo.getDiskSize();
        if (diskSize != null && diskSize.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal maxDiskUsedRate = maxDiskUsedSize.divide(diskSize, 1, RoundingMode.HALF_UP);
            vo.setMaxDiskUsedRate(maxDiskUsedRate);
        }
        vo.setMaxDiskUsage(maxDiskUsedSize);
    }

    private void getThreeAvgIndex(List<ContainerMetricsBO> containerMetricsBOList, ContainerInfoVO vo, ContainerInfo containerInfo) {
        BigDecimal avgCpuRate = DataUtil.double2Decimal(containerMetricsBOList.stream()
                .map(ContainerMetricsBO::getCpuRate).filter(Objects::nonNull).mapToDouble(BigDecimal::doubleValue).average().orElse(0));
        BigDecimal avgDiskUsed = DataUtil.double2Decimal(containerMetricsBOList.stream()
                .map(ContainerMetricsBO::getDiskUsedSize).filter(Objects::nonNull).mapToDouble(BigDecimal::doubleValue).average().orElse(0));
        BigDecimal avgMemUsed = DataUtil.double2Decimal(containerMetricsBOList.stream()
                .map(ContainerMetricsBO::getMemUsedSize).filter(Objects::nonNull).mapToDouble(BigDecimal::doubleValue).average().orElse(0));
        BigDecimal cpuUsage = containerInfo.getCpus().multiply(avgCpuRate);
        vo.setAvgCpuUsage(cpuUsage.setScale(1, RoundingMode.HALF_UP));
        vo.setAvgCpuRate(avgCpuRate);
        BigDecimal memSize = vo.getMemSize();
        if (memSize != null && memSize.compareTo(BigDecimal.ZERO) != 0) {
            vo.setAvgMemRate(avgMemUsed.divide(memSize, 1, RoundingMode.HALF_UP));
        }
        vo.setAvgDiskUsage(avgDiskUsed);
        BigDecimal diskSize = vo.getDiskSize();
        if (diskSize != null && diskSize.compareTo(BigDecimal.ZERO) != 0) {
            vo.setAvgDiskRate(avgDiskUsed.divide(diskSize, 1, RoundingMode.HALF_UP));
        }
        vo.setAvgMemUsage(avgMemUsed);
    }

    private long calculateDurationMillis(LocalDateTime target) {
        if (target == null) {
            return 0L;
        }
        Instant providedInstant = target.atZone(ZoneId.systemDefault()).toInstant();
        Instant currentInstant = Instant.now();
        return currentInstant.toEpochMilli() - providedInstant.toEpochMilli();
    }

    private void calculateState(ContainerInfoVO vo) {
        List<AlertRule> ruleList = alertRuleService.list();
        if (ObjectUtil.isEmpty(ruleList)) {
            log.warn("没有配置预警规则");
            return;
        }
        // 预警名字对应详细信息的map
        Map<String, AlertRule> ruleMap = alertRuleService.getRuleMap();
        // CPU率
        setAlertState(vo, ContainerInfoVO.Field.CPU, "container.cpu.rate", ruleMap);
        // Mem率
        setAlertState(vo, ContainerInfoVO.Field.DISK, "container.mem.rate", ruleMap);
        // Disk使用率
        setAlertState(vo, ContainerInfoVO.Field.DISK, "container.disk.rate", ruleMap);
    }

    private void setAlertState(ContainerInfoVO vo, ContainerInfoVO.Field metricType, String ruleType, Map<String, AlertRule> ruleMap) {
        BigDecimal maxRate = vo.getMaxRate(metricType);
        BigDecimal avgRate = vo.getAvgRate(metricType);

        AlertRule rule = ruleMap.getOrDefault(ruleType, new AlertRule());

        // 设置最大Rate状态
        vo.setMaxState(metricType, getRateState(maxRate, BigDecimal.valueOf(rule.getErrorValue()), BigDecimal.valueOf(rule.getWarningValue())));

        // 设置平均Rate状态
        vo.setAvgState(metricType, getRateState(avgRate, BigDecimal.valueOf(rule.getErrorValue()), BigDecimal.valueOf(rule.getWarningValue())));
    }

    private Integer getRateState(BigDecimal rate, BigDecimal errorValue, BigDecimal warningValue) {
        int state;
        if (rate == null || errorValue == null || warningValue == null) {
            return null;
        }
        if (rate.compareTo(errorValue) > 0) {
            state = 3;
        } else if (rate.compareTo(warningValue) > 0) {
            state = 2;
        } else {
            state = 1;
        }
        return state;
    }

    /**
     * 根据IP和时间区间获得容器信息和容器性能
     *
     * @param ip
     * @param startTime
     * @param endTime
     * @return List<Object> 0号位是容器信息，1号位是容器性能
     */
    private List<Object> getInfoAndMetricsList(String ip, LocalDateTime startTime, LocalDateTime endTime) {
        Integer interval = ServiceUtil.getInterval(startTime, endTime);
        List<String> ips = new ArrayList<>();
        ips.add(ip);
        // ip 对应 容器id 的map
        Map<String, List<String>> ipContainerMap = containerInfoService.getContainerId(ips);
        List<String> containerIdList = ipContainerMap.get(ip);
        if (ObjectUtil.isEmpty(containerIdList)) {
            log.error("{}没有找到对应的容器信息", ip);
            throw new BusinessException(CodeMsg.SYSTEM_ERROR);
        }
        List<ContainerInfo> containerInfoList = containerInfoService.getListByContainerIdList(containerIdList);
        List<Long> uniqueIdList = containerInfoList.stream().map(ContainerInfo::getId).collect(Collectors.toList());
        List<ContainerMetricsBO> metricsList = metricRecordService.getContainerMetricBOList(uniqueIdList, startTime, endTime);
        Map<Long, List<ContainerMetricsBO>> groupByContainerId = metricsList.stream().collect(Collectors.groupingBy(ContainerMetricsBO::getCode));
        List<ContainerMetricsBO> newMetricsList = new ArrayList<>();
        // 根据时间间隔筛选数据
        groupByContainerId.forEach((k, v) -> {
            newMetricsList.addAll(ServiceUtil.filterListByTime(v, interval));
        });
        List<Object> objects = new ArrayList<>();
        objects.add(containerInfoList);
        objects.add(newMetricsList);
        return objects;
    }


    private ContainerTrendVO.MetricValue getMetricValueList(List<ContainerMetricsBO> metricsList,
                                                            ContainerMetricsBO.MetricType metricType, BigDecimal memSize) {
        ContainerTrendVO.MetricValue metricValue = new ContainerTrendVO.MetricValue();
        List<Object> list = new ArrayList<>();
        metricsList.forEach(e -> {
            list.add(e.getUpdateTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
            BigDecimal value = e.getValue(metricType, memSize);
            if (value == null) {
                log.warn("容器性能记录{}为null，容器id为{}", metricType, e.getCode());
            } else {
                list.add(value);
            }
            metricValue.setValue(list);
        });
        return metricValue;
    }
}




