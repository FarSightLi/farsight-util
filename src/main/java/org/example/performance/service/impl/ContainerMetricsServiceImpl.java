package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.performance.pojo.po.ContainerMetrics;
import org.example.performance.service.ContainerMetricsService;
import org.example.performance.mapper.ContainerMetricsMapper;
import org.springframework.stereotype.Service;

/**
* @author bonree
* @description 针对表【container_metrics(容器性能表)】的数据库操作Service实现
* @createDate 2023-12-06 14:14:04
*/
@Service
public class ContainerMetricsServiceImpl extends ServiceImpl<ContainerMetricsMapper, ContainerMetrics>
    implements ContainerMetricsService{

}




