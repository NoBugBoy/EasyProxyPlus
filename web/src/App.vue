<template>
  <div id="app">
    <router-view v-if="this.isRouterAlive"></router-view>
  </div>
</template>

<script>

export default {
  name: 'app',
  data(){
    return{
      isRouterAlive: true
    }
  },
  provide(){
    return {
      reload: this.reload
    }
  },
  methods:{
    reload(){
      this.isRouterAlive = false;
      this.$nextTick(function () {
        this.isRouterAlive = true;
      });
  }
}}

</script>

<style>
</style>
