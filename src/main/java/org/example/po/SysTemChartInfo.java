package org.example.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SysTemChartInfo {
    private String desc;
    private String name;
    private int state;
    private String type;
    private double value;
    private Double maxValue;
    private double triggerWarnLimit;
    private double triggerErrorLimit;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate monitorTime;

}
