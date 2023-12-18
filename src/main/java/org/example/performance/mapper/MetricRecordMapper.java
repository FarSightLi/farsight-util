package org.example.performance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.performance.pojo.po.MetricRecord;

import java.util.Collection;

/**
 * @author bonree
 * @description 针对表【metric_record】的数据库操作Mapper
 * @createDate 2023-12-18 17:27:25
 * @Entity org.example.performance.pojo.po.MetricRecord
 */
@Mapper
public interface MetricRecordMapper extends BaseMapper<MetricRecord> {
    int insertBatch(@Param("metricRecordCollection") Collection<MetricRecord> metricRecordCollection);
}




