package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.performance.pojo.po.ContainerInfo;
import org.example.performance.service.ContainerInfoService;
import org.example.performance.mapper.ContainerInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author bonree
* @description 针对表【container_info(容器信息表)】的数据库操作Service实现
* @createDate 2023-12-06 14:14:04
*/
@Service
public class ContainerInfoServiceImpl extends ServiceImpl<ContainerInfoMapper, ContainerInfo>
    implements ContainerInfoService{

}




