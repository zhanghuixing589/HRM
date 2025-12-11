import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'HrmLogin',
    component: () => import('@/views/HrmLogin.vue')
  },
  {
    path: '/layout',
    name: 'HrmLayout',
    component: () => import('@/views/HrmLayout.vue')
  }
]

const router = new VueRouter({
  mode: 'history',
  routes
})

export default router