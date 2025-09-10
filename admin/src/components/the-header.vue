<template>
  <a-layout-header class="header">
    <div class="logo">
      <router-link to="/welcome" style="color: white; font-size: 18px">
        甲蛙12306控台
      </router-link>
    </div>
    <div style="float: right; color: white;">&nbsp;
        欢迎使用管理控台
    </div>
    <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="dark"
        mode="horizontal"
        :style="{ lineHeight: '64px' }"
    ><!--将a-menu-item中的key放入组件selectedKeys（选中哪些key）-->
      <a-menu-item key="/welcome">
        <router-link to="/welcome">
          <coffee-outlined /> &nbsp; 欢迎
        </router-link>
      </a-menu-item>
      <a-menu-item key="/about">
        <router-link to="/about">
          <user-outlined /> &nbsp; 关于
        </router-link>
      </a-menu-item>
    </a-menu>
  </a-layout-header>
</template>

<script>
import {defineComponent, ref, watch} from 'vue';
import router from '@/router'

export default defineComponent({
  name: "the-header-view",
  setup() {
    const selectedKeys = ref([]);/*对应上面的selectedKeys声明成一个数组ref*/

    watch(() => router.currentRoute.value.path, (newValue) => {/*watch是被动监视页面路由变化，当页面路由发生变化，找到key值对应的组件使它成为激活状态*/
      console.log('watch', newValue);
      selectedKeys.value = [];/*每次路由变换首先清空*/
      selectedKeys.value.push(newValue);/*侦听页面变化时将newValue（页面上更新的路由）放入selectedKeys，实现组件响应，因为key和路由保持一致*/
    }, {immediate: true});
    return {
      selectedKeys
    };
  },
});
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.logo {
  float: left;
  height: 31px;
  width: 150px;
  color: white;
  font-size: 20px;
}
</style>
