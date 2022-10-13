package com.example.dp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@SpringBootTest
class DpApplicationTests {
    @Resource
    RedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        Object o = redisTemplate.opsForValue().get("a");
        System.out.println(o);

    }

}
