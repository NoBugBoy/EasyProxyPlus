package core;

import handler.ProxyServerHandler;
import utils.Status;

import handler.ServerHandler;
import io.netty.channel.Channel;
import utils.MessageBuild;

import java.util.Iterator;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * author yujian
 * description 1
 * create 2021-02-02 10:42
 **/
public class Keepalive {
    private final ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(1,new ServerThreadFactory("keepalive"));
    public void ping(){
        threadPoolExecutor.scheduleAtFixedRate(() -> {
            //清理已经断开的数据
            ServerHandler.clientChannel.values().removeIf(goClientTable -> !goClientTable.getConn().isActive());
            //发送心跳
            for (Channel channel : ServerHandler.keepaliveChannel) {
                if(channel.isOpen() && channel.isActive()){
                    channel.writeAndFlush(MessageBuild.onlyType(Status.ping));
                }else{
                    channel.close();
                }
            }
        },5,3,TimeUnit.SECONDS);
    }
}
