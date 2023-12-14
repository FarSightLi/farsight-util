package org.example.performance.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;
import org.example.performance.mapper.ContainerMetricsMapper;
import org.example.performance.pojo.po.AlertRule;
import org.example.performance.pojo.po.ContainerInfo;
import org.example.performance.pojo.po.ContainerMetrics;
import org.example.performance.pojo.po.HostInfo;
import org.example.performance.pojo.vo.ContainerInfoVO;
import org.example.performance.pojo.vo.ContainerTrendVO;
import org.example.performance.service.AlertRuleService;
import org.example.performance.service.ContainerInfoService;
import org.example.performance.service.ContainerMetricsService;
import org.example.performance.service.HostInfoService;
import org.example.performance.util.DataUtil;
import org.example.performance.util.MyUtil;
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
public class ContainerMetricsServiceImpl extends ServiceImpl<ContainerMetricsMapper, ContainerMetrics>
        implements ContainerMetricsService {
    @Resource
    private ContainerInfoService containerInfoService;
    @Resource
    private HostInfoService hostInfoService;
    @Resource
    private AlertRuleService alertRuleService;

    @Override
    public void insertBatch(List<ContainerMetrics> containerMetricsList) {
        containerMetricsList.forEach(containerMetrics -> containerMetrics.setUpdateTime(LocalDateTime.now()));
        baseMapper.insertBatch(containerMetricsList);
    }

    @Override
    public List<ContainerInfoVO> getContainerMetricsByIp(String ip, LocalDateTime startTime, LocalDateTime endTime) {
        List<Object> infoAndMetricsList = getInfoAndMetricsList(ip, startTime, endTime);
        List<ContainerInfo> containerInfoList = (List<ContainerInfo>) infoAndMetricsList.get(0);
        List<ContainerMetrics> metricsList = (List<ContainerMetrics>) infoAndMetricsList.get(1);
        // 获得容器CPU为零的主机id
        Set<Integer> hostIdList = containerInfoList.stream().filter(containerInfo -> containerInfo.getCpus().compareTo(BigDecimal.ZERO) == 0).map(ContainerInfo::getHostId).collect(Collectors.toSet());
        // 主机id对应cpu数的map
        Map<Integer, Integer> hostCpuMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(hostIdList)) {
            List<HostInfo> hostInfoList = hostInfoService.lambdaQuery().in(HostInfo::getId, hostIdList).select(HostInfo::getCpuCores, HostInfo::getId).list();
            if (ObjectUtil.isNotEmpty(hostInfoList)) {
                hostCpuMap = hostInfoList.stream().collect(Collectors.toMap(HostInfo::getId, HostInfo::getCpuCores));
            }
        }

        if (ObjectUtil.isEmpty(metricsList)) {
            throw new BusinessException(CodeMsg.PARAMETER_ERROR, "该时段没有相关的信息");
        }
        // 容器id对应的所有性能指标
        Map<String, List<ContainerMetrics>> containerMetricsMap = metricsList.stream().collect(Collectors.groupingBy(ContainerMetrics::getContainerId));
        List<ContainerInfoVO> voList = new ArrayList<>();
        // 提供给Lambda使用
        Map<Integer, Integer> finalHostCpuMap = hostCpuMap;
        containerInfoList.forEach(containerInfo -> {
            ContainerInfoVO vo = new ContainerInfoVO();
            // 所有历史数据
            List<ContainerMetrics> containerMetricsList = containerMetricsMap.get(containerInfo.getContainerId());
            // 拿到最新的数据
            ContainerMetrics containerMetrics = containerMetricsList.stream()
                    .filter(Objects::nonNull)
                    .max(Comparator.comparing(ContainerMetrics::getUpdateTime))
                    .orElseThrow(() -> new BusinessException(CodeMsg.SYSTEM_ERROR, "容器性能指标数据有错"));
            BeanUtils.copyProperties(containerMetrics, vo);
            BeanUtils.copyProperties(containerInfo, vo);
            // 计算三个指标的最大值及相关信息
            getThreeMaxIndex(containerMetricsList, vo, containerInfo, finalHostCpuMap);
            // 计算三个指标的平均值
            getThreeAvgIndex(containerMetricsList, vo, containerInfo);
            // 计算在线时长
            vo.setOnlineTime(calculateDurationMillis(vo.getRestartTime()));
            // 计算预警状态
            calculateState(vo);
            voList.add(vo);
        });
        return voList;
    }

    @Override
    public List<ContainerTrendVO> getMetricTrend(String ip, LocalDateTime startTime, LocalDateTime endTime) {
        List<Object> infoAndMetricsList = getInfoAndMetricsList(ip, startTime, endTime);
        List<ContainerInfo> containerInfoList = (List<ContainerInfo>) infoAndMetricsList.get(0);
        List<ContainerMetrics> metricsList = (List<ContainerMetrics>) infoAndMetricsList.get(1);
        List<ContainerTrendVO> voList = new ArrayList<>();
        Map<String, AlertRule> ruleMap = alertRuleService.list().stream().collect(Collectors.toMap(AlertRule::getMetricName, Function.identity()));
        containerInfoList.forEach(info -> {
            voList.add(getContainerTrendVO(ContainerMetrics.Type.MEM, ruleMap, info, metricsList));
            voList.add(getContainerTrendVO(ContainerMetrics.Type.DISK, ruleMap, info, metricsList));
            voList.add(getContainerTrendVO(ContainerMetrics.Type.CPU, ruleMap, info, metricsList));
            voList.add(getContainerTrendVO(ContainerMetrics.Type.MEM_RATE, ruleMap, info, metricsList));

        });

        return voList;
    }

    private ContainerTrendVO getContainerTrendVO(ContainerMetrics.Type type,
                                                 Map<String, AlertRule> ruleMap,
                                                 ContainerInfo info,
                                                 List<ContainerMetrics> metricsList) {
        String desc = "";
        String name = "";
        String ruleKey = "";
        if (type.equals(ContainerMetrics.Type.CPU)) {
            desc = "容器CPU使用率(相对于limit，%)";
            name = "cpu";
            ruleKey = "container.cpu.rate";
        } else if (type.equals(ContainerMetrics.Type.MEM)) {
            desc = "容器内存使用量(MB)";
            name = "mem";
            ruleKey = "container.mem.sum";
        } else if (type.equals(ContainerMetrics.Type.DISK)) {
            desc = "容器磁盘使用量(MB)";
            name = "disk";
            ruleKey = "container.disk.sum";
        } else {
            desc = "容器内存使用率(相对于limit，%)";
            name = "mem_rate";
            ruleKey = "container.mem.rate";
        }

        ContainerTrendVO vo = new ContainerTrendVO();
        vo.setMetricDesc(desc);
        vo.setMetricName(name);
        vo.setTriggerWarnLimit(ruleMap.getOrDefault(ruleKey, new AlertRule()).getWarningValue());
        vo.setTriggerErrorLimit(ruleMap.getOrDefault(ruleKey, new AlertRule()).getErrorValue());

        List<ContainerTrendVO.MetricDataEntry> metricDataEntryList = new ArrayList<>();
        ContainerTrendVO.MetricDataEntry metricDataEntry = new ContainerTrendVO.MetricDataEntry();
        metricDataEntry.setTarget(info.getContainerName());
        metricDataEntry.setMetrics(getMetricValueList(metricsList, type, info.getMemSize()));
        metricDataEntryList.add(metricDataEntry);
        vo.setList(metricDataEntryList);
        return vo;
    }

    private void getThreeMaxIndex(List<ContainerMetrics> containerMetricsList, ContainerInfoVO vo, ContainerInfo containerInfo, Map<Integer, Integer> hostCpuMap) {
        BigDecimal maxCpuRate = containerMetricsList.stream()
                .filter(containerMetrics -> containerMetrics.getCpuRate() != null)
                .max(Comparator.comparing(ContainerMetrics::getCpuRate))
                .orElseThrow(() -> new BusinessException(CodeMsg.SYSTEM_ERROR, "容器性能指标数据有错")).getCpuRate();
        BigDecimal cpus = containerInfo.getCpus();
        // 代表没有限制,容器cpu即是主机cpu数
        if (cpus.compareTo(BigDecimal.ZERO) == 0) {
            Integer hostCpuCores = hostCpuMap.getOrDefault(containerInfo.getHostId(), null);
            if (hostCpuCores == null) {
                log.error("没有获得容器的主机CPU信息，容器name：{}，主机id：{}", containerInfo.getContainerName(), containerInfo.getHostId());
                throw new BusinessException(CodeMsg.SYSTEM_ERROR);
            }
            cpus = BigDecimal.valueOf(hostCpuCores);
            vo.setCpus(cpus);
        }
        BigDecimal maxCpuUsage = maxCpuRate.multiply(cpus);
        vo.setMaxCpuRate(maxCpuRate);
        vo.setMaxCpuUsage(maxCpuUsage);
        BigDecimal maxMemUsedSize = containerMetricsList.stream()
                .filter(e -> e.getMemUsedSize() != null)
                .max(Comparator.comparing(ContainerMetrics::getMemUsedSize))
                .orElseThrow(() -> new BusinessException(CodeMsg.SYSTEM_ERROR, "容器性能指标数据有错")).getMemUsedSize();
        BigDecimal maxMemUsedRate = maxMemUsedSize.divide(containerInfo.getMemSize(), 1, RoundingMode.HALF_UP);
        vo.setMaxMemUsage(maxMemUsedSize);
        vo.setMaxMemUsedRate(maxMemUsedRate);
        BigDecimal maxDiskUsedSize = containerMetricsList.stream()
                .filter(e -> e.getDiskUsedSize() != null)
                .max(Comparator.comparing(ContainerMetrics::getDiskUsedSize))
                .orElseThrow(() -> new BusinessException(CodeMsg.SYSTEM_ERROR, "容器性能指标数据有错")).getDiskUsedSize();
        BigDecimal maxDiskUsedRate = maxDiskUsedSize.divide(containerInfo.getDiskSize(), 1, RoundingMode.HALF_UP);
        vo.setMaxDiskUsage(maxDiskUsedSize);
        vo.setMaxDiskUsedRate(maxDiskUsedRate);
    }

    private void getThreeAvgIndex(List<ContainerMetrics> containerMetricsList, ContainerInfoVO vo, ContainerInfo containerInfo) {
        BigDecimal avgCpuRate = DataUtil.double2Decimal(containerMetricsList.stream()
                .map(ContainerMetrics::getCpuRate).filter(Objects::nonNull).mapToDouble(BigDecimal::doubleValue).average().orElse(0));
        BigDecimal avgDiskUsed = DataUtil.double2Decimal(containerMetricsList.stream()
                .map(ContainerMetrics::getDiskUsedSize).filter(Objects::nonNull).mapToDouble(BigDecimal::doubleValue).average().orElse(0));
        BigDecimal avgMemUsed = DataUtil.double2Decimal(containerMetricsList.stream()
                .map(ContainerMetrics::getMemUsedSize).filter(Objects::nonNull).mapToDouble(BigDecimal::doubleValue).average().orElse(0));
        BigDecimal cpuUsage = containerInfo.getCpus().multiply(avgCpuRate);
        vo.setAvgCpuUsage(cpuUsage.setScale(1, RoundingMode.HALF_UP));
        vo.setAvgCpuRate(avgCpuRate);
        vo.setAvgMemRate(avgMemUsed.divide(vo.getMemSize(), 1, RoundingMode.HALF_UP));
        vo.setAvgDiskUsage(avgDiskUsed);
        vo.setAvgDiskRate(avgDiskUsed.divide(vo.getDiskSize(), 1, RoundingMode.HALF_UP));
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
        Map<String, AlertRule> ruleMap = ruleList.stream().collect(Collectors.toMap(AlertRule::getMetricName, Function.identity()));
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
        vo.setMaxState(metricType, getRateState(maxRate, rule.getErrorValue(), rule.getWarningValue()));

        // 设置平均Rate状态
        vo.setAvgState(metricType, getRateState(avgRate, rule.getErrorValue(), rule.getWarningValue()));
    }

    private Integer getRateState(BigDecimal rate, BigDecimal errorValue, BigDecimal warningValue) {
        int state;
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
        Integer interval = MyUtil.getInterval(startTime, endTime);
        List<String> ips = new ArrayList<>();
        ips.add(ip);
        Map<String, List<String>> ipContainerMap = containerInfoService.getContainerId(ips);
        List<String> containerIdList = ipContainerMap.get(ip);
        if (ObjectUtil.isEmpty(containerIdList)) {
            log.error("{}没有找到对应的容器信息", ip);
            throw new BusinessException(CodeMsg.SYSTEM_ERROR);
        }
        List<ContainerInfo> containerInfoList = containerInfoService.getListByContainerIdList(containerIdList);
        List<ContainerMetrics> metricsList = baseMapper.getByContainerIdList(containerIdList, startTime, endTime);
        // 根据时间间隔筛选数据
        List<ContainerMetrics> newMetricsList = MyUtil.filterListByTime(metricsList, interval);
        List<Object> objects = new ArrayList<>();
        objects.add(containerInfoList);
        objects.add(newMetricsList);
        return objects;
    }


    private ContainerTrendVO.MetricValue getMetricValueList(List<ContainerMetrics> metricsList,
                                                            ContainerMetrics.Type type, BigDecimal memSize) {
        ContainerTrendVO.MetricValue metricValue = new ContainerTrendVO.MetricValue();
        List<Object> list = new ArrayList<>();
        metricsList.forEach(e -> {
            list.add(e.getUpdateTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
            BigDecimal value = e.getValue(type, memSize);
            if (value == null) {
                log.error("容器性能记录{}为null，记录id为{}", type, e.getId());
            } else {
                list.add(e.getMemUsedSize());
            }
            metricValue.setValue(list);
        });
//        if (type.equals(ContainerMetrics.Type.MEM)) {
//            metricsList.forEach(e -> {
//                list.add(e.getUpdateTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
//                if (e.getMemUsedSize() == null || memSize == null) {
//
//                }
//                metricValue.setValue(list);
//            });
//        } else if (type.equals(ContainerMetrics.Type.CPU)) {
//            metricsList.forEach(e -> {
//                list.add(e.getUpdateTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
//                if (e.getCpuRate() == null || memSize == null) {
//                    log.error("容器性能记录CpuRate为null，记录id为{}", e.getId());
//                } else {
//                    list.add(e.getCpuRate());
//                }
//                metricValue.setValue(list);
//            });
//        } else if (type.equals(ContainerMetrics.Type.DISK)) {
//            metricsList.forEach(e -> {
//                list.add(e.getUpdateTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
//                if (e.getDiskUsedSize() == null || memSize == null) {
//                    log.error("容器性能记录MemUsedSize为null，记录id为{}", e.getId());
//                } else {
//                    list.add(e.getDiskUsedSize());
//                }
//                metricValue.setValue(list);
//            });
//        } else {
//            metricsList.forEach(e -> {
//                list.add(e.getUpdateTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
//                if (e.getMemUsedSize() == null || memSize == null) {
//                    log.error("容器性能记录MemUsedSize或MemSize为null，记录id为{}", e.getId());
//                } else {
//                    list.add(e.getMemUsedSize().divide(memSize, 1, RoundingMode.HALF_UP));
//                }
//                metricValue.setValue(list);
//            });
//        }
        return metricValue;
    }
}




