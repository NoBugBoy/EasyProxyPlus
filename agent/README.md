EasyProxyPlus 客户端

打包
```
go build 
```
查看帮助
```
./proxy-agent -h 
```

查看帮助
```

./proxy-agent -ip {EasyProxyPlusSeverIp} -name {客户端别名(英文最好)} -port {EasyProxyPlusSeverPort}

  -ip string
        nettyServer ip (default "127.0.0.1")
  -name string
        客户端别名 (default "client")
  -port string
        nettyServer 端口 (default "9675")

```

配置代理服务，可以配置多个
```
在可执行程序目录下，创建proxy.conf文件
格式为:
localhost;6379
localhost;8080
```
