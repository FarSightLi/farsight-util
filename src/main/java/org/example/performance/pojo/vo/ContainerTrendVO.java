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
    /**
     * 某容器的数据
     */
    private List<MetricDataEntry> list;

    /**
     * 指标描述
     */
    private String metricDesc;

    /**
     * 指标名字如container.mem.rate
     */
    private String metricName;

    /**
     * 指标警告阈值
     */
    private BigDecimal triggerWarnLimit;

    /**
     * 指标严重报警阈值
     */
    private BigDecimal triggerErrorLimit;

    @Data
    public static class MetricDataEntry {
        /**
         * 容器名
         */
        private String target;

        /**
         * 具体数据（是一个列表，每个数据的第一位表示时间戳，第二位表示性能具体数据）
         */
        private MetricValue metrics;
    }

    @Data
    public static class MetricValue {
        /**
         * 第一位表示时间戳，第二位表示性能具体数据
         */
        private List<Object> value;
    }
}
