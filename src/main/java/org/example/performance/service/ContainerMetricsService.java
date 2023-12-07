package org.example.performance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.performance.pojo.po.ContainerMetrics;

import java.util.List;

/**
* @author bonree
* @description 针对表【container_metrics(容器性能表)】的数据库操作Service
* @createDate 2023-12-06 14:14:04
*/
public interface ContainerMetricsService extends IService<ContainerMetrics> {
    void insertBatch(List<ContainerMetrics> containerMetricsList);

}
