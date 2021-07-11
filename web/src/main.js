import Vue from 'vue'
import App from './App.vue'
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/antd.css';
import axios from "axios";


// axios.defaults.withCredentials = true
axios.interceptors.request.use(
	config => {
		config.headers.common['Access-Control-Allow-Origin'] = '*'
		return config;
	},
	err => {
		return Promise.reject(err);
	}
); 

Vue.prototype.$http = axios;



import router from './route.js'
Vue.config.productionTip = false
router.beforeEach((to, from, next) => {
  /* 路由发生变化修改页面title */
  if (to.meta.title) {
    document.title = to.meta.title
  }
  next()
})

Vue.use(Antd)

new Vue({
    router,
  render: h => h(App),
}).$mount('#app')