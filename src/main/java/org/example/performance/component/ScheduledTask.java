package org.example.performance.component;

import lombok.extern.slf4j.Slf4j;
import org.example.performance.config.SessionConfig;
import org.example.performance.config.ThreadPoolConfig;
import org.example.performance.pojo.po.*;
import org.example.performance.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ScheduledTask {
    @Value("#{'${ipList}'.split(',')}")
    private List<String> ipList = new ArrayList<>();

    private final InfoService infoService = new InfoService();
    @Resource
    private HostInfoService hostInfoService;
    @Resource
    private HostMetricsService hostMetricsService;
    @Resource
    private DiskInfoService diskInfoService;
    @Resource
    private ContainerInfoService containerInfoService;
    @Resource
    private ContainerMetricsService containerMetricsService;

    /**
     * 获得主机信息
     */
    @Async
    @Scheduled(fixedRate = 10 * 60 * 1000) //十分钟
    public void getSysInfoTask() {
        List<CompletableFuture<Void>> sysFutures = new ArrayList<>();
        List<HostInfo> hostInfoList = Collections.synchronizedList(new ArrayList<>());

        // 主机信息采集
        ipList.forEach(ip -> {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        HostInfo hostInfo = infoService.getSysInfo(SessionConfig.getSession(ip), ip);
                        hostInfoList.add(hostInfo);
                    }, ThreadPoolConfig.getSys());
                    sysFutures.add(future);
                }
        );
        sysFutures.forEach(CompletableFuture::join);
        log.info("主机信息:" + hostInfoList);
        // 保存主机信息
        hostInfoService.updateOrInsertBatch(hostInfoList);

        // 保存容器信息
        List<ContainerInfo> containerList = getContainerList(hostInfoList);
        containerInfoService.updateOrInsertContainer(containerList);
        // 保存ip和容器id对应关系到缓存
        CacheInfo.resetContainerMap(containerList
                .stream().collect(Collectors.groupingBy(ContainerInfo::getHostIp, Collectors.mapping(ContainerInfo::getContainerId, Collectors.toList()))));
        log.info("所有主机采集完毕");
    }

    /**
     * 获得容器信息
     */
    @Async
    @Scheduled(fixedRate = 10 * 60 * 1000) //十分钟
    public void getContainerInfoTask() {
        Map<String, List<String>> containerMap = containerInfoService.getContainerId(ipList);
        List<ContainerInfo> containerInfoList = Collections.synchronizedList(new ArrayList<>());
        // 容器信息采集
        List<CompletableFuture<Void>> containerFutures = new ArrayList<>();
        containerMap.forEach((ip, idList) -> idList.forEach(id -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                containerInfoList.add(infoService.getContainerInfo(SessionConfig.getSession(ip), id, ip));
            }, ThreadPoolConfig.getContainer());
            containerFutures.add(future);
        }));
        containerFutures.forEach(CompletableFuture::join);
        log.info("容器基础信息:" + containerInfoList);
        containerInfoService.updateOrInsertContainer(containerInfoList);
        log.info("所有容器基础信息采集完毕");
    }

    /**
     * 获得主机性能指标
     */
    @Async
    @Scheduled(fixedRate = 60 * 1000) //每分钟
    public void getSysIndexTask() {
        List<CompletableFuture<Void>> sysFutures = new ArrayList<>();
        List<HostMetrics> hostMetricsList = Collections.synchronizedList(new ArrayList<>());
        List<DiskInfo> diskInfoList = Collections.synchronizedList(new ArrayList<>());
        // 主机性能采集
        ipList.forEach(ip -> {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        HostMetrics hostMetrics = infoService.getSysIndex(SessionConfig.getSession(ip), ip);
                        hostMetricsList.add(hostMetrics);
                        diskInfoList.addAll(hostMetrics.getDiskInfoList());
                    }, ThreadPoolConfig.getSys());
                    sysFutures.add(future);
                }
        );
        sysFutures.forEach(CompletableFuture::join);
        log.info("主机性能指标:" + hostMetricsList);
        diskInfoService.saveDiskInfo(diskInfoList);
        hostMetricsService.insertBatch(hostMetricsList);
        log.info("所有主机性能采集完毕");
    }

    /**
     * 获得容器性能指标
     */
    @Async
    @Scheduled(fixedRate = 60 * 1000) //每分钟
    public void getContainerIndexInfoTask() {
        Map<String, List<String>> containerMap = containerInfoService.getContainerId(ipList);
        List<ContainerMetrics> containerMetricsList = Collections.synchronizedList(new ArrayList<>());
        // 容器信息采集
        List<CompletableFuture<Void>> containerFutures = new ArrayList<>();
        containerMap.forEach((ip, idList) -> idList.forEach(id -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                containerMetricsList.add(infoService.getContainerIndexInfo(SessionConfig.getSession(ip), id, ip));
            }, ThreadPoolConfig.getContainer());
            containerFutures.add(future);
        }));
        containerFutures.forEach(CompletableFuture::join);
        log.info("容器性能指标:" + containerMetricsList);
        containerMetricsService.insertBatch(containerMetricsList);
        log.info("所有容器性能指标采集完毕");
    }

    private List<ContainerInfo> getContainerList(List<HostInfo> hostInfoList) {
        return hostInfoList.stream()
                .flatMap(hostInfo -> hostInfo.getContainerIdList().stream()
                        .map(containerInfoId -> {
                            ContainerInfo containerInfo = new ContainerInfo();
                            containerInfo.setHostIp(hostInfo.getIp());
                            containerInfo.setContainerId(containerInfoId);
                            return containerInfo;
                        })
                )
                .collect(Collectors.toList());
    }

}
