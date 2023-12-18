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
 * 磁盘信息表
 *
 * @TableName disk_info
 */
@TableName(value = "disk_info")
@Data
public class DiskInfo implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 磁盘分区名称
     */
    private String dfName;

    /**
     * 磁盘分区总大小
     */
    private BigDecimal dfSize;

    /**
     * 磁盘已使用大小
     */
    private BigDecimal diskUsedSize;

    /**
     * Inode使用率
     */
    private BigDecimal inodeUsedRate;

    /**
     * IO使用率
     */
    private BigDecimal ioRate;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /**
     * 关联的主机id
     */
    private Long hostId;

    /**
     * 关联的主机ip
     */
    @TableField(exist = false)
    private String hostIp;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}