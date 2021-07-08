# 基于netty实现的内网穿透
由于本地无法被公网访问，如果想通过请求公网服务器将流量转发给本地就实现了内网穿透



### 客户端代理单个端口启动方式
先从客户端启动命令上进行解释
```java
java -jar EasyProxyClient-1.0.jar 
-server 47.110.122.122:18888 -pp 9001 -local localhost:8080
```
| 命令 | 作用 |
|--|--|
| -port | 代表服务器的port |
| -web | 指定web端口（html页面调用时使用） |
| -h | 提示以上命令（其他指令先忽略） |

```


当与服务端建立连接后，EasyProxyServer会新监听一个-pp指定的端口，监听成功后，通知EasyProxyClient，EasyProxyClient与本地服务建立连接，然后就可以访问了。

服务端的启动命令解释
### 服务端启动方式 1
```java
 java -jar EasyProxyServer-1.0.jar -port 6379 -web 18800
```
| 命令 | 作用 |
|--|--|
| -port | 启动的端口号 |
| -sync | 是否同步等待（设置后不会无限等待,可选） |
| time | 同步等待时间，超时自动返回，可选 默认30S |
| -h | 提示以上命令 |


