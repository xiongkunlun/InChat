package com.github.unclecatmyself.common.base;

import io.netty.channel.Channel;

/**
 * Create by UncleCatMySelf in 2018/12/06
 */
public interface HandlerApi {

    void close(Channel channel);

}
