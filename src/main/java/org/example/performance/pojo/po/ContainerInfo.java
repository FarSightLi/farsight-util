package org.example.performance.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}