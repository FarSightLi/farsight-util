package org.example.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SysTemChartInfo {
    private String ip;
    // 主机内存使用量
    private BigDecimal mem;
    //主机网络流量IN(MB/s)
    private BigDecimal byteIn;
    //主机负载(1min)
    private BigDecimal load;
    //主机CPU使用率(%)
    private BigDecimal cpu;
    //主机磁盘使用率(%)
    private String disk;
    //主机TCP连接数
    private BigDecimal tcp;
    //主机内存使用率(%)
    private BigDecimal memRate;
    //主机磁盘IO使用率(/data，%)
    private BigDecimal io;
    //主机磁盘INODE使用率(/data，%)
    private String inode;
    //"主机网络流量OUT(MB/s)
    private BigDecimal byteOut;

}
