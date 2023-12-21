package org.example.performance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.performance.pojo.po.AlertRule;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;

/**
 * @author bonree
 * @description 针对表【alert_rule(预警规则表)】的数据库操作Service
 * @createDate 2023-12-19 17:26:26
 */
public interface AlertRuleService extends IService<AlertRule> {
    /**
     * 获得指标名字对应的预警规则Map
     *
     * @return
     */
    Map<String, AlertRule> getRuleMap();

    BigDecimal getRuleValue(AlertRule alertRule, Function<AlertRule, Integer> valueExtractor);

}
