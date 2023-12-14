package org.example.performance.pojo.po;

import com.baomidou.mybatisplus.annotation.*;
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

/**
 * 容器性能表
 *
 * @TableName container_metrics
 */
@TableName(value = "container_metrics")
@Data
@Slf4j
public class ContainerMetrics implements Serializable, HasUpdateTime {
    public enum Type {
        CPU,
        MEM,
        MEM_RATE,
        DISK
    }

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

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
    private String state;

    /**
     * 使用内存大小
     */
    private BigDecimal memUsedSize;

    /**
     * 使用磁盘大小
     */
    private BigDecimal diskUsedSize;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public BigDecimal getValue(Type field, BigDecimal memSize) {
        if (memSize == null || memSize.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException(CodeMsg.PARAMETER_ERROR);
        }
        BigDecimal result;
        if (Type.MEM.equals(field)) {
            result = this.memUsedSize;
        } else if (Type.CPU.equals(field)) {
            result = this.cpuRate;
        } else if (Type.DISK.equals(field)) {
            result = this.diskUsedSize;
        } else if (Type.MEM_RATE.equals(field)) {
            if (memUsedSize == null) {
                return null;
            }
            result = this.memUsedSize.divide(memSize, 1, RoundingMode.HALF_UP);
        } else {
            log.error("不支持的字段：{}", field);
            throw new BusinessException(CodeMsg.PARAMETER_ERROR);
        }
        return result;
    }
}