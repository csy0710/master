import { createRouter, createWebHistory } from 'vue-router'
import store from "@/store";
import {notification} from "ant-design-vue";
//路由页面，对页面的跳转进行编辑
//静态导入适合小型项目，页面少，对编译后的文件大小影响不大
const routes = [

    // 登录
  {
    path: '/login',
    component: () => import('../views/login.vue')
  },
  {
    path: '/',
    component: () => import('../views/main.vue'),
    meta:{
      loginRequire:true
    },
    children: [{
      path: 'welcome',
      component: () => import('../views/main/welcome.vue'),
    },
      {
        path: 'passenger',
        component: () => import('../views/main/passenger.vue'),
      },{
        path: 'ticket',
        component: () => import('../views/main/ticket.vue'),
      },]
  },{
    path: '',//访问根域名（‘/’）直接跳转到根域名下welcome，/main/welcome
    redirect: '/welcome'
  },
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})
// 路由登录拦截
router.beforeEach((to, from, next) => {
  // 要不要对meta.loginRequire属性做监控拦截
  if (to.matched.some(function (item) {
    console.log(item, "是否需要登录校验：", item.meta.loginRequire || false);
    return item.meta.loginRequire
  })) {
    const _member = store.state.member;
    console.log("页面登录校验开始：", _member);
    if (!_member.token) {
      console.log("用户未登录或登录超时！");
      notification.error({ description: "未登录或登录超时" });
      next('/login');
    } else {
      next();
    }
  } else {
    next();
  }
});

export default router
