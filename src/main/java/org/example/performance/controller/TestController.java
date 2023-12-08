package org.example.performance.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.performance.component.Result;
import org.example.performance.pojo.po.Test;
import org.example.performance.service.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 测试Controller
 * @date 2023/12/6 09:11:57
 */
@RestController
@RequestMapping("/test")
public class TestController {
    @Resource
    private TestService testService;

    @GetMapping("/test")
    public Result test() {
        return Result.success();
    }

    @RequestMapping("/testDB")
    public Result<List<Test>> testDB() {
        return Result.success(testService.list(new QueryWrapper<>()));
    }
}
