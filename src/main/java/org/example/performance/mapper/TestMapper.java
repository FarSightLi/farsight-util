package org.example.performance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.performance.pojo.po.Test;

/**
 * @author bonree
 * @description 针对表【test】的数据库操作Mapper
 * @createDate 2023-12-06 09:37:07
 * @Entity org.example.po.Test
 */
@Mapper
public interface TestMapper extends BaseMapper<Test> {

}




