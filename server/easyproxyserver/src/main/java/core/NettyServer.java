package core;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import utils.CommonUtils;
import utils.TypeEnum;
/**
 * Author yujian
 * Description 内网穿透服务端
 * Date 2021/2/2
 */ 
public class NettyServer {
    public  ChannelFuture start(ChannelHandler initializer,int port){
        TypeEnum typeEnum = CommonUtils.useNio();
        EventLoopGroup bossGroup ;
        EventLoopGroup workerGroup;
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
            if (typeEnum == TypeEnum.EPOLL) {
                bossGroup = new EpollEventLoopGroup(CommonUtils.nThread(),new ServerThreadFactory("ePollBoss"));
                workerGroup = new EpollEventLoopGroup(CommonUtils.nThread(), new ServerThreadFactory("ePollWork"));
                serverBootstrap.group(bossGroup, workerGroup).channel(EpollServerSocketChannel.class);;
            } else if(typeEnum == TypeEnum.KQUEUE){
                bossGroup = new KQueueEventLoopGroup(CommonUtils.nThread(), new ServerThreadFactory("kQueueBoss"));
                workerGroup = new KQueueEventLoopGroup(CommonUtils.nThread(), new ServerThreadFactory("kQueueWork"));
                serverBootstrap.group(bossGroup, workerGroup).channel(KQueueServerSocketChannel.class);
            } else {
                bossGroup = new NioEventLoopGroup(CommonUtils.nThread(), new ServerThreadFactory("nioBoss"));
                workerGroup = new NioEventLoopGroup(CommonUtils.nThread(), new ServerThreadFactory("nioWork"));
                serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
            }
            serverBootstrap.childHandler(initializer);
            ChannelFuture f = serverBootstrap.bind(port).sync();
            f.channel().closeFuture().addListener((ChannelFutureListener) future -> {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            });
            return f;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
