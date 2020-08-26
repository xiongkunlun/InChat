package com.github.unclecatmyself.bootstrap.channel.http;

import com.github.unclecatmyself.common.bean.SendInChat;
import com.github.unclecatmyself.common.bean.vo.SendServerVO;
import io.netty.channel.Channel;

import java.util.Map;

/**
 * Create by UncleCatMySelf in 11:41 2018\12\31 0031
 */
public interface HttpChannelService {

    void getSize(Channel channel);

    void sendFromServer(Channel channel,SendServerVO serverVO);

    void notFindUri(Channel channel);

    void close(Channel channel);

    void getList(Channel channel);

    void sendInChat(String token, Map msg);

    void sendByInChat(Channel channel,SendInChat sendInChat);

}
