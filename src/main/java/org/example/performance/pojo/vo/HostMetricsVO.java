package org.example.performance.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 主机性能指标VO
 * @date 2023/12/12 09:58:06
 */
@Data
public class HostMetricsVO {
    /**
     * 指标描述
     */
    private String desc;

    /**
     * 指标名字如host.mem.sum
     */
    private String name;

    /**
     * 指标状态 1正常 2警告 3严重报警
     */
    private Integer state;

    /**
     * 指标type如mem
     */
    private String type;

    /**
     * 指标值
     */
    private BigDecimal value;

    /**
     * 最大值（目前尚未用到）
     */
    private BigDecimal maxValue;

    /**
     * 警告阈值
     */
    private BigDecimal triggerWarnLimit;

    /**
     * 严重报警阈值
     */
    private BigDecimal triggerErrorLimit;


    /**
     * 检测时间
     */
    private LocalDateTime monitorTime;

}
