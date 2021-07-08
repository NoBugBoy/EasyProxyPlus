package tcpserver

import (
	"bytes"
	"proxy-agent/utils"
)

type ServerMessage struct {
	Magic  int32
	Typ    int32
	Check int32
	Length int32
	Info []byte
	Data   []byte
}

type Transfer interface {
	Encode() []byte
	Decode()  *ServerMessage
}

func (sm *ServerMessage) Encode() []byte {
	dataBuff := bytes.NewBuffer([]byte{})
	//4
	dataBuff.Write(utils.IntToBytes(9675))
	//4
	dataBuff.Write(utils.IntToBytes(int(sm.Typ)))
	var size = 0
	if sm.Data != nil {
		size += len(sm.Data)
	}
	if sm.Info != nil {
		size += len(sm.Info)
	}
	if sm.Info != nil {
		//4
		dataBuff.Write(utils.IntToBytes(len(sm.Info)))
	}else{
		//4
		dataBuff.Write(utils.IntToBytes(0))
	}
	//4
	dataBuff.Write(utils.IntToBytes(size))
	if sm.Info != nil {
		dataBuff.Write(sm.Info)
	}
	if sm.Data != nil {
		dataBuff.Write(sm.Data)
	}
	return dataBuff.Bytes()
}

func (sm *ServerMessage) Decode(byt []byte) *ServerMessage {
	sm.Magic = utils.BytesToInt(byt[0:4])
	if sm.Magic == 9675 {
		sm.Typ = utils.BytesToInt(byt[4:8])
		sm.Length = utils.BytesToInt(byt[12:16])
		if sm.Length > 0 {
			sm.Info = byt[16 : sm.Length+16]
		}
		sm.Data = byt[sm.Length+16:]
	}
	return sm
}

func (sm *ServerMessage) GetMagic() int32{
	return sm.Magic
}
func (sm *ServerMessage) GetTyp() int32{
	return sm.Typ
}
func (sm *ServerMessage) GetLength() int32{
	return sm.Length
}
func (sm *ServerMessage) GetData() []byte{
	return sm.Data
}