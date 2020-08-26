package com.github.unclecatmyself.common.base;

import com.github.unclecatmyself.common.constant.Constans;
import com.github.unclecatmyself.common.constant.LogConstant;
import com.github.unclecatmyself.common.exception.NotFindLoginChannlException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Netty实现初始层
 * Create by UncleCatMySelf in 2018/12/06
 */
public abstract class Handler extends SimpleChannelInboundHandler<Object> {

    private static final Logger log = LoggerFactory.getLogger(Handler.class);

    public HandlerApi handlerApi;

    public Handler(HandlerApi handlerApi) {
        this.handlerApi = handlerApi;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof TextWebSocketFrame) {
                System.out.println("TextWebSocketFrame" + msg);
                textdoMessage(ctx, (TextWebSocketFrame) msg);
            } else if (msg instanceof WebSocketFrame) {
                System.out.println("WebSocketFrame" + msg);
                webdoMessage(ctx, (WebSocketFrame) msg);
            } else if (msg instanceof FullHttpRequest) {
                System.out.println("FullHttpRequest" + msg);
                httpdoMessage(ctx, (FullHttpRequest) msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    protected abstract void webdoMessage(ChannelHandlerContext ctx, WebSocketFrame msg);

    protected abstract void textdoMessage(ChannelHandlerContext ctx, TextWebSocketFrame msg);

    protected abstract void httpdoMessage(ChannelHandlerContext ctx, FullHttpRequest msg);

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info(LogConstant.CHANNELINACTIVE + ctx.channel().localAddress().toString() + LogConstant.CLOSE_SUCCESS);
        try {
            Channel channel = ctx.channel();
            Attribute<String> userIdAttr = channel.attr(Constans.userIdAttr);
            if (userIdAttr != null) {
                String userId = userIdAttr.get();
                //移除id相关的记录，移除channel
                removeInactiveChannel(userId);

            }
            System.out.println("channelIdText:" + channel.toString());  //a754ebc1
            System.out.println("longstr" + channel.id().asLongText()); //60位长度 例如1c1b0dfffea99bb0-00003108-00000001-e1e893ada698884f-9dabc410

            handlerApi.close(ctx.channel());
        } catch (NotFindLoginChannlException e) {
            log.error(LogConstant.NOTFINDLOGINCHANNLEXCEPTION);
        }
    }

    protected abstract void removeInactiveChannel(String userId);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        if(evt instanceof IdleStateEvent){
//            webSocketHandlerApi.doTimeOut(ctx.channel(),(IdleStateEvent)evt);
//        }
        super.userEventTriggered(ctx, evt);
    }
}
