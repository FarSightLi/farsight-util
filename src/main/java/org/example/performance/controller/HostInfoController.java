package org.example.performance.controller;

import org.example.performance.component.Result;
import org.example.performance.pojo.vo.HostInfoVO;
import org.example.performance.service.HostInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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

    @GetMapping("/info/{ip}")
    public Result<HostInfoVO> getHostInfo(@PathVariable String ip) {
        return Result.success(hostInfoService.getHostInfo(ip));
    }

}
