package utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;
import utils.Message;

/**
 * Author yujian
 * Description 二进制编码器
 * Date 2021/2/2
 */ 
public class ProxyEncode extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message message, ByteBuf out) throws Exception {
        out.writeInt(9675);
        out.writeInt(message.getType());
        if(message.getInfo() == null){
            message.setLength(0);
            message.setCheck( (message.getData()==null?0:message.getData().length));
            out.writeInt(message.getCheck());
            out.writeInt(0);
        }else {
            message.setCheck( (message.getData()==null?0:message.getData().length + message.getLength()));
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
}
