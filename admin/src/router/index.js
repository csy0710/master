import { createRouter, createWebHistory } from 'vue-router'
//路由页面，对页面的跳转进行编辑
//静态导入适合小型项目，页面少，对编译后的文件大小影响不大
const routes = [



  {
    path: '/',
    component: () => import('../views/main.vue'),

    children: [{
      path: 'welcome',
      component: () => import('../views/main/welcome.vue'),
    },
      {
        path: 'about',
        component: () => import('../views/main/about.vue'),
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


export default router
