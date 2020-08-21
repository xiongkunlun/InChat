package com.github.unclecatmyself.common.constant;

/**
 * redis 表明常量
 */
public class TableNameConstant {

    //token用户表 hash （token，user）
    public static final String TU = "tu";

    //当前比赛表 hash(userId - uinfp)
    public static final String GROUP = "group";

    //消息表 hash （自增id，消息内容str）
    public static final String MSG = "msg";

    //教师消息表 list (消息id)
    public static final String TMSG = "tmsg";

    public static final String IDGEN = "idGen";
}
