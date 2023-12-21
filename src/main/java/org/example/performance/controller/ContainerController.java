package org.example.performance.controller;

import org.example.performance.component.Result;
import org.example.performance.component.exception.CodeMsg;
import org.example.performance.pojo.dto.ContainerInfoDTO;
import org.example.performance.pojo.dto.IpInfoDTO;
import org.example.performance.pojo.vo.ContainerInfoVO;
import org.example.performance.pojo.vo.ContainerTrendVO;
import org.example.performance.service.ContainerMetricsService;
import org.example.performance.util.ServiceUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
    public Result<List<ContainerInfoVO>> getMetric(@RequestBody IpInfoDTO dto) {
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        if (ServiceUtil.validTime(startTime, endTime)) {
            return Result.error(CodeMsg.PARAMETER_ERROR, "时间间隔不正确");
        } else {
            return Result.success(containerMetricsService.getContainerMetricsByIp(dto.getIp(), startTime, endTime));
        }

    }

    /**
     * 获得容器的图表信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/metricTrend")
    public Result<List<ContainerTrendVO>> metricTrend(@RequestBody IpInfoDTO dto) {
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        if (ServiceUtil.validTime(startTime, endTime)) {
            return Result.error(CodeMsg.PARAMETER_ERROR, "时间间隔不正确");
        } else {
            return Result.success(containerMetricsService.getMetricTrend(dto.getIp(), startTime, endTime));
        }
    }


    /**
     * 查看某个时段指定容器的容器信息
     *
     * @param dto
     */
    @PostMapping("/metricById")
    public Result<ContainerInfoVO> getMetricById(@RequestBody ContainerInfoDTO dto) {
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        if (ServiceUtil.validTime(startTime, endTime)) {
            return Result.error(CodeMsg.PARAMETER_ERROR, "时间间隔不正确");
        } else {
            return Result.success(containerMetricsService.getContainerMetricsById(dto.getId(), startTime, endTime));
        }
    }

    /**
     * 获得容器的图表信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/metricTrendById")
    public Result<List<ContainerTrendVO>> metricTrendById(@RequestBody ContainerInfoDTO dto) {
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        if (ServiceUtil.validTime(startTime, endTime)) {
            return Result.error(CodeMsg.PARAMETER_ERROR, "时间间隔不正确");
        } else {
            return Result.success(containerMetricsService.getMetricTrendById(dto.getId(), startTime, endTime));
        }
    }
}
