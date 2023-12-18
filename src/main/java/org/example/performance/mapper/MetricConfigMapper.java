package org.example.performance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.performance.pojo.po.MetricConfig;

/**
 * @author bonree
 * @description 针对表【metric_config(性能指标配置表)】的数据库操作Mapper
 * @createDate 2023-12-18 17:27:25
 * @Entity org.example.performance.pojo.po.MetricConfig
 */
@Mapper
public interface MetricConfigMapper extends BaseMapper<MetricConfig> {

}




