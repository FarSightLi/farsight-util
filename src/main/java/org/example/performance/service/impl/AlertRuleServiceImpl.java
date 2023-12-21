package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.performance.mapper.AlertRuleMapper;
import org.example.performance.pojo.po.AlertRule;
import org.example.performance.pojo.po.MetricConfig;
import org.example.performance.service.AlertRuleService;
import org.example.performance.service.MetricConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author bonree
 * @description 针对表【alert_rule(预警规则表)】的数据库操作Service实现
 * @createDate 2023-12-19 17:26:26
 */
@Service
public class AlertRuleServiceImpl extends ServiceImpl<AlertRuleMapper, AlertRule>
        implements AlertRuleService {
    @Resource
    private MetricConfigService metricConfigService;

    public Map<String, AlertRule> getRuleMap() {
        // 指标id对应的rule 的map
        Map<Integer, AlertRule> id2ruleMap = list().stream().collect(Collectors.toMap(AlertRule::getMetricId, Function.identity()));
        Map<Integer, String> id2NameMap = metricConfigService.lambdaQuery().in(MetricConfig::getId, id2ruleMap.keySet()).list()
                .stream().collect(Collectors.toMap(MetricConfig::getId, MetricConfig::getMetricName));
        Map<String, AlertRule> ruleMap = new HashMap<>();
        id2ruleMap.forEach((id, alert) -> ruleMap.put(id2NameMap.get(id), alert));
        return ruleMap;
    }

    public BigDecimal getRuleValue(AlertRule alertRule, Function<AlertRule, Integer> valueExtractor) {
        return Optional.ofNullable(alertRule)
                .map(valueExtractor)
                .map(BigDecimal::valueOf)
                .orElse(null);
    }

}




