package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.performance.pojo.po.SystemInfo;
import org.example.performance.service.SystemInfoService;
import org.example.performance.mapper.SystemInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author bonree
* @description 针对表【system_info(系统信息表)】的数据库操作Service实现
* @createDate 2023-12-06 14:14:04
*/
@Service
public class SystemInfoServiceImpl extends ServiceImpl<SystemInfoMapper, SystemInfo>
    implements SystemInfoService{

}




