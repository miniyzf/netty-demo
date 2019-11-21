package com.example.netty.handler;

import com.example.netty.common.Common;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义 handler 处理类
 * 客户端向服务端发送数据，先把数据放在缓冲区，服务端再从缓冲区读取。
 */
public class MyWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    
    Logger logger = LoggerFactory.getLogger(MyWebSocketHandler.class);
    
    /**
     * 保留所有与服务器建立连接的channel对象
     */
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private static ConcurrentHashMap<String,Channel> clientUC = new ConcurrentHashMap<String, Channel>();
    private static ConcurrentHashMap<Channel,String> clientCU = new ConcurrentHashMap<Channel,String>();

    private WebSocketServerHandshaker handshaker;

    private int i=0;

    /**
     *
     *  服务端收到新的客户端连接，将客户端的 Channel 存入 ChannelGroup 列表，并通知列表中的其他客户端 Channel
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        for (Channel channel : channelGroup) {
            channel.writeAndFlush("[SERVER] - " + ctx.channel().remoteAddress() + " 加入\n");
        }
//        logger.info("Client:" + ctx.channel().remoteAddress() + "加入");
        //  存入 channelGroup 通道组中
        channelGroup.add(ctx.channel());

        // 系统会自动给每一个 channel 分配一串很长的字符串作为唯一的ID（使用 asLongText() 获取），使用 asShortText() 会把该ID进行精简（精简过后可能会有重复）
//        logger.info("channel的长ID："+ctx.channel().id().asLongText());
//        logger.info("channel的短ID："+ctx.channel().id().asShortText());
    }

    /**
     * 服务端监听到客户端活动
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
//        logger.info("Client:" + ctx.channel().remoteAddress() + "上线");
    }

    /**
     * 客户端与服务端断开连接的时候调用
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
//        logger.info("Client:" + ctx.channel().remoteAddress() + "下线");
    }

    /**
     *
     * 客户端断开时，将客户端的 Channel 移除 ChannelGroup 列表，并通知列表中的其他客户端 Channel
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        for (Channel channel : channelGroup) {
            channel.writeAndFlush("[SERVER] - " + ctx.channel().remoteAddress() + " 离开\n");
        }
//        logger.info("Client:" + ctx.channel().remoteAddress() + "离开");
        //从 channelGroup 通道组中移除
        channelGroup.remove(ctx.channel());

        clientUC.remove(clientCU.get(ctx.channel()));
        clientCU.remove(ctx.channel());
    }

    /**
     * 服务端 channel 读取数据结束
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * 当出现 Throwable 对象才会被调用，即当 Netty 由于 IO 错误或者处理器在处理事件时抛出的异常时。
     * channel发生异常，若不关闭，随着异常channel的逐渐增多，性能也就随之下降
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.info("Client:" + ctx.channel().remoteAddress() + "异常");
        logger.info("用户【" + clientCU.get(ctx.channel()) + "】异常下线");
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 服务端处理客户端webSocket请求的核心方法
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof FullHttpRequest) {       // 处理客户端向服务端发起http握手请求的业务

            FullHttpRequest request = (FullHttpRequest) msg;
            String uri = request.uri();
            Map paramMap=getUrlParams(uri);
            String uid = (String) paramMap.get("uid");
            clientUC.put(uid,ctx.channel());
            clientCU.put(ctx.channel(),uid);
            logger.info("用户【" + uid + "】: " + ctx.channel().remoteAddress() + "上线");
            handHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {     // 处理websocket连接业务
            handWebSocketFrame(ctx, (WebSocketFrame) msg);
        }

        /*//首次连接是FullHttpRequest，处理参数
        if (null != msg && msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            String uri = request.uri();

            Map paramMap=getUrlParams(uri);
            String uid = (String) paramMap.get("uid");
            clients.put(uid,ctx.channel());

            logger.info("接收到的uid是："+ uid);
            logger.info("接收到的参数是："+ JSON.toJSONString(paramMap));
            //如果url包含参数，需要处理
            if(uri.contains("?")){
                String newUri=uri.substring(0,uri.indexOf("?"));
                logger.info(newUri);
                request.setUri(newUri);
            }

        }else if(msg instanceof TextWebSocketFrame){
            //正常的TEXT消息类型
            TextWebSocketFrame frame=(TextWebSocketFrame)msg;
            logger.info("客户端收到服务器数据：" +frame.text());
            sendAllMessage(ctx,frame.text());
        }
        super.channelRead(ctx, msg);*/
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {

    }


    /**
     * 处理客户端与服务端之前的websocket业务
     *
     * @param ctx
     * @param frame
     */
    private void handWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否是关闭webSocket的指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }

        // 判断是否ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        // 每当从服务端读到客户端写入信息时，将信息写入缓冲区，写完后刷到对应客户端
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame msg = (TextWebSocketFrame) frame;

            JSONObject message = null;
            try {
                message = JSONObject.fromObject(msg.text());
            } catch (Exception e) {
                message = JSONObject.fromObject("{\"uid\":\""+clientCU.get(ctx.channel())+"\",\"msg\":\"消息接收异常\",\"type\":1}");
                //e.printStackTrace();
            }
            /*
            ++i;
            logger.info((i)+"msg--" + message.toString());
            for(String id : clientUC.keySet()){
                if(id.equals(message.getString("uid"))){
                    ctx.channel().writeAndFlush(new TextWebSocketFrame("["+i+"自己]" + message.get("msg").toString()));
                }else{
                    clientUC.get(id).writeAndFlush(new TextWebSocketFrame(message.getInt("uid")+" : "+message.get("msg").toString()));
                }
            }*/


            for(String id : clientUC.keySet()){
                if(id.equals(message.getString("uid"))){
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(message.toString()));
                }else{
                    clientUC.get(id).writeAndFlush(new TextWebSocketFrame(message.toString()));
                }
            }



            /*Channel cur_channel = ctx.channel();
            for (Channel channel : channelGroup) {
                if (channel != cur_channel) {
                    channel.writeAndFlush(new TextWebSocketFrame("[" + cur_channel.remoteAddress() + "]" + msg.text()));
                } else {
                    channel.writeAndFlush(new TextWebSocketFrame("[自己]" + msg.text()));
                }
            }*/

            // 群发消息
