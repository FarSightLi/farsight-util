package org.example.performance.component;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 一些常用信息的缓存
 * @date 2023/12/7 17:47:31
 */
@Component
@Slf4j
@Scope("singleton")
public class CacheInfo {

    private static List<String> ipList;

    public static List<String> getIpList() {
        if (ObjectUtil.isEmpty(ipList)) {
            log.error("ipList为null");
            throw new BusinessException(CodeMsg.SYSTEM_ERROR);
        }
        return ipList;
    }

    public static void setIpList(List<String> newIpList) {
        ipList = newIpList;
    }

    private CacheInfo() {

    }

}
