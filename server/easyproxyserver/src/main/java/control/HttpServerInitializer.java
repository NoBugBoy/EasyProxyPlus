package control;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author nobugboy
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
    private final String auth;
    public HttpServerInitializer(String auth){
        this.auth = auth;
    }
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ch.pipeline().addLast(new CorsHandler(CorsConfigBuilder.forAnyOrigin().build()));
        ch.pipeline().addLast(new HttpServerCodec());
        ch.pipeline().addLast("http-aggregator",
            new HttpObjectAggregator(65536));
        ch.pipeline().addLast(new ChunkedWriteHandler());
        ch.pipeline().addLast(new HttpServerHandler(auth));



    }
}