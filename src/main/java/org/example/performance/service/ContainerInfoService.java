package org.example.performance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.performance.pojo.po.ContainerInfo;

import java.util.List;
import java.util.Map;

/**
 * @author bonree
 * @description 针对表【container_info(容器信息表)】的数据库操作Service
 * @createDate 2023-12-06 14:14:04
 */
public interface ContainerInfoService extends IService<ContainerInfo> {
    /**
     * 获得Ip对应的容器id的列表
     *
     * @param ipList ip的List
     * @return Ip对应的容器id的列表
     */
    Map<String, List<String>> getContainerId(List<String> ipList);

    /**
     * 保存或更新容器信息(容器信息要包含主机IP)
     * <p>
     * 需要注意的是，是通过ContainerId是否存在来判断是更新还是插入的
     *
     * @param containerInfoList 容器信息列表
     */
    void updateOrInsertContainer(List<ContainerInfo> containerInfoList);

    List<ContainerInfo> getListByContainerIdList(List<String> containerIdList);

    /**
     * 获得容器id对应的全局唯一id uniqueCode
     * <p>
     * 如果此容器id无法对应一个uniqueCode，则会抛出异常
     *
     * @return
     */
    Long getCodeByContainerId(String containerId);
}
