package org.example.performance.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}