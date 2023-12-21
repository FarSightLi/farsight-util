package org.example.performance.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 性能指标配置表
 *
 * @TableName metric_config
 */
@TableName(value = "metric_config")
@Data
public class MetricConfig implements Serializable {
    public enum Type {
        HOST(1),
        CONTAINER(2),
        SERVICE(3),
        CLUSTER(4);

        private final Integer value;

        Type(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }

        public String getValueStr() {
            return value.toString();
        }
    }

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 指标英文全称
     */
    private String metricName;

    /**
     * 指标所属环境 (1主机 2容器 3服务 4集群)
     */
    private Integer type;

    /**
     * 指标中文描述
     */
    private String metricDesc;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}