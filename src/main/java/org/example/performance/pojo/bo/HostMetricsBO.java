package org.example.performance.pojo.bo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.HasUpdateTime;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;
import org.example.performance.pojo.po.DiskInfo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 主机指标表
 *
 * @TableName host_metrics
 */
@TableName(value = "host_metrics")
@Data
@Slf4j
public class HostMetricsBO implements Serializable, HasUpdateTime {

    public enum Type {
        MEM, BYTIN, LOAD, CPU, DISK, TCP, MEM_RATE, IO, INODE, BYOUT

    }

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 主机ID
     */
    private Long hostId;

    /**
     * 主机Ip
     */
    @TableField(exist = false)
    private String hostIp;

    /**
     * 磁盘信息
     */
    @TableField(exist = false)
    private List<DiskInfo> diskInfoList;

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
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 传入一个字段type枚举来动态获得字段值
     *
     * @param type 字段枚举
     * @return 返回字段对应的值
     */
    public BigDecimal getValueByType(Type type) {
        if (Type.MEM.equals(type)) {
            return mem;
        } else if (Type.BYTIN.equals(type)) {
            return byteIn;
        } else if (Type.LOAD.equals(type)) {
            return hostLoad;
        } else if (Type.CPU.equals(type)) {
            return cpu;
        } else if (Type.DISK.equals(type)) {
            return disk;
        } else if (Type.TCP.equals(type)) {
            return tcp;
        } else if (Type.MEM_RATE.equals(type)) {
            return memRate;
        } else if (Type.IO.equals(type)) {
            return io;
        } else if (Type.INODE.equals(type)) {
            return inode;
        } else if (Type.BYOUT.equals(type)) {
            return byteOut;
        } else {
            log.error("不支持的字段类型{}", type);
            throw new BusinessException(CodeMsg.SYSTEM_ERROR);
        }
    }

    /**
     * 传入一个字段type来动态获得字段值
     *
     * @param type 字段类型
     * @return 返回字段对应的值
     */
    public BigDecimal getValueByType(String type) {
        switch (type) {
            case "mem":
                return mem;
            case "bytin":
                return byteIn;
            case "load":
                return hostLoad;
            case "cpu":
                return cpu;
            case "disk":
                return disk;
            case "tcp":
                return tcp;
            case "mem_rate":
                return memRate;
            case "io":
                return io;
            case "inode":
                return inode;
            case "bytout":
                return byteOut;
            default:
                log.error("不支持的字段类型{}", type);
                throw new BusinessException(CodeMsg.SYSTEM_ERROR);
        }
    }

    /**
     * 传入一个字段type来动态获得字段值
     *
     * @param type 字段类型
     * @return 返回字段对应的值
     */
    public void setValue(String type, BigDecimal value) {
        switch (type) {
            case "mem":
                mem = value;
                break;
            case "bytin":
                byteIn = value;
                break;
            case "load":
                hostLoad = value;
                break;
            case "cpu":
                cpu = value;
                break;
            case "disk":
                disk = value;
                break;
            case "tcp":
                tcp = value;
                break;
            case "mem_rate":
                memRate = value;
                break;
            case "io":
                io = value;
                break;
            case "inode":
                inode = value;
                break;
            case "bytout":
                byteOut = value;
                break;
            default:
                log.error("不支持的字段类型{}", type);
                throw new BusinessException(CodeMsg.SYSTEM_ERROR);
        }
    }
}