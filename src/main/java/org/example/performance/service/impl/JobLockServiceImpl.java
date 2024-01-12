package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.mapper.JobLockMapper;
import org.example.performance.pojo.po.JobLock;
import org.example.performance.service.JobLockService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author bonree
 * @description 针对表【job_lock(定时任务分布式锁)】的数据库操作Service实现
 * @createDate 2023-12-22 18:41:52
 */
@Service
@Slf4j
public class JobLockServiceImpl extends ServiceImpl<JobLockMapper, JobLock>
        implements JobLockService {

    @Override
    public boolean getLock(String name, int interval) {
        try {
            JobLock jobLock = this.lambdaQuery().eq(JobLock::getJobName, name).one();
            LocalDateTime now = LocalDateTime.now();
            // 有锁，未过期
            if (jobLock != null && now.isBefore(jobLock.getExpirationTime())) {
                return false;
            }
            // 有锁，已过期
            if (jobLock != null && now.isAfter(jobLock.getExpirationTime())) {
                // 删除老锁
                releaseLock(name);
            }
            // 没锁,创建新锁
            save(name, interval);
            return true;
        } catch (Exception e) {
            log.error("获取锁时失败了：{}", e.getMessage());
        }
        return false;
    }

    @Override
    public void releaseLock(String name) {
        lambdaUpdate().eq(JobLock::getJobName, name).remove();
    }

    private void save(String name, int interval) {
        JobLock jobLock = new JobLock();
        jobLock.setJobName(name);
        jobLock.setExpirationTime(LocalDateTime.now().plusMinutes(interval));
        save(jobLock);
    }
}




