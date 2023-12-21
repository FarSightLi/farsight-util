package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.mapper.DiskInfoMapper;
import org.example.performance.pojo.po.DiskInfo;
import org.example.performance.service.DiskInfoService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bonree
 * @description 针对表【disk_info(磁盘信息表)】的数据库操作Service实现
 * @createDate 2023-12-06 14:14:04
 */
@Service
@Slf4j
public class DiskInfoServiceImpl extends ServiceImpl<DiskInfoMapper, DiskInfo>
        implements DiskInfoService {
    @Override
    public void saveDiskInfo(List<DiskInfo> diskInfoList) {
        // 这样能保证一组数据的更新时间一定是相同的
        LocalDateTime now = LocalDateTime.now();
        List<DiskInfo> saveDiskList = diskInfoList.stream().map(diskInfo -> {
            diskInfo.setUpdateTime(now);
            return diskInfo;
        }).collect(Collectors.toList());
        getBaseMapper().insertBatch(saveDiskList);
        log.info("磁盘信息:" + saveDiskList);
    }
}




