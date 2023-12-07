package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.mapper.DiskInfoMapper;
import org.example.performance.pojo.po.DiskInfo;
import org.example.performance.service.DiskInfoService;
import org.example.performance.service.HostInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    @Resource
    private HostInfoService hostInfoService;

    @Override
    public void saveDiskInfo(List<DiskInfo> diskInfoList) {
        // ip对应磁盘信息的map
        Map<String, List<DiskInfo>> diskInfoMap = diskInfoList.stream().collect(Collectors.groupingBy(DiskInfo::getHostIp));
        Set<String> ipSet = diskInfoMap.keySet();
        Map<String, Integer> ip2IdMap = hostInfoService.getIp2IdMap(ipSet);
        List<DiskInfo> saveDiskList = diskInfoList.stream().map(diskInfo -> {
            diskInfo.setHostId(ip2IdMap.get(diskInfo.getHostIp()));
            diskInfo.setUpdateTime(LocalDateTime.now());
            return diskInfo;
        }).collect(Collectors.toList());
        getBaseMapper().insertBatch(saveDiskList);
        log.info("磁盘信息:" + saveDiskList);
    }
}




