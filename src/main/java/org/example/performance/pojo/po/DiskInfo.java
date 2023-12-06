package org.example.performance.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 磁盘信息表
 * @TableName disk_info
 */
@TableName(value ="disk_info")
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}