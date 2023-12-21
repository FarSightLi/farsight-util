package org.example.performance.pojo.bo;

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
 * 主机指标BO
 */
@Data
@Slf4j
public class HostMetricsBO implements Serializable, HasUpdateTime {

    public enum Type {
        MEM, BYTIN, LOAD, CPU, DISK, TCP, MEM_RATE, IO, INODE, BYOUT

    }

    /**
     * ID
     */
    private Integer id;

    /**
     * 主机ID
     */
    private Long hostId;

    /**
     * 主机Ip
     */
    private String hostIp;

    /**
     * 磁盘信息
     */
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
    private LocalDateTime updateTime;

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
     * <p>
     * 如果不支持该字段，将会返回null
     *
     * @param type 字段类型
     * @return 返回字段对应的值
     */
    public BigDecimal getValueByType(String type) {
        switch (type) {
            case "host.mem.sum":
                return mem;
            case "host.network.bytin":
                return byteIn;
            case "host.load.avg":
                return hostLoad;
            case "host.cpu.rate":
                return cpu;
            case "host.disk.rate":
                return disk;
            case "host.network.tcp":
                return tcp;
            case "host.mem.rate":
                return memRate;
            case "host.disk.io.rate":
                return io;
            case "host.disk.inode.rate":
                return inode;
            case "host.network.bytout":
                return byteOut;
            default:
                log.error("不支持的字段类型{}", type);
                return null;
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
            case "host.mem.sum":
                mem = value;
                break;
            case "host.network.bytin":
                byteIn = value;
                break;
            case "host.load.avg":
                hostLoad = value;
                break;
            case "host.cpu.rate":
                cpu = value;
                break;
            case "host.disk.rate":
                disk = value;
                break;
            case "host.network.tcp":
                tcp = value;
                break;
            case "host.mem.rate":
                memRate = value;
                break;
            case "host.disk.io.rate":
                io = value;
                break;
            case "host.disk.inode.rate":
                inode = value;
                break;
            case "host.network.bytout":
                byteOut = value;
                break;
            default:
                log.error("不支持的字段类型{}", type);
        }
    }
}