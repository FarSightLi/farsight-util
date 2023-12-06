package org.example.performance.component;

import lombok.extern.slf4j.Slf4j;
import org.example.performance.service.InfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ScheduledTask {
    @Value("#{'${ipList}'.split(',')}")
    private List<String> ipList = new ArrayList<>();

    private final InfoService infoService = new InfoService();

    /**
     * 获得主机信息
     */
    @Scheduled(fixedRate = 10 * 60 * 1000) //十分钟
    public void getSysInfoTask() {
        List<CompletableFuture<Void>> sysFutures = new ArrayList<>();
        // 主机信息采集
        ipList.forEach(ip -> {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        infoService.getSysInfo(SessionConfig.getSession(ip), ip);
                    }, ThreadPoolConfig.getSys());
                    sysFutures.add(future);
                }
        );
        sysFutures.forEach(CompletableFuture::join);
        log.info("所有主机采集完毕");
    }

    @Scheduled(fixedRate = 10 * 60 * 1000) //十分钟
    public void getContainerInfoTask() {
        if (InfoCache.CONTAINER_MAP.isEmpty()) {
            log.info("容器id列表暂未采集，开始采集主机信息");
            getSysInfoTask();
        }
        // 容器信息采集
        List<CompletableFuture<Void>> containerFutures = new ArrayList<>();
        InfoCache.CONTAINER_MAP.forEach((ip, idList) -> {
            idList.forEach(id -> {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    infoService.getContainerInfo(SessionConfig.getSession(ip), id, ip);
                }, ThreadPoolConfig.getContainer());
                containerFutures.add(future);
            });
        });
        containerFutures.forEach(CompletableFuture::join);
        log.info("所有容器采集完毕");
    }

    /**
     * 获得主机性能指标
     */
    @Scheduled(fixedDelay = 60 * 1000) //每分钟
    public void getSysIndexTask() {
        List<CompletableFuture<Void>> sysFutures = new ArrayList<>();
        // 主机性能采集
        ipList.forEach(ip -> {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        infoService.getSysIndex(SessionConfig.getSession(ip), ip);
                    }, ThreadPoolConfig.getSys());
                    sysFutures.add(future);
                }
        );
        sysFutures.forEach(CompletableFuture::join);
        log.info("所有主机性能采集完毕");
    }

    /**
     * 获得容器信息
     */
    @Scheduled(fixedDelay = 60 * 1000) //每分钟
    public void getContainerIndexInfoTask() {
        if (InfoCache.CONTAINER_MAP.isEmpty()) {
            log.info("容器id列表暂未采集，开始采集主机信息");
            getSysInfoTask();
        }
        // 容器信息采集
        List<CompletableFuture<Void>> containerFutures = new ArrayList<>();
        InfoCache.CONTAINER_MAP.forEach((ip, idList) -> {
            idList.forEach(id -> {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    infoService.getContainerIndexInfo(SessionConfig.getSession(ip), id, ip);
                }, ThreadPoolConfig.getContainer());
                containerFutures.add(future);
            });
        });
        containerFutures.forEach(CompletableFuture::join);
        log.info("所有容器采集完毕");
    }


}
