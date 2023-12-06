package org.example.performance.po;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class SystemInfo {
    private String cpuArch;
    private Integer cpuCores;
    private DiskInfo diskInfo;
    private String ip;
    // 内核版本
    private String kernelRelease;
    private BigDecimal memSize;
    private String sysVersion;


}
