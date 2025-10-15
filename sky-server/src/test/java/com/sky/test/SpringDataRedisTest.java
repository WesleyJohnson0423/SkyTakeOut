package com.sky.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.entity.ddr;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class SpringDataRedisTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedisTemplate(){
        System.out.println(redisTemplate);
    }

    @Test
    public void testString(){
        redisTemplate.opsForValue().set("city","北京");
        System.out.println(redisTemplate.opsForValue().get("city"));
    }

    @Test
    public void testSaveUser() throws JsonProcessingException {
        ddr jack = new ddr("jack", 14);
        String value = mapper.writeValueAsString(jack);
        stringRedisTemplate.opsForValue().set("user:200",value);
        String json = stringRedisTemplate.opsForValue().get("user:200");
        ddr ddr = mapper.readValue(json, ddr.class);
        System.out.println(ddr);
    }
}
