
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
                    children:[
                        {
                           path: '/list',
                           name: 'List',
                           component: List
                        },
                        {
                           path: '/create',
                           name: 'Create',
                           component: Create
                        },
                        {
                           path: '/set',
                           name: 'Set',
                           component: Set
                        },
                        ]
                },
               
              ]
   }
)
