package org.example.performance.controller;

import cn.hutool.jwt.JWTUtil;
import org.example.performance.component.Result;
import org.example.performance.component.aop.NotIdentify;
import org.example.performance.component.exception.CodeMsg;
import org.example.performance.pojo.dto.AccountDTO;
import org.example.performance.pojo.po.UserAccount;
import org.example.performance.service.UserAccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 安全验证相关
 * @date 2023/12/8 14:24:56
 */
@RestController
@RequestMapping("/security")
@NotIdentify
public class SecurityController {
    @Value("${jwt.secret}")
    private String secret;
    @Resource
    private UserAccountService userAccountService;

    /**
     * 登录
     *
     * @param accountDTO
     * @return
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody AccountDTO accountDTO) {
        String md5 = userAccountService.aesToMd5(accountDTO.getPwd());
        if (userAccountService.lambdaQuery().eq(UserAccount::getUserName, accountDTO.getUserName()).eq(UserAccount::getPwd, md5).count() == 1L) {
            HashMap<String, Object> payloadMap = new HashMap<>(0);
            return Result.success(JWTUtil.createToken(payloadMap, secret.getBytes()));
        } else {
            return Result.error(CodeMsg.PARAMETER_ERROR, "账号不对");
        }
    }

    /**
     * 注册
     *
     * @param accountDTO
     * @return
     */
    @PostMapping("/sign")
    public Result<String> sign(@RequestBody AccountDTO accountDTO) {
        String md5 = userAccountService.aesToMd5(accountDTO.getPwd());
        if (userAccountService.lambdaQuery().eq(UserAccount::getUserName, accountDTO.getUserName()).count() == 0) {
            HashMap<String, Object> payloadMap = new HashMap<>(0);
            UserAccount userAccount = new UserAccount();
            userAccount.setUserName(accountDTO.getUserName());
            userAccount.setPwd(md5);
            userAccountService.save(userAccount);
            return Result.success(JWTUtil.createToken(payloadMap, secret.getBytes()));
        } else {
            return Result.error(CodeMsg.PARAMETER_ERROR, "用户名已注册");
        }
    }
}
