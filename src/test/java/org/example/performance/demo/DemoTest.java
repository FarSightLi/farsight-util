package org.example.performance.demo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description 随意测试
 * @date 2023/12/8 09:17:35
 */
public class DemoTest {
    @Test
    public void test1() {
        BigDecimal bigDecimal = BigDecimal.valueOf(0.00);
        bigDecimal.add(BigDecimal.valueOf(1.74));
        System.out.println(bigDecimal);
        System.out.println(bigDecimal.add(BigDecimal.valueOf(1.74)));
    }
}
