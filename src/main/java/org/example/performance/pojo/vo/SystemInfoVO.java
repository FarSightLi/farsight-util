package org.example.performance.pojo.vo;

import lombok.Data;
import org.example.performance.pojo.po.DiskInfo;

import java.math.BigDecimal;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 主机信息VO
 * @date 2023/12/6 14:17:02
 */
@Data
public class SystemInfoVO {
    private String cpuArch;
    private Integer cpuCores;
    private DiskInfo diskInfo;
    private String ip;
    // 内核版本
    private String kernelRelease;
    private BigDecimal memSize;
    private String sysVersion;
}
