package org.example.performance.controller;

import cn.hutool.jwt.JWTUtil;
import org.example.performance.component.aop.NotIdentify;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 获得token
     *
     * @param userName
     * @return
     */
    @GetMapping("/getToken/{userName}")
    public String getToken(@PathVariable String userName) {
        if ("admin".equals(userName)) {
            HashMap<String, Object> payloadMap = new HashMap<>(0);
            return JWTUtil.createToken(payloadMap, secret.getBytes());
        } else {
            throw new BusinessException(CodeMsg.PARAMETER_ERROR);
        }
    }
}
