package org.example.performance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.performance.pojo.bo.HostMetricsBO;
import org.example.performance.pojo.po.MetricRecord;

import java.util.List;

/**
 * @author bonree
 * @description 针对表【metric_record】的数据库操作Service
 * @createDate 2023-12-18 17:27:25
 */
public interface MetricRecordService extends IService<MetricRecord> {
    void insertHostBatch(List<HostMetricsBO> hostMetricsBOList);

}