package com.github.unclecatmyself.bootstrap.channel.cache;

import com.github.unclecatmyself.auto.AutoConfig;
import com.github.unclecatmyself.auto.ConfigFactory;
import com.github.unclecatmyself.common.exception.NotFindLoginChannlException;
import com.github.unclecatmyself.common.constant.NotInChatConstant;
import com.github.unclecatmyself.common.utils.RedisUtil;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket链接实例本地存储
 * Created by MySelf on 2018/11/26.
 */
@Component
public class WsCacheMap {

    /**
     * 存储用户标识与链接实例
     */
    private final static Map<String,Channel> maps = new ConcurrentHashMap<String,Channel>();

    /**
     * 存储链接地址与用户标识
     */
    private final static Map<String,String> addMaps = new ConcurrentHashMap<>();

    /**
     * Redis连接实例
     */
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 是否启动分布式
     */
//    private final static Boolean isDistributed = ConfigFactory.initNetty.getDistributed();
    private final static Boolean isDistributed = false;

    private final static String address = AutoConfig.address;

    /**
     * 存储链接
     *
     * @param userId  {@link String} 用户id
     * @param channel {@link Channel} 链接实例
     */
    public static void saveWs(String userId, Channel channel) {
        maps.put(userId, channel);
    }

    /**
     * 存储登录信息
     * @param address 登录地址
     * @param userId 用户标签
     */
    public static void saveAd(String address, String userId) {
        addMaps.put(address, userId);
    }

    /**
     * 获取链接数据
     * @param userId {@link String} 用户标识
     * @return {@link Channel} 链接实例
     */
    public static Channel getByUserId(String userId) {
        if (isDistributed) {
            if (!maps.containsKey(userId)) {
                //转分布式发送
                return null;
            }
        }
        return maps.get(userId);
    }

    /**
     * 获取对应token标签
     * @param address {@link String} 链接地址
     * @return {@link String}
     */
    public static String getByAddress(String address){
        return addMaps.get(address);
    }

    /**
     * 删除链接数据
     *
     * @param userId {@link String} 用户标识
     */
    public static void deleteWs(String userId) {
        try {
            maps.remove(userId);
        } catch (NullPointerException e) {
            throw new NotFindLoginChannlException(NotInChatConstant.Not_Login);
        }
    }

    /**
     * 删除链接地址
     * @param address
     */
    public static void deleteAd(String address){
        addMaps.remove(address);
    }

    /**
     * 获取链接数
     * @return {@link Integer} 链接数
     */
    public static Integer getSize(){
        if (isDistributed) {
            return 1;
//            return jedis.keys("*").size();
        }
        return maps.size();
    }

    /**
     * 判断是否存在链接账号
     *
     * @param token {@link String} 用户标识
     * @return {@link Boolean} 是否存在
     */
    public boolean hasToken(String token) {
        if (isDistributed) {
            ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
            return stringRedisTemplate.hasKey(token);
        }
        return maps.containsKey(token);
    }

    /**
     * 获取在线用户标签列表
     *
     * @return {@link Set} 标识列表
     */
    public Set<String> getTokenList() {
        if (isDistributed) {
            return new HashSet<>();
//            return jedis.keys("*");
        }
        Set keys = maps.keySet();
        return keys;
    }

    public String getByJedis(String token) {
        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        return stringStringValueOperations.get(token);
    }
}
