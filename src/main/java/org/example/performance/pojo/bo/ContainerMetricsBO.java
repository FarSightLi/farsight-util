package org.example.performance.pojo.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.HasUpdateTime;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;

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

    public static HashMap<String, Integer> getStatusMap() {
        return STATUS_MAP;
    }


    public enum Type {
        CPU,
        MEM,
        MEM_RATE,
        DISK
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
    public BigDecimal getValue(Type field, BigDecimal memSize) {
        BigDecimal result;
        if (Type.MEM.equals(field)) {
            result = this.memUsedSize;
        } else if (Type.CPU.equals(field)) {
            result = this.cpuRate;
        } else if (Type.DISK.equals(field)) {
            result = this.diskUsedSize;
        } else if (Type.MEM_RATE.equals(field)) {
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
     *
     * @param field
     * @return
     */
    public BigDecimal getValueByStr(String field) {
        switch (field) {
            case "mem":
                return this.memUsedSize;
            case "cpu":
                return this.cpuRate;
            case "disk":
                return this.diskUsedSize;
            default:
                log.error("不支持的字段：{}", field);
                throw new BusinessException(CodeMsg.PARAMETER_ERROR);
        }

    }

    /**
     * 通过字符串字段类型设置对应的值
     *
     * @param field
     * @return
     */
    public void setValue(String field, BigDecimal value) {
        switch (field) {
            case "mem":
                this.memUsedSize = value;
                break;
            case "cpu":
                this.cpuRate = value;
                break;
            case "disk":
                this.diskUsedSize = value;
                break;
            default:
                log.error("不支持的字段：{}", field);
                throw new BusinessException(CodeMsg.PARAMETER_ERROR);
        }

    }
}