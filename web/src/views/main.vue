<template>
  <a-layout id="components-layout-demo-top-side-2">
    <the-header-view></the-header-view>
    <a-layout>
      <the-sider-view></the-sider-view>
      <a-layout-content
          :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
      >
        所有会员总数：{{count}}
        <router-view></router-view>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>
<script>
import {defineComponent, ref} from 'vue';
import TheHeaderView from "@/components/the-header";
import TheSiderView from "@/components/the-sider";
import axios from "axios";
import {notification} from "ant-design-vue";
// import store from "@/store";

export default defineComponent({
  components: {
    TheSiderView,
    TheHeaderView,
  },
  setup() {
    const count=ref(0);//ref声明基本的数据类型
    axios.get("/member/member/count").//直接传递实体
        then((response)=> {
          let data = response.data;//获取结果的数据
          if (data.success) {
            count.value = data.content;
          } else {
            notification.error({description: data.message});
          }
        })
    return {
      count
    };
  },
});
</script>
<style>

</style>
