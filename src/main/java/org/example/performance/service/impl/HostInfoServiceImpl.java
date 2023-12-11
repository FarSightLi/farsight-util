package org.example.performance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;
import org.example.performance.mapper.DiskInfoMapper;
import org.example.performance.mapper.HostInfoMapper;
import org.example.performance.pojo.po.DiskInfo;
import org.example.performance.pojo.po.HostInfo;
import org.example.performance.pojo.vo.DiskInfoVO;
import org.example.performance.pojo.vo.HostInfoVO;
import org.example.performance.service.HostInfoService;
import org.example.performance.util.DataUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bonree
 * @description 针对表【system_info(系统信息表)】的数据库操作Service实现
 * @createDate 2023-12-06 14:14:04
 */
@Service
@Slf4j
public class HostInfoServiceImpl extends ServiceImpl<HostInfoMapper, HostInfo>
        implements HostInfoService {
    @Resource
    private HostInfoMapper hostInfoMapper;
    @Resource
    private DiskInfoMapper diskInfoMapper;

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void updateOrInsertBatch(List<HostInfo> hostInfoList) {
        hostInfoMapper.updateOrInsertBatch(hostInfoList);
    }

    @Override
    public Map<String, Integer> getIp2IdMap(Collection<String> ipList) {
        Set<String> ipSet = new HashSet<>(ipList);
        List<HostInfo> hostInfoList = hostInfoMapper.selectList(new LambdaQueryWrapper<HostInfo>().in(HostInfo::getIp, ipSet).select(HostInfo::getId, HostInfo::getIp));
        // ip对应主机id的map
        Map<String, Integer> ip2IdMap = hostInfoList.stream().collect(Collectors.toMap(HostInfo::getIp, HostInfo::getId));
        // 有ip没查到对应主机id
        if (ip2IdMap.keySet().size() != ipSet.size()) {
            Set<String> differenceSet = new HashSet<>(ipList);
            differenceSet.removeAll(ip2IdMap.keySet());
            log.warn("有ip没查询到对应的主机id信息，ip为：{}", differenceSet);
            throw new BusinessException(CodeMsg.SYSTEM_ERROR);
        }
        return ip2IdMap;
    }

    @Override
    public HostInfoVO getHostInfo(String ip) {
        HostInfoVO hostInfoVO = new HostInfoVO();
        HostInfo hostInfo = hostInfoMapper.getOneByIp(ip);
        Integer hostId = hostInfo.getId();
        List<DiskInfo> diskInfoList = diskInfoMapper.selectNewestByHostId(hostId);
        BeanUtils.copyProperties(hostInfo, hostInfoVO);
        hostInfoVO.setDiskInfoVO(getDiskInfoVO(diskInfoList));
        return hostInfoVO;
    }

    private DiskInfoVO getDiskInfoVO(List<DiskInfo> diskInfoList) {
        DiskInfoVO diskInfoVO = new DiskInfoVO();
        List<DiskInfoVO.DiskDetail> details = new ArrayList<>();
        BigDecimal totalSize = BigDecimal.valueOf(0.00);
        BigDecimal usedSize = BigDecimal.valueOf(0.00);
        for (DiskInfo e : diskInfoList) {
            totalSize = totalSize.add(e.getDfSize());
            usedSize = usedSize.add(e.getDiskUsedSize());

            DiskInfoVO.DiskDetail diskDetail = new DiskInfoVO.DiskDetail();
            diskDetail.setDiskUsedSize(e.getDiskUsedSize());
            diskDetail.setIoRate(e.getIoRate());
            diskDetail.setDfName(e.getDfName());
            diskDetail.setInodeUsedRate(e.getInodeUsedRate());
            diskDetail.setDfSize(e.getDfSize());
            diskDetail.setDiskUsedRate(e.getInodeUsedRate());
            details.add(diskDetail);
        }
        BigDecimal rate = usedSize.divide(totalSize, 2, RoundingMode.HALF_UP);
        diskInfoVO.setUsedRate(rate);
        diskInfoVO.setTotalSize(DataUtil.mb2Gb(totalSize) + "GB");
        diskInfoVO.setUsedSize(DataUtil.mb2Gb(usedSize) + "GB");
        diskInfoVO.setPartitions(details);
        return diskInfoVO;
    }
}




