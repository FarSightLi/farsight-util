package org.example.performance.pojo.po;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统信息表
 *
 * @TableName host_info
 */
@TableName(value = "host_info")
@Data
public class HostInfo implements Serializable {
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
     * IP地址
     */
    private String ip;


    /**
     * 主机名字
     */
    private String hostName;

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
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<String> containerIdList;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}