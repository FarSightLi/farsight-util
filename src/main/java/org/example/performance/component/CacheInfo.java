package org.example.performance.component;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;
import org.example.performance.service.ContainerInfoService;
import org.example.performance.service.HostInfoService;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 一些常用信息的缓存
 * @date 2023/12/7 17:47:31
 */
@Component
@Slf4j
@Scope("singleton")
public class CacheInfo {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private ContainerInfoService containerInfoService;
    @Resource
    private HostInfoService hostInfoService;

    private static final String IP_CONTAINER_KEY = "container:ip_container";
    private static final String IP_ID_KEY = "host:ip_id";

    @Getter
    @Setter
    private List<String> ipList;

    private Map<String, Integer> ip2idMap;

    public Map<String, Integer> getIp2idMap() {
        Object ipIdrMapObject = redisTemplate.opsForValue().get(IP_ID_KEY);
        if (ipIdrMapObject == null) {
            log.info("没有在redis中获取到ip2IdMap");
            return hostInfoService.getIp2IdMap(ipList);
        }
        if (ObjectUtil.isEmpty(ip2idMap)) {
            ip2idMap = hostInfoService.getIp2IdMap(ipList);
        }
        return ip2idMap;
    }

    public void setIp2idMap(Map<String, Integer> ip2idMap) {
        this.ip2idMap = ip2idMap;
    }


    private CacheInfo() {

    }

    public Map<String, List<String>> getContainerMap() {
        Object containerMapObject = redisTemplate.opsForValue().get(IP_CONTAINER_KEY);
        if (containerMapObject == null) {
            log.info("没有获得ip2ContainerMap缓存信息");
            return containerInfoService.getContainerId(ipList);
        }
        if (containerMapObject instanceof HashMap) {
            return (HashMap<String, List<String>>) containerMapObject;
        } else {
            // 可以选择抛出异常或返回一个默认值
            throw new BusinessException(CodeMsg.SYSTEM_ERROR, "ip2ContainerMap在从redis中获取时类型出错");
        }

    }

    /**
     * 对缓存进行增量更新
     *
     * @param newData
     */
    public void updateCache(Map<String, List<String>> newData) {
        redisTemplate.opsForValue().set(IP_CONTAINER_KEY, newData);
        log.info("ip对应容器信息缓存已更新");
    }

}
