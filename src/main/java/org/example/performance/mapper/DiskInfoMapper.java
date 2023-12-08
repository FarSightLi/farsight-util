package org.example.performance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.performance.pojo.po.DiskInfo;

import java.util.List;

/**
 * @author bonree
 * @description 针对表【disk_info(磁盘信息表)】的数据库操作Mapper
 * @createDate 2023-12-06 14:14:04
 * @Entity org.example.performance.po.DiskInfo
 */
@Mapper
public interface DiskInfoMapper extends BaseMapper<DiskInfo> {
    int insertBatch(@Param("diskInfoList") List<DiskInfo> diskInfoList);

    List<DiskInfo> selectNewestByHostId(@Param("hostId") Integer hostId);
}