//            channelGroup.writeAndFlush(msg.text());
        }

    }

    /**
     * 处理客户端向服务端发起http握手请求的业务
     *
     * @param ctx
     * @param req
     */
    @SuppressWarnings("deprecation")
    private void handHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // 如果HTTP解码失败，返回HTTP异常
        if (!req.getDecoderResult().isSuccess() || !("websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        // 正常WebSocket的Http连接请求，构造握手响应返回
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(Common.WEB_SOCKET_URL, null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {// 无法处理的websocket版本
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else {// 向客户端发送websocket握手,完成握手
            handshaker.handshake(ctx.channel(), req);
        }
    }

    /**
     * 服务端向客户端响应消息
     *
     * @param ctx
     * @param req
     * @param res
     */
    @SuppressWarnings("deprecation")
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();

            HttpHeaders.setContentLength(res, res.content().readableBytes());
        }
        // 如果是非Keep-Alive，关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }
















    private void sendAllMessage(ChannelHandlerContext ctx,String message){
        //发送给指定的人
        Channel channel = ctx.channel();
        channelGroup.forEach(ch -> {
            if(channel !=ch){
                ch.writeAndFlush(new TextWebSocketFrame(message));
            }else{
                ch.writeAndFlush(" 【自己】"+message +" \n");
            }
        });
        //收到信息后，群发给所有channel
        //channelGroup.writeAndFlush( new TextWebSocketFrame(message));
    }

    private static Map getUrlParams(String url){
        Map<String,String> map = new HashMap<>();
        url = url.replace("?",";");
        if (!url.contains(";")){
            return map;
        }
        if (url.split(";").length > 0){
            String[] arr = url.split(";")[1].split("&");
            for (String s : arr){
                String key = s.split("=")[0];
                String value = s.split("=")[1];
                map.put(key,value);
            }
            return  map;

        }else{
            return map;
        }
    }
}
