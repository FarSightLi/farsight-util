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
 * @description 查询容器的参数DTO
 * @date 2023/12/21 15:59:33
 */
@Data
public class ContainerInfoDTO {
    /**
     * ip
     */
    @NotNull
    @NotBlank
    private Long id;

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
