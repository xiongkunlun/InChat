package com.github.unclecatmyself.bootstrap.data;

import com.github.unclecatmyself.common.bean.InChatMessage;

/**
 * Created by MySelf on 2018/12/3.
 */
public interface InChatToDataBaseService {

    Boolean writeMessage(InChatMessage message);

}
