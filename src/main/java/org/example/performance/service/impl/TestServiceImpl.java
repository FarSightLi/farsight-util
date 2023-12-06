package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.performance.mapper.TestMapper;
import org.example.performance.po.Test;
import org.example.performance.service.TestService;
import org.springframework.stereotype.Service;

/**
 * @author bonree
 * @description 针对表【test】的数据库操作Service实现
 * @createDate 2023-12-06 09:37:07
 */
@Service
public class TestServiceImpl extends ServiceImpl<TestMapper, Test>
        implements TestService {

}




