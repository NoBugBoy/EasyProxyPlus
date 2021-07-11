<template>
    <div>
  <a-table 
  :columns="columns" 
  :data-source="data"
  :rowClassName="tableRowClassName"
  :pagination="false"
  >

    <a slot="name" slot-scope="text">{{ text }}</a>
    <span slot="customTitle"><a-icon type="smile-o" /> Name</span>
    <span slot="tags" slot-scope="tags">
      <a-tag
        v-for="tag in tags"
        :key="tag"
        :color="tag === 'loser' ? 'volcano' : tag.length > 5 ? 'geekblue' : 'green'"
      >
        {{ tag.toUpperCase() }}
      </a-tag>
    </span>
     <span slot="action" slot-scope="text, record">
       
        <a-button type="danger" v-on:click="close(record)">
              CloseNatPort
            </a-button>
        </span>
  
  </a-table>
  </div>
</template>
<script>
const columns = [
  {
    title: 'HostName',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: 'Ip',
    dataIndex: 'ip',
    key: 'ip',
  },
  {
    title: 'Port',
    key: 'port',
    dataIndex: 'port',
  },
  {
    title: 'Describe',
    key: 'desc',
    dataIndex: 'desc',
  },
  {
    title: 'NatPort',
    key: 'proxyPort',
    dataIndex: 'proxyPort',
  },
  {
    title: 'ReadByte',
    key: 'readBytes',
    dataIndex: 'readBytes',
  },
  {
    title: 'WriteByte',
    key: 'writeBytes',
    dataIndex: 'writeBytes',
  },
  {
    title: 'ConnectTime(Minutes)',
    key: 'createTime',
    dataIndex: 'createTime',
  },
  {
    title: 'Status',
    key: "status",
    dataIndex: 'status',
  },
  {
      title: 'Action',
      key: 'action',
      scopedSlots: { customRender: 'action' },
    },
];

const data = [
        
];
import common from "@/path";
export default {
  inject:['reload'],
  data() {
    return {
      data,
      columns,
      inv: null,
    };
  },
  destroyed: function () {
    clearInterval(this.inv)
  },
  created() {
      this.select();
      this.autoReload();
  },
  methods:{
      autoReload(){
         this.inv =  setInterval(this.select, 3000);
      },
      close(e){
          let host = sessionStorage.getItem("host")
          this.$http.post(host, {
                       "method":"delete",
                       "params":{
                           "targetName":e.name,
                           "targetPort":e.port
                       }
                  }    ).then((response) => {
                                   this.reload()
                         
                      })
                      .catch(() =>{
                          this.reload()
                      });
      },
      tableRowClassName(row, rowIndex) { 
            console.log(row)
            if(row.status==='已断开'){
                 return 'red'
            }else if (row.proxyPort === '未配置Nat端口' && row.status==='已连接'){
                return 'yellow'
            }else{
                   return 'green'
            }
            
           },
      select(){
          let host = sessionStorage.getItem("host")
          this.$http.post(host, {
                   "method":"select",
              } ,{
						emulateJSON: true
					})   .then((response) => {
                  console.log(response)
                    for (var i=0;i<response.data.length;i++)
                    { 
                     
                        response.data[i].createTime = new Date(new Date().getTime() - response.data[i].createTime).getMinutes();
                        if (response.data[i].conn.active) {
                            response.data[i].status = '已连接'
                        }else{
                              response.data[i].status = '已断开'
                        }
                        if(response.data[i].proxyPort === '0'){
                            response.data[i].proxyPort="未配置Nat端口"
                            response.data[i].desc="未配置Nat端口"
                        }
                        
                    }
                      this.data = response.data
                    
                      console.log( this.data )
                  })
                  .catch(() =>{
                        
                  });
          },
      },
};
</script>
<style>
.yellow {
    background-color: yellow;
}

.green {
    background-color: yellowgreen;
}

.red {
    background-color: red;
}
    
</style>