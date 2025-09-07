<template>
  <a-layout id="components-layout-demo-top-side-2"><!--main.vue负责页面框架的搭建，分成上下布局，上面是headers下面是layout，lay分成左右布局，左边是sidebar，右边是routerview动态页面-->
    <the-header-view></the-header-view>
    <a-layout>
      <the-sider-view></the-sider-view>
      <a-layout-content
          :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
      >
        <router-view></router-view>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>
<script>
import {defineComponent} from 'vue';
import TheHeaderView from "@/components/the-header";
import TheSiderView from "@/components/the-sider";

export default defineComponent({
  components: {
    TheSiderView,
    TheHeaderView,
  },
  setup() {

    return {

    };
  },
});
</script>
<style>

</style>
