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
import org.example.performance.pojo.vo.SimpleContainerVO;
import org.example.performance.service.ContainerInfoService;
import org.example.performance.service.HostInfoService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private RedisTemplate<String, Long> redisTemplate;
    private static final String ID_CODE_KEY = "container:id_code";


    @Override
    public Map<String, List<String>> getContainerId(List<String> ipList) {
        List<ContainerInfo> list = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(ipList)) {
            list = lambdaQuery().in(ContainerInfo::getHostIp, ipList).select(ContainerInfo::getContainerId, ContainerInfo::getHostIp).list();
        }
        return list.stream().collect(Collectors.groupingBy(ContainerInfo::getHostIp, Collectors.mapping(ContainerInfo::getContainerId, Collectors.toList())));
    }

    @Override
    @Transactional
    public void updateOrInsertContainer(List<ContainerInfo> containerInfoList) {
        LocalDateTime now = LocalDateTime.now();
        containerInfoList.forEach(containerInfo -> {
            containerInfo.setId(IdUtil.getSnowflakeNextId());
            containerInfo.setUpdateTime(now);
        });
        baseMapper.updateOrInsertBatch(containerInfoList);
        log.info("主机对应的容器id更新完毕");
        Map<String, Long> id2CodeMap = containerInfoList.stream().collect(Collectors.toMap(ContainerInfo::getContainerId, ContainerInfo::getId));
        fresh(id2CodeMap);
    }

    @Override
    public List<ContainerInfo> getListByContainerIdList(List<String> containerIdList) {
        return baseMapper.selectList(new LambdaQueryWrapper<ContainerInfo>().in(ContainerInfo::getContainerId, containerIdList));
    }

    @Override
    public Long getCodeByContainerId(String containerId) {
        Long code = redisTemplate.opsForValue().get(ID_CODE_KEY + ":" + containerId);
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

    @Override
    public List<SimpleContainerVO> getContainerList(String ip) {
        List<ContainerInfo> infoList = lambdaQuery().eq(ContainerInfo::getHostIp, ip).select(ContainerInfo::getId, ContainerInfo::getContainerName, ContainerInfo::getVersion).list();
        return infoList.stream().map(info -> {
            SimpleContainerVO simpleContainerVO = new SimpleContainerVO();
            simpleContainerVO.setCode(info.getId());
            simpleContainerVO.setVersion(info.getVersion());
            simpleContainerVO.setName(info.getContainerName());
            return simpleContainerVO;
        }).collect(Collectors.toList());
    }

    /**
     * 获得容器id对应code的map，并刷新redis缓存
     *
     * @return
     */
    private Map<String, Long> getAndFreshId2CodeMap() {
        Map<String, Long> map = lambdaQuery().select(ContainerInfo::getContainerId, ContainerInfo::getId).list()
                .stream().collect(Collectors.toMap(ContainerInfo::getContainerId, ContainerInfo::getId));
        fresh(map);
        return map;
    }

    private void fresh(Map<String, Long> map) {
        map.forEach((k, v) -> redisTemplate.opsForValue().set(ID_CODE_KEY + ":" + k, v, 10L, TimeUnit.MINUTES));
        log.info("容器id2Code缓存已刷新");
    }
}




