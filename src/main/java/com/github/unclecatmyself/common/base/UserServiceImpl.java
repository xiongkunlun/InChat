package com.github.unclecatmyself.common.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

public class UserServiceImpl implements UserService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public String getById(String userId) {
        HashOperations<String, String, String> userHash = redisTemplate.opsForHash();
        String userInfo = userHash.get("userInfo", userId);
        return userInfo;
    }

    @Override
    public void saveUser(String userId, String userInfo) {
        HashOperations<String, String, String> userHash = redisTemplate.opsForHash();
        userHash.put("userInfo", userId, userInfo);
    }
}
