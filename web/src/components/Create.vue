<template>
    <div style="text-align: center;
    height: 600px;
    width: 500px;
　　　　　　　　　　　　　margin:0 auto; ">
  <a-form :form="form" :label-col="{ span: 5 }" :wrapper-col="{ span: 12 }" @submit="handleSubmit" style="margin-top: 80px;">
    
    <a-form-item label="Proxy">
      <a-select
      v-decorator="['proxy', { rules: [{ required: true, message: 'Please select your proxy!' }] }]"
        placeholder="Select not set natPort target"
        @change="handleSelectChange"
      >
        <a-select-option v-for="(nat,roleindex) in data" :key="nat.name +split +  nat.ip +split +  nat.port">
             {{ nat.name+ "-" + nat.ip + "-" + nat.port }}
        </a-select-option>
       
      </a-select>
    </a-form-item>
    <a-form-item label="NatPort">
      <a-input
        v-decorator="['proxyPort', { rules: [{ required: true, message: 'Please input your NatPort!' }] }]"
      />
    </a-form-item>
    <a-form-item label="Desc">
      <a-input
        v-decorator="['desc', { rules: [{ required: true, message: 'Please input your Desc!' }] }]"
      />
    </a-form-item>
   
    <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
      <a-button type="primary" html-type="submit" onClick="submit">
        Submit
      </a-button>
    </a-form-item>
  </a-form>
  <Home v-bind:current = "current"></Home>
    </div>
</template>

<script>
import common from "@/path";
export default {
  data() {
    return {
      current : ['create'],
      createData: {},
      split : ',',  
      data: [],
      formLayout: 'horizontal',
      form: this.$form.createForm(this, { name: 'coordinated' }),
    };
  },
  created() {
      this.pull()
  },
  methods: {
    create(){
          let host = sessionStorage.getItem("host")
        this.$http.post(host, {
                     "method":"create",
                     "params":{
                         "proxyPort":this.createData.proxyPort,
                         "targetName":this.createData.targetName,
                         "targetPort":this.createData.targetPort,
                         "desc": this.createData.desc,
                     }
                }    ).then((response) => {
                    this.openNotification()
                    this.current = ['list'],
                                 this.$router.push("/list");
                       
                    })
                    .catch(() =>{
                      this.$message.error("请求超时")
                    });
          
    },
    pull(){
          let host = sessionStorage.getItem("host")
       this.$http.post(host, {
                    "method":"pull",
               }    ).then((response) => {
                   console.log(response)                   
                       this.data = response.data
                   })
                   .catch(() =>{
                     this.$message.error("请求超时")
                   });
           } ,
    handleSubmit(e) {
       
      this.form.validateFields((err, values) => {
        if (!err) {
            console.log(values)
       
            this.createData.proxyPort = values.proxyPort;
           
            this.createData.desc = values.desc;
            
            var s = values.proxy.split(',')
            
            this.createData.targetName= s[0];
            
            this.createData.targetPort = s[2];
           
            this.create()
       
        }
      });
    },
      openNotification() {
          this.$notification.open({
            message: 'Notification',
            description:
              'Nat端口设置成功.',
            icon: <a-icon type="smile" style="color: #108ee9" />,
          });},
    startApp() {
        return Vue.http.get('/static/config.json')
    },
    handleSelectChange(value) {

    },
     handleChange(value) {
         
        },
        handleBlur() {
        
        },
        handleFocus() {
        
        },
        filterOption(input, option) {
          return (
            option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
          );
        },
  }
};
</script>