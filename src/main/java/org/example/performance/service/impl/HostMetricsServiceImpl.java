package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.performance.mapper.HostMetricsMapper;
import org.example.performance.pojo.po.HostMetrics;
import org.example.performance.service.HostInfoService;
import org.example.performance.service.HostMetricsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author bonree
* @description 针对表【host_metrics(主机指标表)】的数据库操作Service实现
* @createDate 2023-12-06 14:14:04
*/
@Service
public class HostMetricsServiceImpl extends ServiceImpl<HostMetricsMapper, HostMetrics>
    implements HostMetricsService{
    @Resource
    private HostInfoService hostInfoService;

    @Override
    public void insertBatch(List<HostMetrics> hostMetricsList) {
        Map<String, Integer> ip2IdMap = hostInfoService.getIp2IdMap(hostMetricsList.stream().map(HostMetrics::getHostIp).collect(Collectors.toList()));
        hostMetricsList.forEach(hostMetrics -> {
            hostMetrics.setHostId(ip2IdMap.get(hostMetrics.getHostIp()));
            hostMetrics.setUpdateTime(LocalDateTime.now());
        });
        baseMapper.insertBatch(hostMetricsList);
    }
}




