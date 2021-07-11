<template>
    <div style="text-align: center;
    height: 800px;
    width: 500px;
　　　　　　　　　　　　　margin:0 auto; ">
  <a-form :form="form" :label-col="{ span: 5 }" :wrapper-col="{ span: 12 }" @submit="handleSubmit" style="margin-top: 80px;">
   
    <a-form-item label="服务端地址">
      <a-input
        v-decorator="['host', { rules: [{ required: true, message: 'Please input your remote host!' }] }]"
      />
    </a-form-item>
   
    <a-form-item :wrapper-col="{ span: 12, offset: 5 }">
      <a-button type="primary" html-type="submit" onClick="submit">
        Submit
      </a-button>
    </a-form-item>
  </a-form>
    </div>
</template>

<script>
import common from "@/path";
export default {
  data() {
    return {
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
      openNotification() {
          this.$notification.open({
            message: 'Notification',
            description:
              '设置成功.',
            icon: <a-icon type="smile" style="color: #108ee9" />,
          });},
    handleSubmit(e) {
      this.form.validateFields((err, values) => {
        if (!err) {
            
             sessionStorage.setItem("host",values.host)
             this.openNotification()
                this.$router.push("/list"); 
        }
      });
    },
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