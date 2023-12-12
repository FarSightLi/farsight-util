package org.example.performance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.performance.pojo.po.HostMetrics;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * @author bonree
 * @description 针对表【host_metrics(主机指标表)】的数据库操作Mapper
 * @createDate 2023-12-06 14:14:04
 * @Entity org.example.performance.po.HostMetrics
 */
@Mapper
public interface HostMetricsMapper extends BaseMapper<HostMetrics> {
    int insertBatch(@Param("hostMetricsCollection") Collection<HostMetrics> hostMetricsCollection);

    List<HostMetrics> selectByHostId(@Param("hostId") Integer hostId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);
}




