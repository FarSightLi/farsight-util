package org.example.performance.component.aop;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.jwt.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 配置身份认证切面
 * @date 2023/12/8 13:57:02
 */
@Component
@Aspect
@Slf4j
public class SecurityAspect {
    @Value("${jwt.secret}")
    private String secret;

    @Pointcut("execution(* org.example.performance.controller..*(..)) && !@within(NotIdentify)")
    public void controllerMethods() {
    }

    @Before("controllerMethods()")
    public void identify(JoinPoint joinPoint) {
        HttpServletRequest request = getRequest(joinPoint);
        if (request != null) {
            String token = getToken(request);
            if (!JWTUtil.verify(token, secret.getBytes())) {
                throw new BusinessException(CodeMsg.PARAMETER_ERROR);
            }
        } else {
            log.error("无法获得request");
            throw new BusinessException(CodeMsg.PARAMETER_ERROR);
        }
    }


    private String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (ObjectUtil.isEmpty(token)) {
            throw new BusinessException(CodeMsg.PARAMETER_ERROR, "token错误");
        } else {
            return token;
        }
    }


    // 通过 JoinPoint 获取 HttpServletRequest
    private HttpServletRequest getRequest(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest();
        }
        return null;
    }
}
