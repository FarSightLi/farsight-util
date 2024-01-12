package org.example.performance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.performance.pojo.po.JobLock;

/**
 * @author bonree
 * @description 针对表【job_lock(定时任务分布式锁)】的数据库操作Service
 * @createDate 2023-12-22 18:41:52
 */
public interface JobLockService extends IService<JobLock> {
    /**
     * 尝试获得一把锁
     *
     * @param name
     * @param interval 分钟为单位
     * @return
     */
    boolean getLock(String name, int interval);

    void releaseLock(String name);
}
