<template>
    <div>
     
    <a-menu v-model="current" mode="horizontal" >
      <a-menu-item key="list">
         <a-icon type="ordered-list" />
           穿透连接列表
          <router-link to="/list" />
      </a-menu-item>
  
      <a-sub-menu>
        <span slot="title" class="submenu-title-wrapper"
          ><a-icon type="setting" />设置</span
        >
        <a-menu-item-group title="create">
          <a-menu-item key="setting:1">
            设置转发端口
            <router-link to="/create" />
                 </a-menu-item>
            <a-menu-item key="setting:2">
              设置EasyProxyPlus后台地址
              <router-link to="/set" />
          </a-menu-item>
          
        </a-menu-item-group>
      </a-sub-menu>
    
    </a-menu>
    <div>
         <router-view/>
    <div style="position:fixed;
	bottom:0;
	left:0;
	right:0;
	margin:auto;
    width: 400px;
    height: 50px;
	">
      Easy Proxy Plus ©2021 Created by <a href="https://github.com/NoBugBoy/">NoBugBoy</a>
    </div>
    </div>

  </div>
</template>
<script>
export default {
  data() {
    return {
      current: ['list'],
    };
  },
};
</script>
