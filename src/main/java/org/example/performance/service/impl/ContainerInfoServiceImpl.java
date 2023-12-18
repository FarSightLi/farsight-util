package org.example.performance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.mapper.ContainerInfoMapper;
import org.example.performance.pojo.po.ContainerInfo;
import org.example.performance.service.ContainerInfoService;
import org.example.performance.service.HostInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author bonree
 * @description 针对表【container_info(容器信息表)】的数据库操作Service实现
 * @createDate 2023-12-06 14:14:04
 */
@Service
@Slf4j
public class ContainerInfoServiceImpl extends ServiceImpl<ContainerInfoMapper, ContainerInfo>
        implements ContainerInfoService {
    @Resource
    private HostInfoService hostInfoService;

    @Override
    public Map<String, List<String>> getContainerId(List<String> ipList) {
        Map<String, Long> ip2IdMap = hostInfoService.getIp2IdMap(ipList);
        Map<Long, List<String>> containerMap = getBaseMapper().selectList(new LambdaQueryWrapper<ContainerInfo>()
                        .in(ContainerInfo::getHostId, ip2IdMap.values())
                        .select(ContainerInfo::getContainerId, ContainerInfo::getHostId))
                .stream().collect(Collectors.groupingBy(ContainerInfo::getHostId,
                        Collectors.mapping(ContainerInfo::getContainerId, Collectors.toList())));

        // 以 ip 为 key 的结果,value为 ContainerIdList 的Map
        Map<String, List<String>> containerIdMap = new HashMap<>();
        containerMap.forEach((hostId, containerInfoList) -> {
            // 通过 hostId 在 ip2IdMap 中找到对应的 ip
            ip2IdMap.entrySet().stream()
                    .filter(e -> e.getValue().equals(hostId))
                    .map(Map.Entry::getKey)
                    .findFirst().ifPresent(ip -> containerIdMap.put(ip, containerInfoList));
        });
        return containerIdMap;
    }

    @Override
    @Transactional
    public void updateOrInsertContainer(List<ContainerInfo> containerInfoList) {
        Map<String, Long> ip2IdMap = hostInfoService.getIp2IdMap(containerInfoList.stream().map(ContainerInfo::getHostIp).collect(Collectors.toSet()));
        containerInfoList.forEach(containerInfo -> {
            containerInfo.setHostId(ip2IdMap.get(containerInfo.getHostIp()));
            containerInfo.setUpdateTime(LocalDateTime.now());
        });
        baseMapper.updateOrInsertBatch(containerInfoList);
        log.info("主机对应的容器id更新完毕");
    }

    @Override
    public List<ContainerInfo> getListByContainerIdList(List<String> containerIdList) {
        return baseMapper.selectList(new LambdaQueryWrapper<ContainerInfo>().in(ContainerInfo::getContainerId, containerIdList));
    }
}




