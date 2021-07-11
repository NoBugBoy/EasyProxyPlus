package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"io"
	"net"
	"proxy-agent/tcpclient"
	"proxy-agent/tcpserver"
	"proxy-agent/types"
	"proxy-agent/utils"
	"time"
)


var client = &tcpclient.TcpClient{}

func pong (targetName string,coreThread net.Conn){
	fmt.Println("收到来自EasyProxyServer的心跳...")
	data := &tcpclient.ProxyData{
		TargetName:targetName,
	}
	marshaler,err := json.Marshal(data)
	if err != nil {
		panic(err)
	}
	sm := &tcpserver.ServerMessage{
		Typ:    types.Pong,
		Info: marshaler,
	}

	_, _ = coreThread.Write(sm.Encode())
}

func waiting(ip,port,targetName string,coreThread net.Conn,list *tcpclient.TcpClient)  {
	for {
		buf := make([]byte, 10240)
		offset, err := coreThread.Read(buf)
		if err != nil {
			if err == io.EOF {
				fmt.Println("与EasyProxyServer断开连接，尝试重连...")
				for{
					coreThread = utils.Bind(ip,port)
					if coreThread != nil {
						initProxy(targetName,coreThread)
						break
					}
					time.Sleep(3 * time.Second)
				}
			}
			continue
		}
		if offset > 12 {
			sm := &tcpserver.ServerMessage{}
			s := sm.Decode(buf[:offset])
			pd := &tcpclient.ProxyData{}
			if s.Typ == types.Ping{
				pong(targetName,coreThread)
				continue
			}
			if s.Info != nil {
				err := json.Unmarshal(s.Info, pd)
				if err != nil {
					panic(err)
				}
			}
			for _,data := range list.ProxyArray {
				if data.Port == pd.Port {
					switch s.Typ {
					case types.Connback:
						if data.Conn == nil {
							go reConn(data)
						}else{
							go data.ClientConnection()
						}
						//pass
					case types.Data:
						if data.Conn == nil {
							reConn(data)
						}
						data.Send(s.Data)
					}
				}
			}

		}
	}
}

func reConn(data *tcpclient.Proxy){
	for{
		clientConn := utils.Bind(data.Ip,data.Port)
		if clientConn != nil {
			data.Conn = clientConn
			data.ClientConnection()
			break
		}else {
			time.Sleep(1 * time.Second)
		}
	}
}
func main() {
	ip := flag.String("ip", "127.0.0.1", "EasyProxyServer host")
	port := flag.String("port", "9675", "EasyProxyServer port")
	name := flag.String("name", "client", "客户端别名")
	flag.Parse()
	//init server
	coreThread := utils.Bind(*ip,*port)
	client.ImportProxy()
	targetName := *name

	// core thread
	initProxy(targetName,coreThread)
	// waiting main thread
	waiting(*ip,*port,targetName,coreThread,client)
	
}

func initProxy(targetName string,coreThread net.Conn)  {

	for _,proxy := range client.ProxyArray {
		proxy.TargetName = targetName
		proxy.ServerConn = coreThread
		register := &tcpserver.Register{
			Name: targetName,
			Port: proxy.Port,
			Ip: proxy.Ip,
		}
		marshaler, _ := json.Marshal(register)
		sm := &tcpserver.ServerMessage{
			Magic:  9675,
			Typ:    types.Conn,
			Info: marshaler,
		}
		_, err := coreThread.Write(sm.Encode())
		if err != nil {
			fmt.Println(err)
			continue
		}
	}
}