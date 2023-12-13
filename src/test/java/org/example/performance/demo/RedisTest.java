package org.example.performance.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description RedisTest
 * @date 2023/12/13 14:56:28
 */
@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void get() {
        String test = redisTemplate.opsForValue().get("1");
        System.out.println(test);
    }

    @Test
    void put() {
        redisTemplate.opsForValue().set("test", "demo21");
    }
}
