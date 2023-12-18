package org.example.performance.pojo.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.example.performance.pojo.po.HostInfo;

import java.util.List;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 主机信息VO
 * @date 2023/12/6 14:17:02
 */
@Data
public class HostInfoVO extends HostInfo {
    /**
     * 磁盘具体信息
     */
    private DiskInfoVO diskInfoVO;

    @Override
    @JsonIgnore
    public Long getId() {
        return super.getId();
    }

    @Override
    @JsonIgnore
    public List<String> getContainerIdList() {
        return super.getContainerIdList();
    }
}
