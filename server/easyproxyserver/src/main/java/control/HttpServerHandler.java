package control;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import core.GoClientTable;
import handler.ServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.internal.StringUtil;
import org.apache.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Author yujian
 * Description 服务端处理器
 * Date 2021/2/2
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    public static final Map<String,String>         cacheSettings = new ConcurrentHashMap<>();
    public static final Map<String, ChannelFuture> closeCache    = new ConcurrentHashMap<>();
    private String auth;
    public HttpServerHandler(String auth){
        this.auth = auth;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        super.exceptionCaught(ctx, cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if(HttpMethod.OPTIONS.name().equals(request.method().name().toUpperCase())){
            ctx.channel().writeAndFlush(response(HttpStatus.SC_OK,""));
            return;
        }
        //todo 认证有时候不弹窗 先不管了下次更新再说
        // String authorization = request.headers().get("Authorization");
        // if(authorization==null){
        //     ctx.channel().writeAndFlush(response(HttpStatus.SC_UNAUTHORIZED,"not auth",true));
        //     return;
        // }
        // String decodedAuth = new String(Base64.getDecoder().decode(authorization.substring(6).getBytes()));
        // if(!auth.equals(decodedAuth)){
        //     ctx.channel().writeAndFlush(response(HttpStatus.SC_UNAUTHORIZED,"not auth",true));
        //     return;
        // }
        if(HttpMethod.POST.name().equals(request.method().name().toUpperCase())){
            ByteBuf content    = request.content();
            byte[]  reqContent = new byte[content.readableBytes()];
            content.readBytes(reqContent);
            String strContent = new String(reqContent, StandardCharsets.UTF_8);
            JSONObject json = JSONObject.parseObject(strContent);
            switchMethod(json,ctx.channel());
        }
    }

    /**
     * 路由
     * @param json 请求结构体
     * @param channel socket
     */
    public void switchMethod(JSONObject json, Channel channel){
        String method = (String)json.get("method");
        if(StringUtil.isNullOrEmpty(method)){
            channel.writeAndFlush(response(HttpStatus.SC_BAD_REQUEST,"can not found method"));
            return;
        }
        JSONObject params = (JSONObject)json.get("params");
        switch (method.toLowerCase()){
            case  "create" :
                params.getBoolean("sync");
                boolean ok = WebControl.createProxy(params.getInteger("proxyPort"),params.getString("targetName"),params.getInteger("targetPort"),params.getInteger("time"),
                    params.getBoolean("sync") != null && params.getBoolean("sync"));
                if(ok) {
                    String member =  params.getString("targetName")+params.getInteger("targetPort");
                    //cache init settings
                    cacheSettings.put(member + "proxyPort",String.valueOf(params.getInteger("proxyPort")));
                    cacheSettings.put(member + "desc",params.getString("desc"));
                    // init settings
                    GoClientTable goClientTable = ServerHandler.clientChannel.get(member);
                    if (goClientTable!=null ){
                        goClientTable.setProxyPort(String.valueOf(params.getInteger("proxyPort")));
                        goClientTable.setDesc(params.getString("desc"));
                    }
                    channel.writeAndFlush(response(HttpStatus.SC_OK,""));
                }else{
                    channel.writeAndFlush(response(HttpStatus.SC_INTERNAL_SERVER_ERROR,""));
                }
                break;
            case  "delete" :
                String member =  params.getString("targetName")+params.getInteger("targetPort");
                GoClientTable goClientTable = ServerHandler.clientChannel.get(member);
                if (goClientTable!=null ){
                    goClientTable.setProxyPort("0");
                    goClientTable.setDesc("");
                }
                //shutdown natPort
                if(closeCache.get(member)!=null){
                    closeCache.get(member).channel().disconnect();
                    closeCache.remove(member);
                }
                channel.writeAndFlush(response(HttpStatus.SC_OK,""));
            case  "select" :
                channel.writeAndFlush(response(HttpStatus.SC_OK,JSON.toJSONString(ServerHandler.clientChannel.values(),
                    SerializerFeature.DisableCircularReferenceDetect)));
                break;
            case  "pull" :
                List<GoClientTable> collect = ServerHandler.clientChannel.values().stream().filter(
                    x -> "0".equals(x.getProxyPort())).collect(Collectors.toList());
                channel.writeAndFlush(response(HttpStatus.SC_OK,JSON.toJSONString(collect,
                    SerializerFeature.DisableCircularReferenceDetect)));
                break;
            default:
                channel.writeAndFlush(response(HttpStatus.SC_NOT_FOUND,""));
                break;
        }

    }
    public FullHttpResponse response(int status,String jsonContent){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
            HttpResponseStatus.valueOf(status), Unpooled.wrappedBuffer(jsonContent.getBytes()));
        response.headers().set("Content-Type","text/plain;charset=UTF-8");
        response.headers().set("Content-Length",response.content().readableBytes());
        response.headers().set("Access-Control-Allow-Origin","*");
        response.headers().set("Access-Control-Allow-Headers","*");//允许headers自定义
        response.headers().set("Access-Control-Allow-Methods","PUT,POST,GET,DELETE,OPTIONS");
        // response.headers().set("Access-Control-Allow-credentials","true");

        return response;
    }
    public FullHttpResponse response(int status,String jsonContent,boolean auth){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
            HttpResponseStatus.valueOf(status), Unpooled.wrappedBuffer(jsonContent.getBytes()));
        if(auth){
            response.headers().set("www-Authenticate","Basic realm=\"Secure Area\"");
        }
        response.headers().set("Content-Type","text/plain;charset=UTF-8");
        response.headers().set("Content-Length",response.content().readableBytes());
        response.headers().set("Access-Control-Allow-Origin","*");
        response.headers().set("Access-Control-Allow-Headers","*");//允许headers自定义
        response.headers().set("Access-Control-Allow-Methods","PUT,POST,GET,DELETE,OPTIONS");
        // response.headers().set("Access-Control-Allow-credentials","true");

        return response;
    }


}

