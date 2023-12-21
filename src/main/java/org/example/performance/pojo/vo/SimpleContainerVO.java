package org.example.performance.pojo.vo;

import lombok.Data;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description
 * @date 2023/12/21 18:03:30
 */
@Data
public class SimpleContainerVO {
    /**
     * 容器名
     */
    private String name;
    /**
     * 容器版本
     */
    private String version;
    /**
     * 容器全局唯一id
     */
    private Long code;
}
