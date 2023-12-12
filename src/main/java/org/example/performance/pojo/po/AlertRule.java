package org.example.performance.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 预警规则表
 *
 * @TableName alert_rule
 */
@TableName(value = "alert_rule")
@Data
public class AlertRule implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 预警项
     */
    private String metricName;

    /**
     *
     */
    private String metricDesc;

    /**
     * 严重阈值
     */
    private BigDecimal errorValue;

    /**
     * 警告阈值
     */
    private BigDecimal warningValue;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}