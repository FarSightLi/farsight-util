package org.example.performance.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.performance.component.exception.BusinessException;
import org.example.performance.component.exception.CodeMsg;
import org.example.performance.pojo.po.ContainerInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 容器基本信息
 * @date 2023/12/5 16:42:12
 */
@Data
@Slf4j
public class ContainerInfoVO extends ContainerInfo {
    public enum Field {
        MEM,
        CPU,
        DISK
    }
    private BigDecimal maxCpuRate;
    private BigDecimal maxCpuUsage;
    private BigDecimal maxDiskUsage;
    private BigDecimal maxDiskUsedRate;
    private BigDecimal maxMemUsage;
    private BigDecimal maxMemUsedRate;
    private BigDecimal avgCpuUsage;
    private BigDecimal avgCpuRate;
    private BigDecimal avgDiskUsage;
    private BigDecimal avgDiskRate;
    private BigDecimal avgMemUsage;
    private BigDecimal avgMemRate;
    private Integer maxDiskUsedState;
    private Integer maxCpuState;
    private Integer maxMemUsedState;
    private Integer avgMemUsedState;
    private Integer avgCpuState;
    private Integer avgDiskUsedState;
    private Long onlineTime;

    /**
     * CPU使用率
     */
    private BigDecimal cpuRate;

    /**
     * 重启时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime restartTime;

    /**
     * 容器状态
     */
    private String state;

    /**
     * 使用内存大小
     */
    private BigDecimal memUsedSize;

    /**
     * 使用磁盘大小
     */
    private BigDecimal diskUsedSize;

    public BigDecimal getMaxRate(Field field) {
        BigDecimal result;
        if (Field.MEM.equals(field)) {
            result = this.maxMemUsedRate;
        } else if (Field.CPU.equals(field)) {
            result = this.maxCpuRate;
        } else if (Field.DISK.equals(field)) {
            result = this.maxDiskUsedRate;
        } else {
            log.error("不支持的字段：{}", field);
            throw new BusinessException(CodeMsg.PARAMETER_ERROR);
        }
        return result;
    }

    public BigDecimal getAvgRate(Field field) {
        BigDecimal result;
        if (Field.MEM.equals(field)) {
            result = this.avgMemRate;
        } else if (Field.CPU.equals(field)) {
            result = this.avgCpuRate;
        } else if (Field.DISK.equals(field)) {
            result = this.avgDiskRate;
        } else {
            log.error("不支持的字段：{}", field);
            throw new BusinessException(CodeMsg.PARAMETER_ERROR);
        }
        return result;
    }

    public void setAvgState(Field field, Integer state) {
        if (Field.MEM.equals(field)) {
            this.avgMemUsedState = state;
        } else if (Field.CPU.equals(field)) {
            this.avgCpuState = state;
        } else if (Field.DISK.equals(field)) {
            this.avgDiskUsedState = state;
        } else {
            log.error("不支持的字段：{}", field);
            throw new BusinessException(CodeMsg.PARAMETER_ERROR);
        }
    }

    public void setMaxState(Field field, Integer state) {
        if (Field.MEM.equals(field)) {
            this.maxMemUsedState = state;
        } else if (Field.CPU.equals(field)) {
            this.maxCpuState = state;
        } else if (Field.DISK.equals(field)) {
            this.maxDiskUsedState = state;
        } else {
            log.error("不支持的字段：{}", field);
            throw new BusinessException(CodeMsg.PARAMETER_ERROR);
        }
    }


    @Override
    @JsonIgnore
    public Integer getId() {
        return super.getId();
    }

    @Override
    @JsonIgnore
    public String getContainerId() {
        return super.getContainerId();
    }

    @Override
    @JsonIgnore
    public Integer getHostId() {
        return super.getHostId();
    }

    @Override
    @JsonIgnore
    public String getHostIp() {
        return super.getHostIp();
    }

    @Override
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime getUpdateTime() {
        return super.getUpdateTime();
    }

    @Override
    @JsonIgnore
    public void setId(Integer id) {
        super.setId(id);
    }

    @Override
    public String getContainerName() {
        return super.getContainerName();
    }

    @Override
    public BigDecimal getImageSize() {
        return super.getImageSize();
    }

    @Override
    public BigDecimal getCpus() {
        return super.getCpus();
    }

    @Override
    public LocalDateTime getCreateTime() {
        return super.getCreateTime();
    }

    @Override
    public String getVersion() {
        return super.getVersion();
    }

    @Override
    public BigDecimal getMemSize() {
        return super.getMemSize();
    }

    @Override
    public BigDecimal getDiskSize() {
        return super.getDiskSize();
    }

    @Override
    public void setContainerName(String containerName) {
        super.setContainerName(containerName);
    }

    @Override
    public void setContainerId(String containerId) {
        super.setContainerId(containerId);
    }

    @Override
    public void setHostId(Integer hostId) {
        super.setHostId(hostId);
    }

    @Override
    public void setHostIp(String hostIp) {
        super.setHostIp(hostIp);
    }

    @Override
    public void setImageSize(BigDecimal imageSize) {
        super.setImageSize(imageSize);
    }

    @Override
    public void setCpus(BigDecimal cpus) {
        super.setCpus(cpus);
    }

    @Override
    public void setCreateTime(LocalDateTime createTime) {
        super.setCreateTime(createTime);
    }

    @Override
    public void setVersion(String version) {
        super.setVersion(version);
    }

    @Override
    public void setMemSize(BigDecimal memSize) {
        super.setMemSize(memSize);
    }

    @Override
    public void setDiskSize(BigDecimal diskSize) {
        super.setDiskSize(diskSize);
    }
}
