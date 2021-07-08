package control;

import core.NettyServer;
import handler.ProxyServerHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 * author nobugboy
 * description
 * create 2021-07-01 10:33
 **/
public class WebControl {

    public static boolean createProxy(Integer proxyPort,String targetName,Integer targetPort,Integer time,boolean sync){
        NettyServer nettyServer = new NettyServer();
        try{
            ProxyServerHandler proxyServerHandler = new ProxyServerHandler(targetName, targetPort + "", time, sync);

            ChannelFuture start = nettyServer.start(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new ByteArrayDecoder());
                    ch.pipeline().addLast(new ByteArrayEncoder());
                    ch.pipeline().addLast(proxyServerHandler);
                }
            }, proxyPort);
            HttpServerHandler.closeCache.put(targetName+targetPort,start);
            System.out.println("EasyProxyServer创建代理"+targetName+"目标端口: " + targetPort + "代理端口:" + proxyPort);
            return true;
        }catch (Exception e){
            System.err.println(e.getMessage());
            return false;
        }



    }
}
