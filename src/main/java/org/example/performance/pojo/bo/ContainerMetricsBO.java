package org.example.performance.pojo.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.HasUpdateTime;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 容器性能BO
 */
@Data
@Slf4j
public class ContainerMetricsBO implements Serializable, HasUpdateTime {
    private static final HashMap<String, Integer> STATUS_MAP = new HashMap<>();

    static {
        STATUS_MAP.put("running", 1);
        STATUS_MAP.put("created", 2);
        STATUS_MAP.put("restarting", 3);
        STATUS_MAP.put("removing", 4);
        STATUS_MAP.put("paused", 5);
        STATUS_MAP.put("exited", 6);
        STATUS_MAP.put("dead", 7);
    }

    public static Map<String, Integer> getStatusMap() {
        return STATUS_MAP;
    }


    @Getter
    public enum MetricType {
        CPU("container.cpu.rate", "cpu", "容器CPU使用率(相对于limit，%)"),
        MEM("container.mem.sum", "mem", "容器内存使用量(MB)"),
        MEM_RATE("container.mem.rate", "mem_rate", "容器内存使用率(相对于limit，%)"),
        DISK("container.disk.sum", "disk", "容器磁盘使用量(MB)");

        private final String ruleKey;
        private final String name;
        private final String desc;

        MetricType(String ruleKey, String name, String desc) {
            this.ruleKey = ruleKey;
            this.name = name;
            this.desc = desc;
        }
    }

    /**
     * 容器唯一ID
     */
    private Long code;

    /**
     * 容器ID
     */
    private String containerId;

    /**
     * CPU使用率
     */
    private BigDecimal cpuRate;

    /**
     * 重启时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime restartTime;

    /**
     * 容器状态
     */
    private Integer state;

    /**
     * 使用内存大小
     */
    private BigDecimal memUsedSize;

    /**
     * 使用磁盘大小
     */
    private BigDecimal diskUsedSize;

    /**
     * 使用内存大小
     */
    private BigDecimal memSize;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;

    /**
     * 通过枚举字段类型获得对应的值
     *
     * @param field
     * @param memSize
     * @return
     */
    public BigDecimal getValue(MetricType field, BigDecimal memSize) {
        BigDecimal result;
        if (MetricType.MEM.equals(field)) {
            result = this.memUsedSize;
        } else if (MetricType.CPU.equals(field)) {
            result = this.cpuRate;
        } else if (MetricType.DISK.equals(field)) {
            result = this.diskUsedSize;
        } else if (MetricType.MEM_RATE.equals(field)) {
            if (memUsedSize == null || memSize == null || memSize.compareTo(BigDecimal.ZERO) == 0) {
                return null;
            }
            result = this.memUsedSize.divide(memSize, 1, RoundingMode.HALF_UP);
        } else {
            log.error("不支持的字段：{}", field);
            throw new BusinessException(CodeMsg.PARAMETER_ERROR);
        }
        return result;
    }

    /**
     * 通过字符串字段类型获得对应的值
     * <p>
     * 如果是不支持的字段就返回null
     *
     * @param field
     * @return 对应的value
     */
    public BigDecimal getValueByStr(String field) {
        switch (field) {
            case "container.mem.sum":
                return this.memUsedSize;
            case "container.cpu.rate":
                return this.cpuRate;
            case "container.disk.sum":
                return this.diskUsedSize;
            default:
                log.warn("不支持的字段：{}", field);
                return null;
        }

    }

    /**
     * 通过字符串字段类型设置对应的值
     *
     * @param field
     */
    public void setValue(String field, BigDecimal value) {
        switch (field) {
            case "container.mem.sum":
                this.memUsedSize = value;
                break;
            case "container.cpu.rate":
                this.cpuRate = value;
                break;
            case "container.disk.sum":
                this.diskUsedSize = value;
                break;
            default:
                log.warn("不支持的字段：{}", field);
        }

    }
}