package org.example.performance.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务分布式锁
 *
 * @TableName job_lock
 */
@TableName(value = "job_lock")
@Data
public class JobLock implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 创建时间
     */
    private LocalDateTime expirationTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}