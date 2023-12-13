package org.example.performance.controller;

import org.example.performance.component.Result;
import org.example.performance.pojo.dto.InfoDTO;
import org.example.performance.pojo.vo.ContainerInfoVO;
import org.example.performance.pojo.vo.ContainerTrendVO;
import org.example.performance.service.ContainerMetricsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 容器的controller
 * @date 2023/12/11 16:23:47
 */
@RestController
@RequestMapping("/container")
public class ContainerController {
    @Resource
    private ContainerMetricsService containerMetricsService;

    /**
     * 查看某个时段某主机的容器信息
     *
     * @param dto
     */
    @PostMapping("/metric")
    public Result<List<ContainerInfoVO>> getMetric(@RequestBody InfoDTO dto) {
        return Result.success(containerMetricsService.getContainerMetricsByIp(dto.getIp(), dto.getStartTime(), dto.getEndTime()));
    }

    /**
     * 获得容器的图表信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/metricTrend")
    public Result<List<ContainerTrendVO>> metricTrend(@RequestBody InfoDTO dto) {
        return Result.success(containerMetricsService.getMetricTrend(dto.getIp(), dto.getStartTime(), dto.getEndTime()));
    }
}
