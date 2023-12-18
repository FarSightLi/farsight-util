package org.example.performance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.performance.pojo.bo.ContainerMetricsBO;
import org.example.performance.pojo.vo.ContainerInfoVO;
import org.example.performance.pojo.vo.ContainerTrendVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author bonree
 * @description 针对表【container_metrics(容器性能表)】的数据库操作Service
 * @createDate 2023-12-06 14:14:04
 */
public interface ContainerMetricsService extends IService<ContainerMetricsBO> {
    void insertBatch(List<ContainerMetricsBO> containerMetricsBOList);

    List<ContainerInfoVO> getContainerMetricsByIp(String ip, LocalDateTime startTime, LocalDateTime endTime);

    List<ContainerTrendVO> getMetricTrend(String ip, LocalDateTime startTime, LocalDateTime endTime);
}
