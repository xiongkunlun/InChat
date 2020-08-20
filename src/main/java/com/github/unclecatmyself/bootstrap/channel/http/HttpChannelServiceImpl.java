package com.github.unclecatmyself.bootstrap.channel.http;

import com.alibaba.fastjson.JSONObject;
import com.github.unclecatmyself.auto.ConfigFactory;
import com.github.unclecatmyself.bootstrap.channel.cache.WsCacheMap;
import com.github.unclecatmyself.common.bean.SendInChat;
import com.github.unclecatmyself.common.bean.vo.*;
import com.github.unclecatmyself.common.constant.HttpConstant;
import com.github.unclecatmyself.common.constant.LogConstant;
import com.github.unclecatmyself.common.constant.NotInChatConstant;
import com.github.unclecatmyself.common.utils.RedisUtil;
import com.github.unclecatmyself.users.FromServerServiceImpl;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * Create by UncleCatMySelf in 11:41 2018\12\31 0031
 */
@Service
public class HttpChannelServiceImpl implements HttpChannelService {

    private static final Logger log = LoggerFactory.getLogger(HttpChannelServiceImpl.class);

    private static FromServerService fromServerService = FromServerServiceImpl.TYPE2;

    @Autowired
    private WsCacheMap wsCacheMap;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void getSize(Channel channel) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpConstant.CONTENT_TYPE, HttpConstant.APPLICATION_JSON);
        GetSizeVO getSizeVO = new GetSizeVO(WsCacheMap.getSize(), new Date());
        ResultVO<GetSizeVO> resultVO = new ResultVO<>(HttpResponseStatus.OK.code(), getSizeVO);
        Gson gson = new Gson();
        ByteBuf buf = Unpooled.copiedBuffer(gson.toJson(resultVO), CharsetUtil.UTF_8);
        response.content().writeBytes(buf);
        channel.writeAndFlush(response);
        close(channel);
    }

    @Override
    public void sendFromServer(Channel channel, SendServerVO serverVO) {
        if (serverVO.getToken() == ""){
            notFindUri(channel);
        }
        Channel userChannel = WsCacheMap.getByUserId(serverVO.getToken());
        if (userChannel == null){
            log.info(LogConstant.HTTPCHANNELSERVICEIMPL_NOTFINDLOGIN);
            notFindToken(channel);
        }
        String value = fromServerService.findByCode(Integer.parseInt(serverVO.getValue()));
        SendServer sendServer = new SendServer(value);
        try {
            userChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(sendServer)));
            sendServer(channel, NotInChatConstant.SEND_SUCCESS);
        }catch (Exception e){
            log.info(LogConstant.HTTPCHANNELSERVICEIMPL_SEND_EXCEPTION);
        }
    }

    private void sendServer(Channel channel,String msg){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
        response.headers().set(HttpConstant.CONTENT_TYPE,HttpConstant.APPLICATION_JSON);
        NotFindUriVO notFindUriVO = new NotFindUriVO(msg);
        ResultVO<NotFindUriVO> resultVO = new ResultVO<>(HttpResponseStatus.BAD_REQUEST.code(),notFindUriVO);
        Gson gson = new Gson();
        ByteBuf buf = Unpooled.copiedBuffer(gson.toJson(resultVO), CharsetUtil.UTF_8);
        response.content().writeBytes(buf);
        channel.writeAndFlush(response);
        close(channel);
    }

    private void notFindToken(Channel channel) {
        sendServer(channel,NotInChatConstant.NOT_FIND_LOGIN);
    }

    @Override
    public void notFindUri(Channel channel) {
        sendServer(channel,NotInChatConstant.NOT_FIND_URI);
    }

    @Override
    public void close(Channel channel) {
        log.info(LogConstant.HTTPCHANNELSERVICEIMPL_CLOSE);
        channel.close();
    }

    @Override
    public void getList(Channel channel) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpConstant.CONTENT_TYPE,HttpConstant.APPLICATION_JSON);
        GetListVO getListVO = new GetListVO(wsCacheMap.getTokenList());
        ResultVO<GetListVO> resultVO = new ResultVO<>(HttpResponseStatus.OK.code(), getListVO);
        Gson gson = new Gson();
        ByteBuf buf = Unpooled.copiedBuffer(gson.toJson(resultVO), CharsetUtil.UTF_8);
        response.content().writeBytes(buf);
        channel.writeAndFlush(response);
        close(channel);
    }

    @Override
    public void sendInChat(String token, Map msg) {
        String address = RedisUtil.getAddress(RedisUtil.convertMD5(wsCacheMap.getByJedis(token)));
        String[] str = address.split(":");
        try {
            HttpClient.getInstance().send(str[0],Integer.parseInt(str[1]),token,msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendByInChat(Channel channel, SendInChat sendInChat) {
        Gson gson = new Gson();
        Channel other = WsCacheMap.getByUserId(sendInChat.getToken());
        try {
            other.writeAndFlush(new TextWebSocketFrame(gson.toJson(sendInChat.getFrame())));
        }catch (NullPointerException e){
            e.printStackTrace();
        }
//        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
//        response.headers().set(HttpConstant.CONTENT_TYPE,HttpConstant.APPLICATION_JSON);
//        channel.writeAndFlush(response);
        close(channel);
    }
}
