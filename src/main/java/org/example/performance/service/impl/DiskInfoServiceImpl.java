package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.performance.pojo.po.DiskInfo;
import org.example.performance.service.DiskInfoService;
import org.example.performance.mapper.DiskInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author bonree
* @description 针对表【disk_info(磁盘信息表)】的数据库操作Service实现
* @createDate 2023-12-06 14:14:04
*/
@Service
public class DiskInfoServiceImpl extends ServiceImpl<DiskInfoMapper, DiskInfo>
    implements DiskInfoService{

}




