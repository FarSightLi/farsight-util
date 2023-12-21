package org.example.performance.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 预警规则表
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
     * 严重阈值
     */
    private Integer errorValue;

    /**
     * 警告阈值
     */
    private Integer warningValue;

    /**
     * 对应配置表中的指标id
     */
    private Integer metricId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}