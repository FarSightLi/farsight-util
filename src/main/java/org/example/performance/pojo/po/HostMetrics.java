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
 * 主机指标表
 * @TableName host_metrics
 */
@TableName(value ="host_metrics")
@Data
public class HostMetrics implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 主机ID
     */
    private Integer hostId;

    /**
     * 主机内存使用量
     */
    private BigDecimal mem;

    /**
     * 主机网络流量IN(MB/s)
     */
    private BigDecimal byteIn;

    /**
     * 主机负载(1min)
     */
    private BigDecimal hostLoad;

    /**
     * 主机CPU使用率(%)
     */
    private BigDecimal cpu;

    /**
     * 主机磁盘(根目录)使用率(%)
     */
    private BigDecimal disk;

    /**
     * 主机TCP连接数
     */
    private BigDecimal tcp;

    /**
     * 主机内存使用率(%)
     */
    private BigDecimal memRate;

    /**
     * 主机磁盘IO使用率(根目录,% )
     */
    private BigDecimal io;

    /**
     * 主机磁盘INODE使用率(根目录,%)
     */
    private BigDecimal inode;

    /**
     * 主机网络流量OUT(MB/s)
     */
    private BigDecimal byteOut;

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