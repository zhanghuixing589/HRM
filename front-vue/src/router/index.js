
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
    meta: { title: '薪酬管理', requiresAuth: true,
       roles: [3, 4, 6]
     }
    
  },

{
  path: '/salary/projects',
  name: 'SalaryProjectList',
  component: () => import('@/views/salary/SalaryProjectList.vue'),
  meta: { title: '薪酬项目管理', requiresAuth: true ,roles: [1,3,5]  }
},
{
  path: '/salary/projects/create',
  name: 'SalaryProjectCreate',
  component: () => import('@/views/salary/SalaryProjectCreate.vue'),
  meta: { title: '创建薪酬项目', requiresAuth: true ,roles: [3,]}
},
 {
    path: '/salary/standards',
    name: 'SalaryStandardList',
    component: () => import('@/views/salary/StandardList.vue'),
    meta: { 
      title: '薪酬标准管理', 
      requiresAuth: true,
      // 只需要查看权限即可访问页面
    roles: [1, 2, 3, 4, 5] // 所有经理和专员都可以查看
    }
  },
  {
    path: '/salary/standards/create',
    name: 'SalaryStandardCreate',
    component: () => import('@/views/salary/StandardForm.vue'),
    props: { mode: 'create' },
    meta: { 
      title: '创建薪酬标准', 
      requiresAuth: true,
      roles: [1,3,5] 
    }
  },
  {
    path: '/salary/standards/edit/:id',
    name: 'SalaryStandardEdit',
    component: () => import('@/views/salary/StandardForm.vue'),
    props: { mode: 'edit' },
    meta: { 
      title: '编辑薪酬标准', 
      requiresAuth: true,
      roles: [1,3,5] 
    }
  },

  // 在路由配置中添加
{
  path: '/salary/standards/:id/approval',
  name: 'StandardApprovalDetail',
  component: () => import('@/views/salary/ApprovalDetail.vue'),
  meta: {
    title: '薪酬标准审核',
    requireAuth: true,
    roles: [1,3] // FINANCE_MANAGER
  }
},
{
  path: '/salary/standards/:id/approval-history',
  name: 'ApprovalHistory',
  component: () => import('@/views/salary/ApprovalHistory.vue'),
  meta: {
    title: '审核历史记录',
    requireAuth: true,
    roles: [1, 3, 5] // HR_MANAGER, FINANCE_MANAGER, 
  }
},

{
  path:'/salary/Payment',
  name: 'SalaryPayment',
  component: () => import('@/views/salary/SalaryPayment.vue'),
  meta: { title: '薪酬发放', requiresAuth: true,
       roles: [1,3,5]
     }
},

{
  path:'/salary/payment/create',
  name: 'SalaryPaymentCreate',
  component: () => import('@/views/salary/SalaryPaymentCreate.vue'),
  meta: { title: '薪酬发放表单', requiresAuth: true,
       roles: [1,3,5]
     }
},

{
  path :'/employee/dashboard',
  name:'EmployeeDahsboard',
  component: () => import('@/views/employee/EmployeeDahsboard.vue'),
   meta: { title: '用户个人中心', requiresAuth: true,
       roles: [6]
     }
},

  {
    path: '/hr/archive',
    component: () => import('@/views/archive/ArchiveLayout.vue'),
    children: [
      // 人事经理可见
      {
        path: 'org-pos',
        name: 'OrgPos',
        component: () => import('@/views/archive/OrgPos.vue'),
        meta: {
          roles: [2]
        }
      },
      { path: 'register-check',
        name: 'RegisterCheck',
        component: () => import('@/views/archive/RegisterCheck.vue'),
        meta: {
          roles: [2]
        }
      },
      { path: 'change-check',
        name: 'ChangeCheck',
        component: () => import('@/views/archive/ChangeCheck.vue'),
        meta: {
          roles: [2]
        }
      },

      // 人事专员可见
      {
        path: 'staff-register',
        name: 'StaffRegister',
        component: () => import('@/views/archive/StaffRegister.vue'),
        meta: {
          roles: [4]
        }
      },
      { path: 'staff-change',
        name: 'StaffChange',
        component: () => import('@/views/archive/StaffChange.vue'),
        meta: {
          roles: [4]
        }
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  routes
})

export default router