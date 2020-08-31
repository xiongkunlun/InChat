package com.github.unclecatmyself.common.constant;

/**
 * redis 表明常量
 */
public class TableNameConstant {

    //token用户表 hash （token，user）
    public static final String TU = "tu";

    //用户基础信息表 hash:{groupId}(userId : baseInfo)
    public static final String UBASE = "ubase";

    //消息表 hash :{groupId}（自增id:senderID#msg）
    public static final String MSG = "msg";

    //教师消息表 list:{groupId} (消息id)
    public static final String TMSG = "tmsg";

    public static final String IDGEN = "idGen";
}
