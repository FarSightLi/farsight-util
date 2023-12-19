package org.example.performance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.performance.pojo.po.AlertRule;

/**
 * @author bonree
 * @description 针对表【alert_rule(预警规则表)】的数据库操作Mapper
 * @createDate 2023-12-19 17:26:26
 * @Entity org.example.performance.pojo.po.AlertRule
 */
@Mapper
public interface AlertRuleMapper extends BaseMapper<AlertRule> {

}




