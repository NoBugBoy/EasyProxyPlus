package core;

import io.netty.channel.Channel;

import java.io.Serializable;

/**
 * author yujian
 * description
 * create 2021-07-01 10:25
 **/
public class GoClientTable implements Serializable {
    /**
     * 目标真实host别名
     */
    private String name;
    /**
     * 目标ip
     */
    private String ip;
    /**
     * 目标端口
     */
    private String port;
    private String desc;
    private Long writeBytes = 0L;
    private Long readBytes = 0L;
    private Long createTime;
    /**
     * 代理端口
     */
    private String proxyPort;
    private Channel conn;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getWriteBytes() {
        return writeBytes;
    }

    public void setWriteBytes(Long writeBytes) {
        this.writeBytes = writeBytes;
    }

    public Long getReadBytes() {
        return readBytes;
    }

    public void setReadBytes(Long readBytes) {
        this.readBytes = readBytes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public Channel getConn() {
        return conn;
    }

    public void setConn(Channel conn) {
        this.conn = conn;
    }
}
