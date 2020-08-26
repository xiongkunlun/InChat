package com.github.unclecatmyself.bootstrap.channel;

import com.alibaba.fastjson.JSONObject;
import com.github.unclecatmyself.bootstrap.backmsg.InChatBackMapService;
import com.github.unclecatmyself.bootstrap.channel.http.HttpChannelService;
import com.github.unclecatmyself.bootstrap.channel.ws.WsChannelService;
import com.github.unclecatmyself.bootstrap.verify.InChatVerifyService;
import com.github.unclecatmyself.common.base.HandlerService;
import com.github.unclecatmyself.common.bean.SendInChat;
import com.github.unclecatmyself.common.bean.vo.SendServerVO;
import com.github.unclecatmyself.common.constant.Constans;
import com.github.unclecatmyself.common.constant.TableNameConstant;
import com.github.unclecatmyself.task.DataAsynchronousTask;
import com.github.unclecatmyself.task.TextData;
import com.google.gson.Gson;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.CharsetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by MySelf on 2018/11/21.
 */
@Service
public class HandlerServiceImpl extends HandlerService {

    @Autowired
    private InChatVerifyService inChatVerifyService;

    @Autowired
    private InChatBackMapService inChatBackMapService;
    @Autowired
    private HttpChannelService httpChannelService;
    @Autowired
    private WsChannelService websocketChannelService;
    @Autowired
    private DataAsynchronousTask dataAsynchronousTask;
    @Autowired
    private TextData textData;

    @Autowired
    private RedisTemplate redisTemplate;

    public SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

//    public HandlerServiceImpl(DataAsynchronousTask dataAsynchronousTask, InChatVerifyService inChatVerifyService, TextData textData) {
//        this.dataAsynchronousTask = dataAsynchronousTask;
//        this.inChatVerifyService = inChatVerifyService;
//        this.textData = textData;
//    }

    public HandlerServiceImpl() {

    }


    @Override
    public void getList(Channel channel) {
        httpChannelService.getList(channel);
    }

    @Override
    public void getSize(Channel channel) {
        httpChannelService.getSize(channel);
    }

    @Override
    public void sendFromServer(Channel channel, SendServerVO serverVO) {
        httpChannelService.sendFromServer(channel, serverVO);
    }

    @Override
    public void sendInChat(Channel channel, FullHttpMessage msg) {
        System.out.println(msg);
        String content = msg.content().toString(CharsetUtil.UTF_8);
        Gson gson = new Gson();
        SendInChat sendInChat = gson.fromJson(content, SendInChat.class);
        httpChannelService.sendByInChat(channel, sendInChat);
    }

    @Override
    public void notFindUri(Channel channel) {
        httpChannelService.notFindUri(channel);
    }

    @Override
    public boolean login(Channel channel, JSONObject maps) {
        //校验规则，自定义校验规则
        return check(channel, maps);
    }

    @Override
    public void sendMeText(Channel channel, Map<String, Object> maps) {
        Gson gson = new Gson();
        channel.writeAndFlush(new TextWebSocketFrame(
                gson.toJson(inChatBackMapService.sendMe((String) maps.get(Constans.VALUE))))).addListener(
                new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            dataAsynchronousTask.writeData(maps);
                            textData.writeData(maps);
                        } else {
                            future.cause().printStackTrace();
                            future.channel().close();
                        }
                    }
                }
        );
