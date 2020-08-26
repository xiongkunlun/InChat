package com.github.unclecatmyself;

import com.github.unclecatmyself.auto.ConfigFactory;
import com.github.unclecatmyself.auto.InitServer;
import com.github.unclecatmyself.bootstrap.channel.cache.WsCacheMap;
import com.github.unclecatmyself.users.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;

/**
 * Create by UncleCatMySelf in 22:49 2019\1\4 0004
 */
@Configuration
@SpringBootApplication
public class application {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(application.class, args);
        ConfigFactory.initNetty = new MyInit();
        ConfigFactory.inChatVerifyService = new VerifyServiceImpl();
        ConfigFactory.inChatToDataBaseService = new DataBaseServiceImpl();
        ConfigFactory.fromServerService = FromServerServiceImpl.TYPE2;
        ConfigFactory.textData = new UserTextData();
//        ConfigFactory.RedisIP = "192.168.0.101";
//        ConfigFactory.RedisIP = "192.168.192.132";

        InitServer.open();
    }


    @Bean
    WsCacheMap getWsCacheMap() {
        return new WsCacheMap();
    }

    @Bean
    public RedisTemplate<String, Serializable> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }

}
