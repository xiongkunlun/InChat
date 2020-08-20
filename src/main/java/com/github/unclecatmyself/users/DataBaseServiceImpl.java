package com.github.unclecatmyself.users;

import com.github.unclecatmyself.bootstrap.data.InChatToDataBaseService;
import com.github.unclecatmyself.common.bean.InChatMessage;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Created by MySelf on 2019/1/3.
 */
@Service
public class DataBaseServiceImpl implements InChatToDataBaseService {
    //获取消息
    public Boolean writeMessage(InChatMessage inChatMessage) {
        System.out.println(inChatMessage.toString());
        return true;
    }
}
