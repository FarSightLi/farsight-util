package org.example.performance.demo;

import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.Session;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 不启动spingboot的测试
 * @date 2023/12/22 17:16:03
 */
public class NoSpringTest {
    @Test
    public void test1() {
        Session farsight = JschUtil.getSession("192.168.1.167", 22, "farsight", "123456");
        String exec = JschUtil.exec(farsight, "cat /etc/SuSE-release", StandardCharsets.UTF_8).trim();
        System.out.println(farsight.getHost());
        if (exec.isEmpty()) {
            System.out.println("命令执行失败");
        }
        System.out.println(exec);
    }
}
