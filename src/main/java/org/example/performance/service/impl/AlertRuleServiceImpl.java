package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.performance.mapper.AlertRuleMapper;
import org.example.performance.pojo.po.AlertRule;
import org.example.performance.pojo.po.MetricConfig;
import org.example.performance.service.AlertRuleService;
import org.example.performance.service.MetricConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
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
        Map<Integer, AlertRule> id2ruleMap = list().stream().collect(Collectors.toMap(AlertRule::getId, Function.identity()));
        Map<Integer, String> id2TypeMap = metricConfigService.getMetricConfigList(MetricConfig.Origin.CONTAINER)
                .stream().collect(Collectors.toMap(MetricConfig::getId, MetricConfig::getType));
        Map<String, AlertRule> ruleMap = new HashMap<>();
        id2ruleMap.forEach((id, alert) -> ruleMap.put(id2TypeMap.get(id), alert));
        return ruleMap;
    }
}




