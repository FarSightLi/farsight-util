package org.example.performance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.performance.pojo.po.HostInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author bonree
 * @description 针对表【system_info(系统信息表)】的数据库操作Service
 * @createDate 2023-12-06 14:14:04
 */
public interface HostInfoService extends IService<HostInfo> {
    void updateOrInsertBatch(List<HostInfo> hostInfoList);

    /**
     * 获得ip对应的主机id。如果某个ip没有获得信息，则会抛出异常
     *
     * @param ipList ip集合
     * @return ip对应id的map
     * @Throw BusinessException
     */
    Map<String, Integer> getIp2IdMap(Collection<String> ipList);
}
