package utils

import (
	"bytes"
	"encoding/binary"
	"fmt"
	"net"
)

func IntToBytes(n int) []byte {
	x := int32(n)
	bytesBuffer := bytes.NewBuffer([]byte{})
	_ = binary.Write(bytesBuffer, binary.BigEndian, x)
	return bytesBuffer.Bytes()
}
func BytesToInt(b []byte) int32 {
	bytesBuffer := bytes.NewBuffer(b)
	var x int32
	_ = binary.Read(bytesBuffer, binary.BigEndian, &x)
	return x
}

func Bind(ip,port string) net.Conn {
	target := net.JoinHostPort(ip, fmt.Sprintf("%s", port))
	conn,err := net.Dial("tcp4",target)
	if err!=nil {
		fmt.Println("net.ListenTCP error:",err)
	}
	return conn
}
