import { createRouter, createWebHistory } from 'vue-router'

//静态导入适合小型项目，页面少，对编译后的文件大小影响不大
const routes = [

    // 登录
  {
    path: '/login',
    component: () => import('../views/login.vue')
  },
  {
    path: '/',
    component: () => import('../views/main.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
