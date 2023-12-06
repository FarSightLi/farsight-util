package org.example.performance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.performance.pojo.po.ContainerMetrics;

/**
* @author bonree
* @description 针对表【container_metrics(容器性能表)】的数据库操作Mapper
* @createDate 2023-12-06 14:14:04
* @Entity org.example.performance.po.ContainerMetrics
*/
@Mapper
public interface ContainerMetricsMapper extends BaseMapper<ContainerMetrics> {

}




