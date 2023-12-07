package org.example.performance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.performance.pojo.po.DiskInfo;

import java.util.List;

/**
* @author bonree
* @description 针对表【disk_info(磁盘信息表)】的数据库操作Service
* @createDate 2023-12-06 14:14:04
*/
public interface DiskInfoService extends IService<DiskInfo> {
    void saveDiskInfo(List<DiskInfo> diskInfoList);

}
