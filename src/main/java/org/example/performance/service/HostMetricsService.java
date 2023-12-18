package org.example.performance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.performance.pojo.bo.HostMetricsBO;
import org.example.performance.pojo.vo.HostMetricsVO;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author bonree
* @description 针对表【host_metrics(主机指标表)】的数据库操作Service
* @createDate 2023-12-06 14:14:04
*/
public interface HostMetricsService extends IService<HostMetricsBO> {
    void insertBatch(List<HostMetricsBO> hostMetricsBOList);

    List<HostMetricsVO> getMetricsVO(String ip, LocalDateTime startTime, LocalDateTime endTime);
}
