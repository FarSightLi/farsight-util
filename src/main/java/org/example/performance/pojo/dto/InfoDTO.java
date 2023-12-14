package org.example.performance.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    /**
     * ip
     */
    @NotNull
    @NotBlank
    private String ip;

    /**
     * 开始时间
     */
    @NotNull
    @NotBlank
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @NotNull
    @NotBlank
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
