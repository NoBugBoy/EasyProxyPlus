package utils;


/**
 * author yujian
 * description
 * create 2021-02-03 10:34
 **/
public class MessageBuild {
    public static Message onlyType(int type){
        Message message = new Message();
        message.setType(type);
        message.setLength(0);
        return message;
    }
}
