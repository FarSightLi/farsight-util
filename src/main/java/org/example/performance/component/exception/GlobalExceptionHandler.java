package org.example.performance.component.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.Result;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 全局异常处理器
 * @date 2023/12/8 18:31:39
 */
@ControllerAdvice
@ResponseBody
@Slf4j
@Order(10000)
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result handleMyCustomException(BusinessException ex) {
        StackTraceElement caller = ex.getStackTrace()[0];
        String locationInfo = caller.getClassName() + "." + caller.getMethodName() + "() at line " + caller.getLineNumber();
        log.info(locationInfo);
        log.info("全局异常处理生效" + ex.getMessage());
        return Result.error(ex);
    }
}