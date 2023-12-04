package org.example.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContainerInfo {
    private String containerName;
    private double imageSize;
    private double cpuRate;
    private double avgCpuUsage;
    private double maxCpuUsage;
    private int avgCpuState;
    private double maxCpuRate;
    private int maxCpuState;
    private double cpus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private double memSize;
    private double avgMemUsage;
    private double maxMemUsage;
    private double memUsedRate;
    private int avgMemUsedState;
    private double maxMemUsedRate;
    private int maxMemUsedState;
    private double diskSize;
    private double avgDiskUsage;
    private double maxDiskUsage;
    private double diskUsedRate;
    private int avgDiskUsedState;
    private double maxDiskUsedRate;
    private int maxDiskUsedState;
    private long onlineTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime restartTime;

    private int state;
    private String version;
}
