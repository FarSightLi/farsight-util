package org.example.performance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.performance.pojo.po.MetricConfig;

import java.util.List;
import java.util.Map;

/**
 * @author bonree
 * @description 针对表【metric_config(性能指标配置表)】的数据库操作Service
 * @createDate 2023-12-18 17:27:25
 */
public interface MetricConfigService extends IService<MetricConfig> {
    /**
     * 获得某种类型的配置名对应的配置id
     *
     * @param originType
     * @return
     */
    Map<String, Integer> getMetricName2IdMapByType(MetricConfig.OriginType originType);

    List<MetricConfig> getMetricConfigList(MetricConfig.OriginType originType);
}
