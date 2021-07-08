# 基于netty实现的内网穿透
由于本地无法被公网访问，如果想通过请求公网服务器将流量转发给本地就实现了内网穿透

jar包下载地址
服务端：[EasyProxyServer](https://github.com/NoBugBoy/EasyProxy/releases/download/1.0/EasyProxyServer-1.0.jar)
客户端：[EasyProxyClient](https://github.com/NoBugBoy/EasyProxy/releases/download/1.0/EasyProxyClient-1.0.jar)
docker镜像：```docker pull yujian1996/eps:1.0```
源代码（点个star谢谢）：[Github](https://github.com/NoBugBoy/EasyProxy/tree/main)
> 大致流程图

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210203160536292.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0RheV9EYXlfTm9fQnVn,size_16,color_FFFFFF,t_70)

> 整体设计

大概设计思路就是，在公网服务器上部署一个server端，在本地部署多个client端，client与server建立tcp长连接，进行流量转发，client与server已经建立连接，并且使用自定义协议通信，由于客户端的请求可能为socket或http等方式，所以不能与server共用一个handler，每个穿透都需要有自己的端口定义自己的pipeline进行编解码。所以会出现一个类似如下的图
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210203162127471.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L0RheV9EYXlfTm9fQnVn,size_16,color_FFFFFF,t_70)

 1. EasyProxyServer启动在42.122.122.1的服务器上（外网可访问）
 2. EasyProxyClient启动在本地，并设置server为42.122.122.1与其建立长连接，申请代理本地的tomcat端口8080（此时未与其建立连接），还需要配置一个mapping port 9000，该端口在服务端被监听
 3. 当9000在服务端监听后，会通知EasyProxyClient可以与tomcat建立连接
 4. 此时访问proxy 9000会将流量转发给EasyProxyServer，EasyProxyServer再将流量转发给EasyProxyClient，EasyProxyClient接收到后，会找到与9000配对的端口（也就是本地tomcat的8080）将流量进行发送
 5. tomcat接收到请求后（此时就可以debug等操作），将response返回给EasyProxyClient与tomcat建立连接的随机端口号client proxy，client proxy将response交给EasyProxyClient，EasyProxyClient发送给EasyProxyServer，EasyProxyServer转发给浏览器

> 编码

### 客户端代理单个端口启动方式
先从客户端启动命令上进行解释
```java
java -jar EasyProxyClient-1.0.jar 
-server 47.110.122.122:18888 -pp 9001 -local localhost:8080
```
| 命令 | 作用 |
|--|--|
| -server | 代表服务器的host:port |
| -local | 代表需要穿透的本地host:port |
| -pp | 与本地端口映射的服务器端口，在服务器请求该端口穿透到本地服务 |
| -h | 提示以上命令 |
### 客户端代理多个端口启动方式
在jar包同级目录放入proxy.yml并按照模板编写，可一次代理多个本地端口
```yml
java -jar EasyProxyClient-1.0.jar -yml

proxys:
    - server: localhost:18888
      local: localhost:8080
      proxyPort: 18080
    - server: localhost:18888
      local: localhost:8081
      proxyPort: 18081


```


当与服务端建立连接后，EasyProxyServer会新监听一个-pp指定的端口，监听成功后，通知EasyProxyClient，EasyProxyClient与本地服务建立连接，然后就可以访问了。

服务端的启动命令解释
### 服务端启动方式 1
```java
 java -jar EasyProxyServer-1.0.jar -port 18888 -sync
```
| 命令 | 作用 |
|--|--|
| -port | 启动的端口号 |
| -sync | 是否同步等待（设置后不会无限等待,可选） |
| time | 同步等待时间，超时自动返回，可选 默认30S |
| -h | 提示以上命令 |

### 服务端启动方式 2 docker启动
```java
# 命令和上面一样，注意SPORT需要和容器暴露的端口保持一致,客户端指定的端口也要暴露出来，或者使用host模式
docker run -d --name aaa -p 18888:18888 -e SPORT=18888 -e SYNC=true -e -TIME=30 yujian1996/eps:1.0
#host模式
docker run -d --name aaa --network=host -e SPORT=18888 -e SYNC=true -e -TIME=30 yujian1996/eps:1.0
```

> 遇到的几个问题，和解决办法

 
1. EasyProxyClient在IdleStateHandler的WRITER_IDLE的长时间未写通知后会与本地服务断开连接，    ctx.channel().isActive(） 会变为false状态，所以当长时间未写时，进行一次重新连接
```java
           if (idleEvent.state() == IdleState.READER_IDLE) {
                connbak();
            } else if (idleEvent.state() == IdleState.WRITER_IDLE) {
                connbak();
            } else if (idleEvent.state() == IdleState.ALL_IDLE) {
               ctx.close();
            }
```
2. 在请求代理端口时，偶尔会出现padding问题，再次请求就恢复了，debug发现调用了writeAndFlush，但是没有发送成功，甚至没有调用encode编码器，最后发现是粘包了
```java
  //数据包最大长度  数据包长度偏移量 数据包长度字节数 剩余长度 忽略长度
  //我这里数据包长度偏移量是8 前面有4个字节的magic和4个字节的type
  LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,8,4,0,0)
```
