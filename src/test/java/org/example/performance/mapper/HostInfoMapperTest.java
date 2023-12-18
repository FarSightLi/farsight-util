package org.example.performance.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.performance.pojo.po.HostInfo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 主机信息mapper测试
 * @date 2023/12/7 10:09:44
 */
@SpringBootTest
public class HostInfoMapperTest {
    @Resource
    private HostInfoMapper hostInfoMapper;

    @Test
    public void testUpdateOrInsert() {
        HostInfo hostInfo1 = new HostInfo();
        hostInfo1.setIp("1.1.1.1");
        hostInfo1.setCpuCores(4);
        HostInfo hostInfo2 = new HostInfo();
        hostInfo2.setIp("3.3.3.3");
        hostInfo2.setCpuCores(6);
        List<HostInfo> hostInfoList = new ArrayList<>();
        hostInfoList.add(hostInfo1);
        hostInfoList.add(hostInfo2);
        hostInfoMapper.updateOrInsertBatch(hostInfoList);
    }

    @Test
    public void test() {
        List<String> ipList = new ArrayList<>();
        ipList.add("192.168.1.167");
        ipList.add("1.1.1.2");
        List<HostInfo> hostInfos = hostInfoMapper.selectList(new LambdaQueryWrapper<HostInfo>().in(HostInfo::getIp, ipList).select(HostInfo::getId, HostInfo::getIp));
        System.out.println(hostInfos);
        System.out.println(hostInfos.size());
        Map<String, Long> collect = hostInfos.stream().collect(Collectors.toMap(HostInfo::getIp, HostInfo::getId));
        System.out.println(collect);
    }
}