//        try {
//            dataAsynchronousTask.writeData(maps);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void sendToText(Channel channel, Map<String, Object> maps) {
        Gson gson = new Gson();
        String otherOne = (String) maps.get(Constans.ONE);
        String value = (String) maps.get(Constans.VALUE);
        String token = (String) maps.get(Constans.TOKEN);
        //返回给自己
        channel.writeAndFlush(new TextWebSocketFrame(
                gson.toJson(inChatBackMapService.sendBack(otherOne, value))));
        if (websocketChannelService.hasOther(otherOne)) {
            //发送给对方--在线
            Channel other = websocketChannelService.getChannel(otherOne);
            if (other == null) {
                //转http分布式
                httpChannelService.sendInChat(otherOne, inChatBackMapService.getMsg(token, value));
            } else {
                other.writeAndFlush(new TextWebSocketFrame(
                        gson.toJson(inChatBackMapService.getMsg(token, value))));
            }
        } else {
            maps.put(Constans.ON_ONLINE, otherOne);
        }
        try {
            dataAsynchronousTask.writeData(maps);
        } catch (Exception e) {
            return;
        }
    }

    @Override
    public void sendGroupText(Channel channel, JSONObject maps) {
        String userId = channel.attr(Constans.userIdAttr).get();
        String groupId = channel.attr(Constans.groupIdAttr).get();
        Date time = maps.getDate(Constans.TIME);
        String formatTime = df.format(time);
        String value = maps.getString(Constans.VALUE);
        HashOperations<String, Object, String> hashOperations = redisTemplate.opsForHash();
        String sendUserInfo = hashOperations.get(TableNameConstant.UBASE + ":" + groupId, userId);
        //格式化消息为 userinfo#格式化时间#消息内容
        String saveValue = new StringBuilder()
                .append(userId)
                .append(Constans.BaseInfoSplitor)
                .append(formatTime)
                .append(Constans.BaseInfoSplitor)
                .append(value).toString();

        //记录消息
        ValueOperations idGen = redisTemplate.opsForValue();
        Long increment = idGen.increment(TableNameConstant.IDGEN, 1);
        hashOperations.put(TableNameConstant.MSG + ":" + groupId, increment + "", saveValue);
        boolean b = checkTeacher(userId);
        if (b) {
            //记录为通知信息
            ListOperations listOperations = redisTemplate.opsForList();
            listOperations.leftPush(TableNameConstant.TMSG + ":" + groupId, increment + "");
        }

        List<String> no_online = new ArrayList<>();
        Set<String> userIds = inChatVerifyService.getAllUserIdByGroupId(groupId);

        String sendValue = new StringBuilder()
                .append(sendUserInfo)
                .append(Constans.BaseInfoSplitor)
                .append(formatTime)
                .append(Constans.BaseInfoSplitor)
                .append(value).toString();
        //发给自己
        channel.writeAndFlush(new TextWebSocketFrame(
                JSONObject.toJSONString(inChatBackMapService.sendGroup(userId, sendValue, groupId, b))));
        //发给其他channel
        for (String otherUid : userIds) {
            if (!userId.equals(otherUid)) {
                if (websocketChannelService.hasOther(otherUid)) {
                    Channel otherChannel = websocketChannelService.getChannel(otherUid);
                    if (otherChannel == null) {
                        //如果找不到channel 那就是在其他的服务器上，所以转分布发送，单机情况下不需要
//                        httpChannelService.sendInChat(otherUid, inChatBackMapService.sendGroup(userId, value, groupId));
                    } else {
                        otherChannel.writeAndFlush(new TextWebSocketFrame(
                                JSONObject.toJSONString(inChatBackMapService.sendGroup(userId, sendValue, groupId, b))));
                    }
                } else {
                    no_online.add(otherUid);
                }
            }
        }
        maps.put(Constans.ONLINE_GROUP, no_online);
        try {
            dataAsynchronousTask.writeData(maps);
        } catch (Exception e) {
            return;
        }
    }

    private boolean checkTeacher(String userId) {
        return Integer.valueOf(userId) % 2 == 1;
    }

    @Override
    public void verify(Channel channel) {
        Attribute<String> userAttr = channel.attr(Constans.userIdAttr);
        String userId = userAttr.get();
        if (userId != null) {
            return;
        } else {
            channel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(inChatBackMapService.loginError())));
            close(channel);
        }
    }

    @Override
    public void sendPhotoToMe(Channel channel, Map<String, Object> maps) {
        Gson gson = new Gson();
        System.out.println(maps.get(Constans.VALUE));
        channel.writeAndFlush(new TextWebSocketFrame(
                gson.toJson(inChatBackMapService.sendMe((String) maps.get(Constans.VALUE)))));
        try {
            dataAsynchronousTask.writeData(maps);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hisNotify(Channel channel, JSONObject maps) {
        String groupId = maps.getString("groupId");
        String tableName = TableNameConstant.TMSG + ":" + groupId;
        ListOperations listOperations = redisTemplate.opsForList();
        Long size = listOperations.size(tableName);
        //默认只展示最近的100条
        Long offset = size - 100 < 0 ? 0L : size - 100;

        List<String> hisIds = listOperations.range(tableName, offset, size);
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        List<String> ls = new ArrayList<>(hisIds.size());
        for (String hisId : hisIds) {
            String msg = hashOperations.get(TableNameConstant.MSG + ":" + groupId, hisId);
            String[] split = msg.split("#");
            String senderId = split[0];
            String senderBaseInfo = hashOperations.get(TableNameConstant.UBASE + ":" + groupId, senderId);
            ls.add(msg.replace(senderId, senderBaseInfo));
        }
        JSONObject obj = new JSONObject();
        obj.put("type", "hisNotify");
        obj.put("value", ls);
        channel.writeAndFlush(new TextWebSocketFrame(obj.toJSONString()));
    }

    private Boolean check(Channel channel, JSONObject maps) {
        String userId = maps.getString(Constans.USER_ID);
        if (inChatVerifyService.verifyGroup(maps)) {
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            String userInfo = maps.getString(Constans.BASEINFO);
            String groupId = userInfo.substring(0, userInfo.indexOf(Constans.BaseInfoSplitor));
            //保存用户 信息
            hashOperations.put(TableNameConstant.UBASE + ":" + groupId, userId + "", userInfo);
            //channel 保存token
            channel.attr(Constans.userIdAttr).set(userId);
            channel.attr(Constans.groupIdAttr).set(groupId);
            channel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(inChatBackMapService.loginSuccess())));
            websocketChannelService.loginWsSuccess(channel, userId);
            return true;
        }
        channel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(inChatBackMapService.loginError())));
        close(channel);
        return false;
    }

    private String genToken(String userId) {
        return UUID.randomUUID().toString();
    }

    private String getUserIdByToken(String token) {
        HashOperations<String, String, String> valueOperations = redisTemplate.opsForHash();
        return valueOperations.get("tu", token);
    }

    @Override
    public void close(Channel channel) {
        websocketChannelService.close(channel);
    }
}
