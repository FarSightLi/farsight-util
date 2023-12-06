package org.example.performance.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.Data;

/**
 * 系统信息表
 * @TableName system_info
 */
@TableName(value ="system_info")
@Data
public class SystemInfo implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * CPU架构
     */
    private String cpuArch;

    /**
     * CPU核数
     */
    private Integer cpuCores;

    /**
     * 磁盘信息id
     */
    private Integer diskInfo;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 内核版本
     */
    private String kernelRelease;

    /**
     * 内存大小
     */
    private BigDecimal memSize;

    /**
     * 系统版本
     */
    private String sysVersion;

    /**
     * 更新时间
     */
    private LocalTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}