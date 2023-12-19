package org.example.performance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.performance.pojo.po.AlertRule;

import java.util.Map;

/**
 * @author bonree
 * @description 针对表【alert_rule(预警规则表)】的数据库操作Service
 * @createDate 2023-12-19 17:26:26
 */
public interface AlertRuleService extends IService<AlertRule> {
    Map<String, AlertRule> getRuleMap();

}
