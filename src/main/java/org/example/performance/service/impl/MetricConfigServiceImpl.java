package org.example.performance.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;
import org.example.performance.mapper.MetricConfigMapper;
import org.example.performance.pojo.po.MetricConfig;
import org.example.performance.service.MetricConfigService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author bonree
 * @description 针对表【metric_config(性能指标配置表)】的数据库操作Service实现
 * @createDate 2023-12-18 17:27:25
 */
@Service
@Slf4j
public class MetricConfigServiceImpl extends ServiceImpl<MetricConfigMapper, MetricConfig>
        implements MetricConfigService {
    @Resource
    private RedisTemplate<String, Map<String, Integer>> redisTemplate;

    private final String MetricConfigKey = "metric:name_id";

    public Map<String, Integer> getConfigMapByType(MetricConfig.Type type) {
        Map<String, Integer> name2IdMap = redisTemplate.opsForValue().get(MetricConfigKey);
        if (ObjectUtil.isEmpty(name2IdMap)) {
            name2IdMap = getDBConfigMapByType(type);
            log.info("redis缓存中没有MetricConfig，查询数据库");
        } else {
            log.info("MetricConfig使用了redis缓存");
        }
        return name2IdMap;
    }

    private Map<String, Integer> getDBConfigMapByType(MetricConfig.Type type) {
        List<MetricConfig> list = lambdaQuery().eq(MetricConfig::getType, type.getValue()).list();
        if (ObjectUtil.isEmpty(list)) {
            log.info("{}没有对应的配置信息", type);
            throw new BusinessException(CodeMsg.SYSTEM_ERROR, type + "没有对应的配置信息");
        } else {
            Map<String, Integer> name2IdMap = list.stream().collect(Collectors.toMap(MetricConfig::getMetricName, MetricConfig::getId));
            redisTemplate.opsForValue().set(MetricConfigKey, name2IdMap, 1L, TimeUnit.HOURS);
            log.info("MetricConfig的redis缓存已刷新");
            return name2IdMap;
        }
    }
}




