
import Vue from "vue";
import VueRouter from "vue-router";
import List from "./components/List.vue"
import Home from "./components/Home.vue"
import Create from "./components/Create.vue"
import Set from "./components/Set.vue"

Vue.use(VueRouter);

export default new  VueRouter(
    {
            routes: [
   
                {
                    path: '/',
                    name: 'Home',
                    component: Home,
                    meta: {
                            title: 'EasyProxyPlus'
                          },
                    children:[
                        {
                           path: '/list',
                           name: 'List',
                           component: List,
                           meta: {
                                   title: 'EasyProxyPlus'
                                 }
                           
                        },
                        {
                           path: '/create',
                           name: 'Create',
                           component: Create,
                           meta: {
                                   title: 'EasyProxyPlus'
                                 }
                        },
                        {
                           path: '/set',
                           name: 'Set',
                           component: Set,
                           meta: {
                                   title: 'EasyProxyPlus'
                                 }
                        },
                        ]
                },
               
              ]
   }
)
