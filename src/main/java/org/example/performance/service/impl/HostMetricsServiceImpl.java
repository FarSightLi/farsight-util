package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.performance.pojo.po.HostMetrics;
import org.example.performance.service.HostMetricsService;
import org.example.performance.mapper.HostMetricsMapper;
import org.springframework.stereotype.Service;

/**
* @author bonree
* @description 针对表【host_metrics(主机指标表)】的数据库操作Service实现
* @createDate 2023-12-06 14:14:04
*/
@Service
public class HostMetricsServiceImpl extends ServiceImpl<HostMetricsMapper, HostMetrics>
    implements HostMetricsService{

}




