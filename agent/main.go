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
func pong (targetName string,coreThread net.Conn){
	fmt.Println("收到来自服务端的心跳包...")
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

	coreThread.Write(sm.Encode())
}

func waiting(targetName string,coreThread net.Conn,list *tcpclient.TcpClient)  {
	for {
		buf := make([]byte, 10240)
		offset, err := coreThread.Read(buf)
		if err != nil {
			if err == io.EOF {
				fmt.Println("与服务端断开连接，尝试重连...")
				coreThread = utils.Bind("127.0.0.1","9675")
			}
			continue
		}
		if offset > 8 {
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
						clientConn := utils.Bind(data.Ip,data.Port)
						data.Conn = clientConn
						go data.ClientConnection()
						//pass
					case types.Data:
						data.Send(s.Data)

					}
				}
			}

		}
	}
}

func main() {
	ip := flag.String("ip", "127.0.0.1", "nettyServer ip")
	port := flag.String("port", "9675", "nettyServer 端口")
	name := flag.String("name", "client", "客户端别名")
	flag.Parse()
	//开启server
	coreThread := utils.Bind(*ip,*port)
	client := &tcpclient.TcpClient{}
	client.ImportProxy()
	targetName := *name
	// core thread
	go waiting(targetName,coreThread,client)

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
			return
		}
	}

	for  {
		time.Sleep(10 * time.Second)
	}

	
}
