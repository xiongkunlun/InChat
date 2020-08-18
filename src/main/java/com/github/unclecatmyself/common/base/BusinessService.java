package com.github.unclecatmyself.common.base;

public interface BusinessService {

    /**
     * 保存消息
     *
     * @param msg 用户id#消息内容#时间戳
     * @return 消息的key
     */
    String saveMsg(String msg);

    Long saveTeacherMsg(String msgId);

}
