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
    private String desc;

    private String name;

    private Integer state;

    private String type;

    private BigDecimal value;

    private BigDecimal maxValue;

    private BigDecimal triggerWarnLimit;

    private BigDecimal triggerErrorLimit;


    private LocalDateTime monitorTime;

}
