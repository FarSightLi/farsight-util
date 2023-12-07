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

/**
 * 容器信息表
 * @TableName container_info
 */
@TableName(value ="container_info")
@Data
public class ContainerInfo implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 容器名称
     */
    private String containerName;

    /**
     * 容器ID
     */
    private String containerId;

    /**
     * 主机ID
     */
    private Integer hostId;

    /**
     * 主机Ip
     */
    @TableField(exist = false)
    private String hostIp;

    /**
     * 容器镜像大小
     */
    private BigDecimal imageSize;

    /**
     * 容器CPU数量
     */
    private BigDecimal cpus;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime;

    /**
     * 版本号
     */
    private String version;

    /**
     * 内存大小
     */
    private BigDecimal memSize;

    /**
     * 磁盘大小
     */
    private BigDecimal diskSize;

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
}