package org.example.performance.controller;

import org.example.performance.component.Result;
import org.example.performance.component.exception.CodeMsg;
import org.example.performance.pojo.dto.InfoDTO;
import org.example.performance.pojo.vo.HostInfoVO;
import org.example.performance.pojo.vo.HostMetricsVO;
import org.example.performance.service.HostInfoService;
import org.example.performance.service.HostMetricsService;
import org.example.performance.util.MyUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 主机信息Controller
 * @date 2023/12/8 11:25:00
 */
@RestController
@RequestMapping("/host")
public class HostInfoController {
    @Resource
    private HostInfoService hostInfoService;
    @Resource
    private HostMetricsService hostMetricsService;

    @GetMapping("/info/{ip}")
    public Result<HostInfoVO> getHostInfo(@PathVariable String ip) {
        return Result.success(hostInfoService.getHostInfo(ip));
    }

    /**
     * 通过Ip查找性能指标图表信息
     *
     * @param infoDTO
     * @return
     */
    @PostMapping("/metric")
    public Result<List<HostMetricsVO>> getMetric(@RequestBody InfoDTO infoDTO) {
        LocalDateTime startTime = infoDTO.getStartTime();
        LocalDateTime endTime = infoDTO.getEndTime();
        if (MyUtil.validTime(startTime, endTime)) {
            return Result.error(CodeMsg.PARAMETER_ERROR, "时间间隔不正确");
        } else {
            return Result.success(hostMetricsService.getMetricsVO(infoDTO.getIp(), startTime, endTime));
        }
    }

}
