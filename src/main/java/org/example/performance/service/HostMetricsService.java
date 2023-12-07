package org.example.performance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.performance.pojo.po.HostMetrics;

import java.util.List;

/**
* @author bonree
* @description 针对表【host_metrics(主机指标表)】的数据库操作Service
* @createDate 2023-12-06 14:14:04
*/
public interface HostMetricsService extends IService<HostMetrics> {
    void insertBatch(List<HostMetrics> hostMetricsList);
}
