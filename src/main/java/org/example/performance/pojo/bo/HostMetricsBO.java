package org.example.performance.pojo.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.HasUpdateTime;
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

    @Getter
    public enum FiledType {
        MEM("host.mem.sum", "主机内存使用量(MB)", "mem"),
        BYTIN("host.network.bytin", "", "bytin"),
        LOAD("host.load.avg", "主机负载(1min)", "load"),
        CPU("host.cpu.rate", "主机CPU使用率(%)", "cpu"),
        DISK("host.disk.rate", "主机磁盘使用率(/data，%)", "disk"),
        TCP("host.network.tcp", "主机TCP连接数", "tcp"),
        MEM_RATE("host.mem.rate", "主机内存使用率(%)", "mem_rate"),
        IO("host.disk.io.rate", "主机磁盘IO使用率(/data，%)", "io"),
        INODE("host.disk.inode.rate", "主机磁盘INODE使用率(/data，%)", "inode"),
        BYOUT("host.network.bytout", "主机网络流量OUT(MB/s)", "bytout");

        private final String name;
        private final String desc;
        private final String type;

        FiledType(String name, String desc, String type) {
            this.name = name;
            this.desc = desc;
            this.type = type;
        }
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
     * @param filedType 字段枚举
     * @return 返回字段对应的值
     */
    public BigDecimal getValueByType(FiledType filedType) {
        if (FiledType.MEM.equals(filedType)) {
            return mem;
        } else if (FiledType.BYTIN.equals(filedType)) {
            return byteIn;
        } else if (FiledType.LOAD.equals(filedType)) {
            return hostLoad;
        } else if (FiledType.CPU.equals(filedType)) {
            return cpu;
        } else if (FiledType.DISK.equals(filedType)) {
            return disk;
        } else if (FiledType.TCP.equals(filedType)) {
            return tcp;
        } else if (FiledType.MEM_RATE.equals(filedType)) {
            return memRate;
        } else if (FiledType.IO.equals(filedType)) {
            return io;
        } else if (FiledType.INODE.equals(filedType)) {
            return inode;
        } else if (FiledType.BYOUT.equals(filedType)) {
            return byteOut;
        } else {
            log.error("不支持的字段类型{}", filedType);
            return null;
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
        if (FiledType.MEM.getName().equals(type)) {
            return mem;
        } else if (FiledType.BYTIN.getName().equals(type)) {
            return byteIn;
        } else if (FiledType.LOAD.getName().equals(type)) {
            return hostLoad;
        } else if (FiledType.CPU.getName().equals(type)) {
            return cpu;
        } else if (FiledType.DISK.getName().equals(type)) {
            return disk;
        } else if (FiledType.TCP.getName().equals(type)) {
            return tcp;
        } else if (FiledType.MEM_RATE.getName().equals(type)) {
            return memRate;
        } else if (FiledType.IO.getName().equals(type)) {
            return io;
        } else if (FiledType.INODE.getName().equals(type)) {
            return inode;
        } else if (FiledType.BYOUT.getName().equals(type)) {
            return byteOut;
        } else {
            log.error("不支持的字段类型{}", type);
            return null;
        }
    }

    /**
     * 传入一个字段type来动态获得字段值
     *
     * @param type 字段类型
     */
    public void setValue(String type, BigDecimal value) {
        if (FiledType.MEM.getName().equals(type)) {
            mem = value;
        } else if (FiledType.BYTIN.getName().equals(type)) {
            byteIn = value;
        } else if (FiledType.LOAD.getName().equals(type)) {
            hostLoad = value;
        } else if (FiledType.CPU.getName().equals(type)) {
            cpu = value;
        } else if (FiledType.DISK.getName().equals(type)) {
            disk = value;
        } else if (FiledType.TCP.getName().equals(type)) {
            tcp = value;
        } else if (FiledType.MEM_RATE.getName().equals(type)) {
            memRate = value;
        } else if (FiledType.IO.getName().equals(type)) {
            io = value;
        } else if (FiledType.INODE.getName().equals(type)) {
            inode = value;
        } else if (FiledType.BYOUT.getName().equals(type)) {
            byteOut = value;
        } else {
            log.error("不支持的字段类型{}", type);
        }
    }
}