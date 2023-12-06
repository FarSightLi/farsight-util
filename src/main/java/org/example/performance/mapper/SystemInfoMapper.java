package org.example.performance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.performance.pojo.po.SystemInfo;

/**
* @author bonree
* @description 针对表【system_info(系统信息表)】的数据库操作Mapper
* @createDate 2023-12-06 14:14:04
* @Entity org.example.performance.po.SystemInfo
*/
@Mapper

public interface SystemInfoMapper extends BaseMapper<SystemInfo> {

}




