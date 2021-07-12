#  EasyProxyPlus内网穿透（支持多客户端多端口）

>  EasyProxyPlus由三端组成，Web端使用Vue+Antd编写,通过调用Server的Api,对Nat端口（代理端口）进行配置，关闭，查看连接状态、字节流量等信息,Server端使用Java+netty编写，用来与client通信，发送心跳，Client端使用Golang编写，用来穿透目标服务等

## 画外音

免费开源，点个star不算过分吧 XDM  **https://github.com/NoBugBoy/EasyProxyPlus**


### 1. 安装启动（我这里用本地环境演示）

1. 准备台外网可以访问的云服务器（没有假装本地就是。。）
2. 在云服务器安装Web端和Server端
```shell
# Server端有两个环境变量 WEB、PORT ，分别为web端访问的api端口，和与client连接的端口
# 由于配置的穿透端口需要被宿主机访问到,最好使用host模式或 -p 指定一个范围端口
docker run -d -it  -e WEB=18888 --network host --name sever yujian1996/easyserver:1.0
```
```shell
# Web端直接指定个端口启动就行了，访问后端的地址在页面的设置中配置
docker run -d -it --name web -p 8888:80 easyweb
```
```shell
# Client端封装成了可执行文件,直接运行即可。支持linux mac windows，下载地址
https://github.com/NoBugBoy/EasyPorxyPlus/releases/tag/1.0
# 在客户端的同级目录下需要创建一个proxy.conf文件用来描述哪些本地的服务需要被穿透
#格式为 ip ; port
#我这里配置为穿透一个8080的web服务，和一个内网服务器的ssh
localhost;8080
172.16.3.141;22
```
---
非docker环境的安装稍微麻烦点，后续可以考虑优化。

web端需要clone 源码 & npm install & npm run build 将生成的disk目录交给nginx去代理使用。
server端需要服务器有Java的环境使用 java -jar 运行即可 **-h 可以查看帮助**

### 2. 穿透流程（以下端口均按上述安装步骤配置的端口描述）

当我们启动好Web时,访问映射的端口8888即可打开页面，此时什么时候都做不了，我们需要启动一下Server端

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/2a31b7bae0ce4aba824b9447f7b21567~tplv-k3u1fbpfcp-watermark.image)

当启动好Server之后，我们在页面的设置配置下server的访问地址

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/621663ea03c94cffa8eab829fbe6c975~tplv-k3u1fbpfcp-watermark.image)

这个时候你会发现，好像并没有什么变化，确实，因为代理不是在server端创建的，**凭空创建一个穿透是没有意义的**，只有当你的客户端配置了哪些端口需要被代理，并连接到了server的时候才能看到列表中的信息，**web管理端会控制给某个客户端的某个服务配置使用哪个Nat端口，当然也可以控制关闭端口**

启动一下客户端，再来查看页面,hostname是客户端的别名（多个客户端时容易区分）-name 可以指定，此时页面会出现上面客户端指定的两个服务，因为未配置Nat端口所以是黄色的

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/558bf5283d1143a899fbd838a9de8925~tplv-k3u1fbpfcp-watermark.image)

我们来给client的8080配置一个代理端口，未配置的服务信息可以从下拉列表中选择到，指定一个Nat端口和备注信息就可以了**当配置错或想换一个代理端口时，在列表close然后就可以重新配置了**

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/0efaebe184b14b688f46924e35574d80~tplv-k3u1fbpfcp-watermark.image)

提交之后我们回到列表页查看，此时已经为绿色代表成功设置了nat端口并与客户端连通

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/4e22959fd67242f19ca71cf425992ad1~tplv-k3u1fbpfcp-watermark.image)

我们再以同样的方式配置一下ssh的代理**例如Redis等中间件的代理也都可以穿透**

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/713ddea0bb444fa4928a8b5dc3d01083~tplv-k3u1fbpfcp-watermark.image)

我们来访问一下18080端口,已经成功穿透到了客户端的8080端口上,已经成功穿透

![image.png](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/72d2e5d46a384ff9ac6b60d2e7b4c6ad~tplv-k3u1fbpfcp-watermark.image)

我们再来测试一下ssh连接18022端口试试能否穿透到172.16.3.141上,已经成功穿透


![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/61efc1f86e3c4de59f369ddc8149fbe8~tplv-k3u1fbpfcp-watermark.image)

经过几次请求后，我们再看页面，此时能看到发送和接收的字节数 **真实字节数,未计算自定义协议的数据包**

![image.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/393c78ecfd8f4106befaad62a9ac36b0~tplv-k3u1fbpfcp-watermark.image)

我们断开客户端后再查看页面，此时已经变成红色

![image.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/b1d0ea230c7c49e0b50bdf922f45e336~tplv-k3u1fbpfcp-watermark.image)
