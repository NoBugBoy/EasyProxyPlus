package utils;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * Author yujian
 * Description 二进制消息体
 * Date 2021/2/2
 */
public class Message {
  private   int magic;
  private   int typ;
  private   int check;
  private   int length;
  private   JSONObject info;
  private   byte[] data;

    public int getCheck() {
        return check;
    }

    public void setCheck(int check) {
        this.check = check;
    }

    public JSONObject getInfo() {
        return info;
    }

    public void setInfo(JSONObject info) {
        this.info = info;
    }

    public int getMagic() {
        return magic;
    }

    public void setMagic(int magic) {
        this.magic = magic;
    }

    public int getType() {
        return typ;
    }

    public void setType(int type) {
        this.typ = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getTyp() {
        return typ;
    }

    public void setTyp(int typ) {
        this.typ = typ;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "utils.Message{" + "magic=" + magic + ", type=" + typ + ", length=" + length + ", data='" + data + '\'' + '}';
    }
}
