package org.example.performance.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;
import org.example.performance.mapper.ContainerMetricsMapper;
import org.example.performance.pojo.po.ContainerInfo;
import org.example.performance.pojo.po.ContainerMetrics;
import org.example.performance.pojo.vo.ContainerInfoVO;
import org.example.performance.service.ContainerInfoService;
import org.example.performance.service.ContainerMetricsService;
import org.example.performance.service.HostInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
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

    @Override
    public void insertBatch(List<ContainerMetrics> containerMetricsList) {
        containerMetricsList.forEach(containerMetrics -> containerMetrics.setUpdateTime(LocalDateTime.now()));
        baseMapper.insertBatch(containerMetricsList);
    }

    @Override
    public List<ContainerInfoVO> getContainerMetricsByIp(String ip, LocalDateTime startTime, LocalDateTime endTime) {
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
        if (ObjectUtil.isEmpty(metricsList)) {
            throw new BusinessException(CodeMsg.PARAMETER_ERROR, "该时段没有相关的信息");
        }
        // 容器id对应的所有性能指标
        Map<String, List<ContainerMetrics>> containerMetricsMap = metricsList.stream().collect(Collectors.groupingBy(ContainerMetrics::getContainerId));
        List<ContainerInfoVO> voList = new ArrayList<>();
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
            getThreeMaxIndex(containerMetricsList, vo, containerInfo);
            // 计算三个指标的平均值
            getThreeAvgIndex(containerMetricsList, vo, containerInfo);
            // 计算在线时长
            vo.setOnlineTime(calculateDurationMillis(vo.getRestartTime()));
            voList.add(vo);
        });
        return voList;
    }

    private void getThreeMaxIndex(List<ContainerMetrics> containerMetricsList, ContainerInfoVO vo, ContainerInfo containerInfo) {
        BigDecimal maxCpuRate = containerMetricsList.stream()
                .filter(containerMetrics -> containerMetrics.getCpuRate() != null)
                .max(Comparator.comparing(ContainerMetrics::getCpuRate))
                .orElseThrow(() -> new BusinessException(CodeMsg.SYSTEM_ERROR, "容器性能指标数据有错")).getCpuRate();
        BigDecimal cpus = containerInfo.getCpus();
        // 代表没有限制,容器cpu即是主机cpu数
        if (cpus.compareTo(BigDecimal.ZERO) == 0) {
            // TODO 优化查询逻辑
            Integer hostCpuCores = hostInfoService.getById(containerInfo.getHostId()).getCpuCores();
            cpus = BigDecimal.valueOf(hostCpuCores);
        }
        BigDecimal maxCpuUsage = maxCpuRate.multiply(cpus);
        vo.setMaxCpuRate(maxCpuRate);
        vo.setMaxCpuUsage(maxCpuUsage);
        BigDecimal maxMemUsedSize = containerMetricsList.stream()
                .filter(e -> e.getMemUsedSize() != null)
                .max(Comparator.comparing(ContainerMetrics::getMemUsedSize))
                .orElseThrow(() -> new BusinessException(CodeMsg.SYSTEM_ERROR, "容器性能指标数据有错")).getMemUsedSize();
        BigDecimal maxMemUsedRate = maxMemUsedSize.divide(containerInfo.getMemSize(), 2, RoundingMode.HALF_UP);
        vo.setMaxMemUsage(maxMemUsedSize);
        vo.setMaxMemUsedRate(maxMemUsedRate);
        BigDecimal maxDiskUsedSize = containerMetricsList.stream()
                .filter(e -> e.getDiskUsedSize() != null)
                .max(Comparator.comparing(ContainerMetrics::getDiskUsedSize))
                .orElseThrow(() -> new BusinessException(CodeMsg.SYSTEM_ERROR, "容器性能指标数据有错")).getDiskUsedSize();
        BigDecimal maxDiskUsedRate = maxDiskUsedSize.divide(containerInfo.getDiskSize(), 2, RoundingMode.HALF_UP);
        vo.setMaxDiskUsage(maxDiskUsedSize);
        vo.setMaxDiskUsedRate(maxDiskUsedRate);
        // TODO 设置预警和报警指标
    }

    private void getThreeAvgIndex(List<ContainerMetrics> containerMetricsList, ContainerInfoVO vo, ContainerInfo containerInfo) {
        double avgCpuRate = containerMetricsList.stream()
                .map(ContainerMetrics::getCpuRate)
                .filter(Objects::nonNull).mapToDouble(BigDecimal::doubleValue).average().orElse(0);
        double avgDiskUsed = containerMetricsList.stream()
                .map(ContainerMetrics::getDiskUsedSize)
                .filter(Objects::nonNull).mapToDouble(BigDecimal::doubleValue).average().orElse(0);
        double avgMenUsed = containerMetricsList.stream()
                .map(ContainerMetrics::getMemUsedSize)
                .filter(Objects::nonNull).mapToDouble(BigDecimal::doubleValue).average().orElse(0);
        // TODO 设置预警和报警指标
        BigDecimal cpuUsage = containerInfo.getCpus().multiply(BigDecimal.valueOf(avgCpuRate));
        vo.setAvgCpuUsage(cpuUsage.setScale(2, RoundingMode.HALF_UP));
        vo.setAvgDiskUsage(BigDecimal.valueOf(avgDiskUsed).setScale(2, RoundingMode.HALF_UP));
        vo.setAvgMemUsage(BigDecimal.valueOf(avgMenUsed).setScale(2, RoundingMode.HALF_UP));
    }

    private long calculateDurationMillis(LocalDateTime target) {
        Instant providedInstant = target.atZone(ZoneId.systemDefault()).toInstant();
        Instant currentInstant = Instant.now();
        return currentInstant.toEpochMilli() - providedInstant.toEpochMilli();
    }
}




