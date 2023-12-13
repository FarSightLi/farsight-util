package org.example.performance.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 容器性能图表VO
 * @date 2023/12/12 16:48:30
 */
@Data
public class ContainerTrendVO {
    public enum Type {
        CPU,
        MEM,
        MEM_RATE,
        DISK
    }

    private List<MetricDataEntry> list;
    private String metricDesc;
    private String metricName;
    private BigDecimal triggerWarnLimit;
    private BigDecimal triggerErrorLimit;

    @Data
    public static class MetricDataEntry {
        private String target;
        private MetricValue metrics;
    }

    @Data
    public static class MetricValue {
        private List<Object> value;
    }
}
