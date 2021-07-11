package handler;

import com.alibaba.fastjson.JSONObject;
import core.GoClientTable;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import utils.Message;
import utils.Status;
import utils.TcpQueue;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Author yujian
 * Description proxy处理器，只处理某一个proxy端口的数据
 * Date 2021/2/2
 */
@ChannelHandler.Sharable
public class ProxyServerHandler extends ChannelInboundHandlerAdapter {
    public final static Map<String,Channel> channels = new ConcurrentHashMap<>();
    private final                       String       port;
    private final Integer time;
    private final boolean sync;
    private final String targetName;
    public ProxyServerHandler(String targetName,String port,Integer time,boolean sync){
        this.port = port;
        this.time = time;
        this.sync = sync;
        this.targetName = targetName;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if(!sync){
            channels.put(targetName+port,ctx.channel());
            if(ServerHandler.cache.containsKey(targetName+port)){
                Set<byte[]> bytes = ServerHandler.cache.get(targetName + port);
                if(bytes!=null && bytes.size() > 0){
                    for (byte[] aByte : bytes) {
                        ctx.channel().writeAndFlush(aByte);
                    }
                }
            }else if("22".equals(port)){
                System.out.println(String.format("%s与目标主机的SSH隧道未建立，请稍微重试...",targetName));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        System.err.println("server porxy hander" +cause.getMessage());
        if(ctx.channel().isActive()){
            ctx.channel().close();
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                ctx.close();
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object data) throws Exception {
        byte[] msg = (byte[]) data;
        GoClientTable goClientTable = ServerHandler.clientChannel.get(targetName+port);
        if(goClientTable == null){
            System.out.println("目标主机"+goClientTable + "不存在");
            return;
        }
        goClientTable.setWriteBytes(goClientTable.getWriteBytes() + msg.length);
        Channel channel    = goClientTable.getConn();
        if (channel != null) {
            Message message = new Message();
            message.setType(Status.data);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("port", port);
            message.setLength(jsonObject.toJSONString().getBytes(CharsetUtil.UTF_8).length);
            message.setInfo(jsonObject);
            message.setData(msg);
            channel.writeAndFlush(message);
            if(this.sync){
                Object take = TcpQueue.getQueue(targetName+port).poll(checkTime(time),TimeUnit.SECONDS);
                if(take == null){
                    String response = time + " seconds timeout or empty response";
                    ctx.channel().writeAndFlush(response.getBytes());
                    ctx.close();
                }else{
                    ctx.channel().writeAndFlush(take);
                    ctx.close();
                }
            }
        }
    }
    public int checkTime(Integer time){
        if(time == null){
            return 3;
        }
        if(time <= 3){
            return 3;
        }
        if(time > 30){
            return 30;
        }
        return 3;
    }

    /**
     * 开启sync 如果是http请求则返回http response格式
     * @param msg 内容
     * @return byte[]
     */
    public byte[] response(String msg){
        StringBuilder sb =new StringBuilder();
        sb.append("HTTP/1.1 200\n" + "Content-Type: text/html;charset=UTF-8\n" + "Content-Length: ").append(
            msg.length()).append("\n").append("Date: Tue, 02 Feb 2021 08:16:14 GMT\n").append(
            "Keep-Alive: timeout=60\n").append("Connection: keep-alive\n" + "\n").append(msg);
        return sb.toString().getBytes();
    }
}

