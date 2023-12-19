package org.example.performance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.performance.pojo.bo.ContainerMetricsBO;
import org.example.performance.pojo.bo.HostMetricsBO;
import org.example.performance.pojo.po.MetricRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author bonree
 * @description 针对表【metric_record】的数据库操作Service
 * @createDate 2023-12-18 17:27:25
 */
public interface MetricRecordService extends IService<MetricRecord> {
    /**
     * 批量插入主机指标
     *
     * @param hostMetricsBOList
     */
    void insertHostBatch(List<HostMetricsBO> hostMetricsBOList);

    /**
     * 批量插入容器指标
     * @param containerMetricsBOList
     */
    void insertContainerBatch(List<ContainerMetricsBO> containerMetricsBOList);

    List<HostMetricsBO> getHostMetricBOList(Long id, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 传入容器唯一id和起止时间获得记录
     *
     * @param idList
     * @param startTime
     * @param endTime
     * @return
     */
    List<ContainerMetricsBO> getContainerMetricBOList(List<Long> idList, LocalDateTime startTime, LocalDateTime endTime);
}
