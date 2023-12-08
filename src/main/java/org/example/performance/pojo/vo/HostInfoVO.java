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
    private DiskInfoVO diskInfoVO;

    @Override
    @JsonIgnore
    public Integer getId() {
        return super.getId();
    }

    @Override
    @JsonIgnore
    public List<String> getContainerIdList() {
        return super.getContainerIdList();
    }
}
