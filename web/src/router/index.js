import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
//静态导入适合小型项目，页面少，对编译后的文件大小影响不大
const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
    // 懒加载 适合大型项目 页面多，80%的页面不常用，减少编译后的文件大小，提高初始访问速度。
  {
    path: '/about',
    name: 'about',
    component: () => import(/* webpackChunkName: "about" */ '../views/AboutView.vue')
  },
    // 登录
  {
    path: '/login',
    component: () => import('../views/login.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
