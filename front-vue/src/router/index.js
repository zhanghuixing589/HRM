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
  },

  {
    path:'/salary/manage',
    name: 'SalaryManage',
    component: () => import('@/views/salary/Manage.vue'),
    meta: { title: '薪酬管理', requiresAuth: true }
    
  },

{
  path: '/salary/projects',
  name: 'SalaryProjectList',
  component: () => import('@/views/salary/SalaryProjectList.vue'),
  meta: { title: '薪酬项目管理', requiresAuth: true }
},
{
  path: '/salary/projects/create',
  name: 'SalaryProjectCreate',
  component: () => import('@/views/salary/SalaryProjectCreate.vue'),
  meta: { title: '创建薪酬项目', requiresAuth: true }
}
]

const router = new VueRouter({
  mode: 'history',
  routes
})

export default router