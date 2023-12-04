package org.example.po;

import lombok.Data;

import java.util.List;


@Data
public class SystemInfo {
    private String cpuArch;
    private Integer cpuCores;
    private DiskInfo diskInfo;
    private String ip;
    // 内核版本
    private String kernelRelease;
    private String memSize;
    private String sysVersion;



}
