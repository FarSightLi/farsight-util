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

    private static final String METRIC_CONFIG_KEY = "metric:type_id";

    public Map<String, Integer> getMetricType2IdMapByType(MetricConfig.Origin origin) {
        Map<String, Integer> name2IdMap = redisTemplate.opsForValue().get(METRIC_CONFIG_KEY);
        if (ObjectUtil.isEmpty(name2IdMap)) {
            log.info("redis缓存中没有MetricConfig，查询数据库");
            name2IdMap = getMetricConfigList(origin).stream().collect(Collectors.toMap(MetricConfig::getType, MetricConfig::getId));
        } else {
            log.info("MetricConfig使用了redis缓存");
        }
        return name2IdMap;
    }

    @Override
    public List<MetricConfig> getMetricConfigList(MetricConfig.Origin origin) {
        List<MetricConfig> list = lambdaQuery().eq(MetricConfig::getOrigin, origin.getValue()).list();
        if (ObjectUtil.isEmpty(list)) {
            log.info("{}没有对应的配置信息", origin);
            throw new BusinessException(CodeMsg.SYSTEM_ERROR, origin + "没有对应的配置信息");
        }
        return list;
    }
}




