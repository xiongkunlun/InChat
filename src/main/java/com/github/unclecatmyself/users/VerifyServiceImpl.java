package com.github.unclecatmyself.users;

import ch.qos.logback.classic.db.names.TableName;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.unclecatmyself.bootstrap.verify.InChatVerifyService;
import com.github.unclecatmyself.common.constant.TableNameConstant;
import com.sun.corba.se.impl.oa.toa.TOA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Created by MySelf on 2019/1/3.
 */
@Service
public class VerifyServiceImpl implements InChatVerifyService {

    @Autowired
    RedisTemplate redisTemplate;

    public Boolean verifyToken(String token) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        return hashOperations.hasKey(TableNameConstant.TU, token);
    }


    /**
     * 从比赛用户表里拿到所有用户
     *
     * @param groupId
     * @return 所有用户的userid
     */
    public Set<String> getAllUserIdByGroupId(String groupId) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        return hashOperations.keys(TableNameConstant.GROUP + ":" + groupId);
    }

    /**
     * 判断学生id和比赛是否匹配
     *
     * @param maps 用户信息
     * @return 是否属于该比赛
     */
    @Override
    public boolean verifyGroup(JSONObject maps) {
        return true;
    }
}
