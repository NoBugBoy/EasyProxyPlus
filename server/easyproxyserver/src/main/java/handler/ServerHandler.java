package handler;

import com.alibaba.fastjson.JSONObject;
import control.HttpServerHandler;
import core.GoClientTable;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import utils.Message;
import utils.Status;
import utils.TcpQueue;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Author yujian
 * Description 服务端处理器
 * Date 2021/2/2
 */
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<Message> {
    public static volatile Map<String, GoClientTable> clientChannel    = new ConcurrentHashMap<>();
    public static volatile Map<String, Set<byte[]>>  cache            = new ConcurrentHashMap<>();
    public static volatile Set<Channel>      keepaliveChannel = new CopyOnWriteArraySet<>();
    private final Integer              time;
    private final boolean sync;
    public ServerHandler(Integer time,boolean sync){
     this.time = time;
     this.sync = sync;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        keepaliveChannel.add(ctx.channel());

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        super.exceptionCaught(ctx, cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        int              type = msg.getType();
        if(type == Status.conn){
            //注册
            this.register(msg,ctx);
        }else if(type == Status.back){
            JSONObject info   = msg.getInfo();
            String targetName = (String)info.get("targetName");
            String port = String.valueOf(info.get("port"));
            GoClientTable goClientTable = ServerHandler.clientChannel.get(targetName+port);
            if(msg.getCheck() > msg.getLength()){
                goClientTable.setReadBytes(goClientTable.getReadBytes() + (msg.getCheck() - msg.getLength()));
            }
            if(sync){
                TcpQueue.getQueue(port).add(msg.getData());
            }else if(!ProxyServerHandler.channels.containsKey(targetName + port)){
                Set<byte[]> bytes = cache.get(targetName + port);
                if(bytes == null || bytes.size() == 0){
                    bytes = new CopyOnWriteArraySet<>();
                    bytes.add(msg.getData());
                    cache.put(targetName + port,bytes);
                }
            }else{
                Set<byte[]> bytes = cache.get(targetName + port);
                if(bytes!=null){
                    for (byte[] aByte : bytes) {
                        if(Arrays.equals(aByte, msg.getData())){
                            return;
                        }
                    }
                }
                ProxyServerHandler.channels.forEach((key, value) -> {
                    if (key.contains(targetName + port)) {
                            if(msg.getData()!=null){
                                value.writeAndFlush(msg.getData());
                            }else{
                                value.writeAndFlush("".getBytes());
                            }
                    }
                });
            }
        }else if(type == Status.pong){
            JSONObject info   = msg.getInfo();
            String targetName = (String)info.get("targetName");
            System.out.println("收到"+targetName+"心跳...");
        }
    }

    /**
     * 注册一个客户端  name + port
     * @param message tcp协议
     * @param ctx 上下文
     */
    private void register(Message message,ChannelHandlerContext ctx){
        GoClientTable gct = JSONObject.toJavaObject(message.getInfo(), GoClientTable.class);
        //注册别名 + socket
        gct.setCreateTime(System.currentTimeMillis());
        gct.setDesc(HttpServerHandler.cacheSettings.get(gct.getName()+gct.getPort()+"desc"));
        gct.setProxyPort(HttpServerHandler.cacheSettings.get(gct.getName()+gct.getPort()+"proxyPort")==null?"0":HttpServerHandler.cacheSettings.get(gct.getName()+gct.getPort()+"proxyPort"));
        if(clientChannel.get(gct.getName()+gct.getPort())!=null){
            if(clientChannel.get(gct.getName()+gct.getPort()).getConn().isActive()){
                clientChannel.get(gct.getName()+gct.getPort()).getConn().close();
            }
            System.out.printf("%s与EasyProxyServer重新连接,并注册了端口%s%n",gct.getName(),gct.getPort());
        }else{
            System.out.printf("%s向EasyProxyServer注册了端口%s%n",gct.getName(),gct.getPort());
        }
        gct.setConn(ctx.channel());
        clientChannel.put(gct.getName()+gct.getPort(),gct);

        Message sender = new Message();
        sender.setType(Status.connbak);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("port",gct.getPort());
        sender.setInfo(jsonObject);
        sender.setData(message.getData());
        sender.setLength(jsonObject.toString().getBytes(CharsetUtil.UTF_8).length);
        ctx.channel().writeAndFlush(sender);
    }

}

