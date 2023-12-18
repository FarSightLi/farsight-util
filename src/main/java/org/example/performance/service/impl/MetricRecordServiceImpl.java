package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.performance.mapper.MetricRecordMapper;
import org.example.performance.pojo.po.MetricRecord;
import org.example.performance.service.MetricRecordService;
import org.springframework.stereotype.Service;

/**
 * @author bonree
 * @description 针对表【metric_record】的数据库操作Service实现
 * @createDate 2023-12-18 17:27:25
 */
@Service
public class MetricRecordServiceImpl extends ServiceImpl<MetricRecordMapper, MetricRecord>
        implements MetricRecordService {

}




