package org.example.performance.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;
import org.example.performance.mapper.MetricConfigMapper;
import org.example.performance.pojo.po.MetricConfig;
import org.example.performance.service.MetricConfigService;
import org.springframework.data.redis.core.HashOperations;
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
    private RedisTemplate<String, String> redisTemplate;

    private static final String METRIC_CONFIG_KEY = "metric_conf:type_id";

    public Map<String, Integer> getMetricName2IdMapByType(MetricConfig.Type type) {
        HashOperations<String, String, Map<String, Integer>> opsedForHash = redisTemplate.opsForHash();
        Map<String, Integer> name2IdMap = opsedForHash.get(METRIC_CONFIG_KEY, type.getValueStr());
        if (ObjectUtil.isEmpty(name2IdMap)) {
            log.info("redis缓存中没有{}对应的MetricConfig，查询数据库", type);
            name2IdMap = getMetricConfigList(type).stream().collect(Collectors.toMap(MetricConfig::getMetricName, MetricConfig::getId));
            opsedForHash.put(METRIC_CONFIG_KEY, type.getValueStr(), name2IdMap);
            redisTemplate.expire(METRIC_CONFIG_KEY, 10L, TimeUnit.MINUTES);
            log.info("指标配置Type对应id的缓存已刷新");
        }
        return name2IdMap;
    }

    @Override
    public List<MetricConfig> getMetricConfigList(MetricConfig.Type type) {
        List<MetricConfig> list = lambdaQuery().eq(MetricConfig::getType, type.getValueStr()).list();
        if (ObjectUtil.isEmpty(list)) {
            log.info("{}没有对应的配置信息", type);
            throw new BusinessException(CodeMsg.SYSTEM_ERROR, type + "没有对应的配置信息");
        }
        return list;
    }
}




