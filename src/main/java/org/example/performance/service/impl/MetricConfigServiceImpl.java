package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.performance.mapper.MetricConfigMapper;
import org.example.performance.pojo.po.MetricConfig;
import org.example.performance.service.MetricConfigService;
import org.springframework.stereotype.Service;

/**
 * @author bonree
 * @description 针对表【metric_config(性能指标配置表)】的数据库操作Service实现
 * @createDate 2023-12-18 17:27:25
 */
@Service
public class MetricConfigServiceImpl extends ServiceImpl<MetricConfigMapper, MetricConfig>
        implements MetricConfigService {

}




