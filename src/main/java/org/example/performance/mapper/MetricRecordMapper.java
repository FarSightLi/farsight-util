package org.example.performance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.performance.pojo.po.MetricRecord;

/**
 * @author bonree
 * @description 针对表【metric_record】的数据库操作Mapper
 * @createDate 2023-12-18 17:27:25
 * @Entity org.example.performance.pojo.po.MetricRecord
 */
@Mapper
public interface MetricRecordMapper extends BaseMapper<MetricRecord> {

}




