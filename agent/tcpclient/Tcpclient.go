package tcpclient

import (
	"bufio"
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"io/ioutil"
	"net"
	"os"
	"proxy-agent/tcpserver"
	"proxy-agent/types"
	"proxy-agent/utils"
	"strings"
	"time"
)

type TcpClient struct {
	 ProxyArray []*Proxy
}

type Proxy struct {
	Port string
	Ip string
	TargetName string
	Conn net.Conn
	ServerConn net.Conn
}

func (cli *TcpClient) ImportProxy() {
	path , err := os.Getwd()
	if err != nil {
		fmt.Println("pwd is err",err)
		return
	}
	file, err := ioutil.ReadFile( path + "/proxy.conf")
	if err != nil {
		fmt.Println("read proxylist.txt error err = ",err)
		return
	}
	scanner := bufio.NewScanner(bytes.NewReader(file))
	for scanner.Scan() {
		if scanner.Text() != "" {
			s := strings.Split(scanner.Text(),";")
			if len(s) == 2{
				cli.ProxyArray = append(cli.ProxyArray, &Proxy{
					Ip: s[0],
					Port: s[1],
				})
			}
		}
	}
	fmt.Println("import proxy success count " , len(cli.ProxyArray))
}

type ProxyData struct {
	Port string `json:"port"`
	TargetName string `json:"targetName"`
}



func (pro *Proxy)ClientConnection(){
	_ =  pro.Conn.SetReadDeadline(time.Now().Add(time.Second * 10))
	for {
		buf := make([]byte, 10240)
		n, err := pro.Conn.Read(buf)
		if err != nil {
			if err == io.EOF{
				pro.Conn = utils.Bind(pro.Ip,pro.Port)
				continue
			}
			_ =  pro.Conn.SetReadDeadline(time.Now().Add(time.Second * 3))
			continue
		}
		data := &ProxyData{
			Port: pro.Port,
			TargetName: pro.TargetName,
		}
		bufs , err := json.Marshal(data)
		if err != nil {
			fmt.Println(err)
		}
		sm := &tcpserver.ServerMessage{
			Magic:  9675,
			Typ:    types.Back,
			Info: bufs,
			Data:   buf[:n],
		}
		coode := sm.Encode()
		_, err = pro.ServerConn.Write(coode)
		if err != nil {
			fmt.Println(err)
		}

	}
}

func (pro *Proxy) Send(data []byte)  {
	_, err := pro.Conn.Write(data)
	if err != nil {
		fmt.Println("send to local service err",err)
	}
}