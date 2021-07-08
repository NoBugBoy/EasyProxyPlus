package utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;
/**
 * Author yujian
 * Description 二进制解码器
 * Date 2021/2/2
 */ 
public class ProxyDecode extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() >= 12){
            int magic = in.readInt();
            if(magic == 9675){
                int type = in.readInt();
                if(type == Status.ping || type == Status.connbak){
                    out.add(MessageBuild.onlyType(type));
                }else{
                    int length = in.readInt();
                    //pass
                    int check      = in.readInt();
                    if(in.readableBytes() >= check){
                        Message message = new Message();
                        byte[]  bytes   = new byte[length];
                        in.readBytes(bytes);
                        JSONObject info = JSON.parseObject(new String(bytes, CharsetUtil.UTF_8));
                        message.setType(type);
                        message.setMagic(magic);
                        message.setLength(length);
                        message.setInfo(info);
                        message.setCheck(check);
                        byte[]  data   = new byte[check - length];
                        in.readBytes(data);
                        message.setData(data);
                        out.add(message);
                    }
                }
            }
        }
    }


}
