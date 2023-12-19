package org.example.performance.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;
import org.example.performance.mapper.ContainerInfoMapper;
import org.example.performance.pojo.po.ContainerInfo;
import org.example.performance.service.ContainerInfoService;
import org.example.performance.service.HostInfoService;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    //    @Resource
//    private HashOperations<String, String, Long> hashOperations;
    private static final String ID_CODE_KEY = "container:id_code";


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
        LocalDateTime now = LocalDateTime.now();
        containerInfoList.forEach(containerInfo -> {
            containerInfo.setId(IdUtil.getSnowflakeNextId());
            containerInfo.setHostId(ip2IdMap.get(containerInfo.getHostIp()));
            containerInfo.setUpdateTime(now);
        });
        baseMapper.updateOrInsertBatch(containerInfoList);
        log.info("主机对应的容器id更新完毕");
    }

    @Override
    public List<ContainerInfo> getListByContainerIdList(List<String> containerIdList) {
        return baseMapper.selectList(new LambdaQueryWrapper<ContainerInfo>().in(ContainerInfo::getContainerId, containerIdList));
    }

    @Override
    public Long getCodeByContainerId(String containerId) {
        HashOperations<String, String, Long> opsedForHash = redisTemplate.opsForHash();
        Long code = opsedForHash.get(ID_CODE_KEY, containerId);
        if (ObjectUtil.isNotEmpty(code)) {
            return code;
        } else {
            Map<String, Long> id2CodeMap = getAndFreshId2CodeMap();
            return Optional.ofNullable(id2CodeMap.get(containerId))
                    .orElseThrow(() -> {
                        log.error("未在数据库中查询到容器{}的code信息", containerId);
                        return new BusinessException(CodeMsg.SYSTEM_ERROR);
                    });
        }
    }

    /**
     * 获得容器id对应code的map，并刷新redis缓存
     *
     * @return
     */
    private Map<String, Long> getAndFreshId2CodeMap() {
        HashOperations<String, String, Long> opsedForHash = redisTemplate.opsForHash();
        Map<String, Long> map = lambdaQuery().select(ContainerInfo::getContainerId, ContainerInfo::getId).list()
                .stream().collect(Collectors.toMap(ContainerInfo::getContainerId, ContainerInfo::getId));
        // 容器信息较多，设计为hash类型缓存更合适
        opsedForHash.putAll(ID_CODE_KEY, map);
        redisTemplate.expire(ID_CODE_KEY, 10L, TimeUnit.MINUTES);
        log.info("容器id2Code缓存已刷新");
        return map;
    }

}




