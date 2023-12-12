package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.performance.mapper.AlertRuleMapper;
import org.example.performance.pojo.po.AlertRule;
import org.example.performance.service.AlertRuleService;
import org.springframework.stereotype.Service;

/**
 * @author bonree
 * @description 针对表【alert_rule(预警规则表)】的数据库操作Service实现
 * @createDate 2023-12-12 10:57:49
 */
@Service
public class AlertRuleServiceImpl extends ServiceImpl<AlertRuleMapper, AlertRule>
        implements AlertRuleService {

}




