package org.example.performance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.performance.pojo.po.ContainerInfo;

import java.util.Collection;

/**
 * @author bonree
 * @description 针对表【container_info(容器信息表)】的数据库操作Mapper
 * @createDate 2023-12-06 14:14:04
 * @Entity org.example.performance.po.ContainerInfo
 */
@Mapper
public interface ContainerInfoMapper extends BaseMapper<ContainerInfo> {
    void updateOrInsertBatch(@Param("containerInfoCollection") Collection<ContainerInfo> containerInfoCollection);
}




