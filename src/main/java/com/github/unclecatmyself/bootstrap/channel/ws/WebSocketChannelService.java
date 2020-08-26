package com.github.unclecatmyself.bootstrap.channel.ws;

import com.github.unclecatmyself.bootstrap.channel.cache.WsCacheMap;
import com.google.gson.Gson;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by MySelf on 2018/11/26.
 */
@Service
public class WebSocketChannelService implements WsChannelService {


    @Autowired
    WsCacheMap wsCacheMap;

    @Override
    public void loginWsSuccess(Channel channel, String userId) {
        wsCacheMap.saveWs(userId, channel);
        WsCacheMap.saveAd(channel.remoteAddress().toString(), userId);
    }

    @Override
    public boolean hasOther(String otherOne) {
        return wsCacheMap.hasToken(otherOne);
    }

    @Override
    public Channel getChannel(String otherUserId) {
        return WsCacheMap.getByUserId(otherUserId);
    }

    @Override
    public void close(Channel channel) {
        String token = WsCacheMap.getByAddress(channel.remoteAddress().toString());
        WsCacheMap.deleteAd(channel.remoteAddress().toString());
        wsCacheMap.deleteWs(token);
        channel.close();
    }

    @Override
    public boolean sendFromServer(Channel channel, Map<String, String> map) {
        Gson gson = new Gson();
        try {
            channel.writeAndFlush(new TextWebSocketFrame(gson.toJson(map)));
            return true;
        }catch (Exception e){
            return false;
        }
    }


}
