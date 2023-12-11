package org.example.performance.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 查询容器或主机的参数对象
 * @date 2023/12/11 16:27:43
 */
@Data
public class InfoDTO {
    private String ip;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
