package org.example.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


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
