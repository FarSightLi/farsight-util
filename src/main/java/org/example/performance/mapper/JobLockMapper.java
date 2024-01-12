package org.example.performance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.performance.pojo.po.JobLock;

/**
 * @author bonree
 * @description 针对表【job_lock(定时任务分布式锁)】的数据库操作Mapper
 * @createDate 2023-12-26 11:30:46
 * @Entity org.example.performance.pojo.po.JobLock
 */
@Mapper
public interface JobLockMapper extends BaseMapper<JobLock> {

}




