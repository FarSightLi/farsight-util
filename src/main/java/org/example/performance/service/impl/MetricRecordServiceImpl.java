package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.performance.mapper.MetricRecordMapper;
import org.example.performance.pojo.bo.HostMetricsBO;
import org.example.performance.pojo.po.MetricConfig;
import org.example.performance.pojo.po.MetricRecord;
import org.example.performance.service.HostInfoService;
import org.example.performance.service.MetricConfigService;
import org.example.performance.service.MetricRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author bonree
 * @description 针对表【metric_record】的数据库操作Service实现
 * @createDate 2023-12-18 17:27:25
 */
@Service
public class MetricRecordServiceImpl extends ServiceImpl<MetricRecordMapper, MetricRecord>
        implements MetricRecordService {
    @Resource
    private MetricConfigService metricConfigService;
    @Resource
    private HostInfoService hostInfoService;

    @Override
    public void insertHostBatch(List<HostMetricsBO> hostMetricsBOList) {
        List<String> ipList = hostMetricsBOList.stream().map(HostMetricsBO::getHostIp).collect(Collectors.toList());
        // type 和 id 的map
        Map<String, Integer> metricType2IdMap = metricConfigService.getMetricType2IdMapByType(MetricConfig.Origin.HOST);
        // ip 和 主机id 的map
        Map<String, Long> ip2IdMap = hostInfoService.getIp2IdMap(ipList);
        // 每个bo会创建出10个Record
        List<MetricRecord> metricRecordList = new ArrayList<>(hostMetricsBOList.size() * 10);
        LocalDateTime now = LocalDateTime.now();

        hostMetricsBOList.forEach(e -> metricType2IdMap.forEach((type, id) -> {
            MetricRecord metricRecord = new MetricRecord();
            metricRecord.setMetricId(id);
            metricRecord.setMetricOrigin(ip2IdMap.get(e.getHostIp()));
            metricRecord.setMetricValue(e.getInfoByType(type).toString());
            metricRecord.setUpdateTime(now);
            metricRecordList.add(metricRecord);
        }));

        baseMapper.insertBatch(metricRecordList);
    }
}




