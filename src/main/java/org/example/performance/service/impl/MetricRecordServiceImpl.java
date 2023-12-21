package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.performance.mapper.MetricRecordMapper;
import org.example.performance.pojo.bo.ContainerMetricsBO;
import org.example.performance.pojo.bo.HostMetricsBO;
import org.example.performance.pojo.po.MetricConfig;
import org.example.performance.pojo.po.MetricRecord;
import org.example.performance.service.ContainerInfoService;
import org.example.performance.service.HostInfoService;
import org.example.performance.service.MetricConfigService;
import org.example.performance.service.MetricRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
    @Resource
    private ContainerInfoService containerInfoService;

    @Override
    public void insertHostBatch(List<HostMetricsBO> hostMetricsBOList) {
        List<String> ipList = hostMetricsBOList.stream().map(HostMetricsBO::getHostIp).collect(Collectors.toList());
        // name 和 id 的map
        Map<String, Integer> name2IdMapByType = metricConfigService.getMetricName2IdMapByType(MetricConfig.OriginType.HOST);
        // ip 和 主机id 的map
        Map<String, Long> ip2IdMap = hostInfoService.getIp2IdMap(ipList);
        // 每个bo会创建出10个Record
        List<MetricRecord> metricRecordList = new ArrayList<>(hostMetricsBOList.size() * 10);
        LocalDateTime now = LocalDateTime.now();

        hostMetricsBOList.forEach(bo -> name2IdMapByType.forEach((name, id) -> {
            MetricRecord metricRecord = new MetricRecord();
            metricRecord.setMetricId(id);
            metricRecord.setMetricOrigin(ip2IdMap.get(bo.getHostIp()));
            metricRecord.setMonitorTime(now);
            BigDecimal value = bo.getValueByType(name);
            if (value != null) {
                metricRecord.setMetricValue(value);
                metricRecordList.add(metricRecord);
            }
        }));

        baseMapper.insertBatch(metricRecordList);
    }

    @Override
    public void insertContainerBatch(List<ContainerMetricsBO> containerMetricsBOList) {
        // type 和 id 的map
        Map<String, Integer> metricType2IdMap = metricConfigService.getMetricName2IdMapByType(MetricConfig.OriginType.CONTAINER);
        // 每个bo会创建出5个Record
        List<MetricRecord> metricRecordList = new ArrayList<>(containerMetricsBOList.size() * 5);
        LocalDateTime now = LocalDateTime.now();
        containerMetricsBOList.forEach(bo -> metricType2IdMap.forEach((type, id) -> {
            MetricRecord metricRecord = new MetricRecord();
            metricRecord.setMetricId(id);
            metricRecord.setMetricOrigin(containerInfoService.getCodeByContainerId(bo.getContainerId()));
            if ("state".equals(type)) {
                metricRecord.setMetricValue(BigDecimal.valueOf(bo.getState()));
            } else {
                // 其他小数数据可统一获得
                metricRecord.setMetricValue(bo.getValueByStr(type));
            }
            metricRecord.setMonitorTime(now);
            // 无法获得的数据就不进行保存（即在配置表中的字段无法对应BO的字段）
            // 意味着这些字段不必入库
            if (metricRecord.getMetricValue() != null) {
                metricRecordList.add(metricRecord);
            }
        }));
        baseMapper.insertBatch(metricRecordList);

    }

    @Override
    public List<HostMetricsBO> getHostMetricBOList(Long id, LocalDateTime startTime, LocalDateTime endTime) {
        // 时间 对应 记录的map
        Map<LocalDateTime, List<MetricRecord>> time2RecordMap = lambdaQuery()
                .eq(MetricRecord::getMetricOrigin, id)
                .between(MetricRecord::getMonitorTime, startTime, endTime)
                .select(MetricRecord::getMetricId, MetricRecord::getMetricOrigin, MetricRecord::getMetricValue, MetricRecord::getMonitorTime)
                .list()
                .stream()
                .collect(Collectors.groupingBy(MetricRecord::getMonitorTime));
        // 指标id 对应 name 的map
        Map<Integer, String> metricId2TypeMap = metricConfigService.getMetricConfigList(MetricConfig.OriginType.HOST)
                .stream().collect(Collectors.toMap(MetricConfig::getId, MetricConfig::getMetricName));
        List<HostMetricsBO> boList = new ArrayList<>(time2RecordMap.size());
        time2RecordMap.forEach((time, recordList) -> {
            HostMetricsBO bo = new HostMetricsBO();
            bo.setUpdateTime(time);
            // 设置各个值
            recordList.forEach(metricRecord -> bo.setValue(metricId2TypeMap.get(metricRecord.getMetricId()), metricRecord.getMetricValue()));
            boList.add(bo);
        });
        return boList;
    }

    @Override
    public List<ContainerMetricsBO> getContainerMetricBOList(List<Long> idList, LocalDateTime startTime, LocalDateTime endTime) {
        // 每个容器code对应的记录Map
        Map<Long, List<MetricRecord>> id2RecordMap = lambdaQuery()
                .in(MetricRecord::getMetricOrigin, idList)
                .between(MetricRecord::getMonitorTime, startTime, endTime)
                .select(MetricRecord::getMetricId, MetricRecord::getMetricOrigin, MetricRecord::getMetricValue, MetricRecord::getMonitorTime)
                .list()
                .stream().collect(Collectors.groupingBy(MetricRecord::getMetricOrigin));
        // 每个容器 对应 按时间分组的记录 Map
        Map<Long, Map<LocalDateTime, List<MetricRecord>>> id2TimeRecordMap = id2RecordMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> entry.getValue().stream().collect(Collectors.groupingBy(MetricRecord::getMonitorTime))));
        // 指标id 对应 name 的map
        Map<Integer, String> metricId2TypeMap = metricConfigService.getMetricConfigList(MetricConfig.OriginType.CONTAINER)
                .stream().collect(Collectors.toMap(MetricConfig::getId, MetricConfig::getMetricName));
        List<ContainerMetricsBO> boList = new ArrayList<>(id2TimeRecordMap.size());
        id2TimeRecordMap.forEach((code, map) -> map.forEach((time, recordList) -> {
            ContainerMetricsBO bo = new ContainerMetricsBO();
            bo.setCode(code);
            bo.setUpdateTime(time);
            recordList.forEach(r -> {
                String name = metricId2TypeMap.get(r.getMetricId());
                if ("container.state".equals(name)) {
                    bo.setState(r.getMetricValue().intValue());
                } else {
                    bo.setValue(name, r.getMetricValue());
                }
            });
            boList.add(bo);
        }));

        return boList;
    }
}




