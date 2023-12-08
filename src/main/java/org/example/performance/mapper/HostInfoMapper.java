package org.example.performance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.performance.pojo.po.HostInfo;

import java.util.List;

/**
* @author bonree
* @description 针对表【system_info(系统信息表)】的数据库操作Mapper
* @createDate 2023-12-06 14:14:04
* @Entity org.example.performance.po.SystemInfo
*/
@Mapper

public interface HostInfoMapper extends BaseMapper<HostInfo> {
    int updateOrInsertBatch(@Param("hostInfoCollection") List<HostInfo> hostInfoCollection);

    HostInfo getOneByIp(@Param("ip") String ip);
}




