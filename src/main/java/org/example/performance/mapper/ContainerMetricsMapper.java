package org.example.performance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.performance.pojo.po.ContainerMetrics;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * @author bonree
 * @description 针对表【container_metrics(容器性能表)】的数据库操作Mapper
 * @createDate 2023-12-06 14:14:04
 * @Entity org.example.performance.po.ContainerMetrics
 */
@Mapper
public interface ContainerMetricsMapper extends BaseMapper<ContainerMetrics> {
    int insertBatch(@Param("containerMetricsCollection") Collection<ContainerMetrics> containerMetricsCollection);


    List<ContainerMetrics> getByContainerIdList(@Param("containerIds") List<String> containerIdList,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);
}




