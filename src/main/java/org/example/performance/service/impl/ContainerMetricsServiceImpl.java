package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.performance.mapper.ContainerMetricsMapper;
import org.example.performance.pojo.po.ContainerMetrics;
import org.example.performance.service.ContainerMetricsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author bonree
* @description 针对表【container_metrics(容器性能表)】的数据库操作Service实现
* @createDate 2023-12-06 14:14:04
*/
@Service
public class ContainerMetricsServiceImpl extends ServiceImpl<ContainerMetricsMapper, ContainerMetrics>
    implements ContainerMetricsService{

    @Override
    public void insertBatch(List<ContainerMetrics> containerMetricsList) {
        containerMetricsList.forEach(containerMetrics -> containerMetrics.setUpdateTime(LocalDateTime.now()));
        baseMapper.insertBatch(containerMetricsList);
    }
}




