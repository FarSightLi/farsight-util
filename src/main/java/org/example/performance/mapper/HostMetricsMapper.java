package org.example.performance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.performance.pojo.po.HostMetrics;

/**
* @author bonree
* @description 针对表【host_metrics(主机指标表)】的数据库操作Mapper
* @createDate 2023-12-06 14:14:04
* @Entity org.example.performance.po.HostMetrics
*/
@Mapper
public interface HostMetricsMapper extends BaseMapper<HostMetrics> {

}




