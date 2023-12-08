package org.example.performance.component.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 身份验证注解
 * @date 2023/12/8 13:57:37
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotIdentify {
}
