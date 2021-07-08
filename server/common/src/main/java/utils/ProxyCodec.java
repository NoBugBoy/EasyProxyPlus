package utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Author yujian
 * Description 二进制解码器
 * Date 2021/2/2
 */ 
public class ProxyCodec extends ByteToMessageCodec<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message message, ByteBuf out) throws Exception {
            out.writeInt(9675);
            out.writeInt(message.getType());
            int base = 4;
            if(message.getInfo() == null){
                message.setLength(0);
                message.setCheck(base + (message.getData()==null?0:message.getData().length));
                out.writeInt(message.getCheck());
                out.writeInt(0);
            }else {
                message.setCheck(base + (message.getData()==null?0:message.getData().length + message.getLength()));
                out.writeInt(message.getCheck());
                out.writeInt(message.getLength());
            }
            if(message.getInfo()!=null){
                out.writeBytes(message.getInfo().toJSONString().getBytes(CharsetUtil.UTF_8));
            }
            if(message.getData()!=null){
                out.writeBytes(message.getData());
            }


    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        out.clear();
        if(in.readableBytes() >= 8){
            int magic = in.readInt();
            if(magic == 9675){
                int type = in.readInt();
                if(type == Status.ping || type == Status.pong || type == Status.connbak){
                    out.add(MessageBuild.onlyType(type));
                }else{
                    //pass
                    int check      = in.readInt();
                    if(check == 0){
                        return;
                    }
                    int length = in.readInt();
                    if(in.readableBytes() >= length){
                        Message message = new Message();
                        byte[]  bytes   = new byte[length];
                        in.readBytes(bytes);
                        JSONObject info = JSON.parseObject(new String(bytes,CharsetUtil.UTF_8));
                        message.setType(type);
                        message.setMagic(magic);
                        message.setLength(length);
                        message.setInfo(info);
                        if(in.isReadable()){
                            byte[] data = ByteBufUtil.getBytes(in);
                            message.setData(data);
                        }
                        out.add(message);
                    }
                }
            }

        }
    }
}
