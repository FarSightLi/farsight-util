package org.example.performance.component;

import java.time.LocalDateTime;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 定义具有更新时间字段的接口
 * @date 2023/12/14 09:38:40
 */
public interface HasUpdateTime {
    LocalDateTime getUpdateTime();
}
