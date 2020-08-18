package com.github.unclecatmyself.common.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

public class BusinessServiceImpl implements BusinessService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public String saveMsg(String msg) {
        ValueOperations strId = redisTemplate.opsForValue();
        String id = String.valueOf(strId.increment("id", 1));
        HashOperations<String, String, String> msgHash = redisTemplate.opsForHash();
        msgHash.put("msg", id, msg);
        return id;
    }

    @Override
    public Long saveTeacherMsg(String msgId) {
        ListOperations teacherList = redisTemplate.opsForList();
        return teacherList.leftPush("teaMsg", msgId);
    }
}
