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

  // 系统管理员
  {
    path: '/admin/user-role',
    component: () => import('@/views/admin/UserRoleManagement.vue'),
    meta: { 
      title: '员工角色管理',
      requiresAuth: true,
      roles: [1]
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
      {
        path: 'my-archives',
        name: 'MyArchives',
        component: () => import('@/views/archive/MyArchives.vue'),
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
      },
      {
        path: 'staff-change/:id',
        name: 'StaffChangeWithId',
        component: () => import('@/views/archive/StaffChange.vue'),
        meta: {
          roles: [4]
        }
      },
      {
        path: 'my-change-records',
        name: 'MyChangeRecords',
        component: () => import('@/views/archive/MyChangeRecords.vue'),
        meta: {
          roles: [4]
        }
      },

      // 档案查询页面（人事专员和经理都可以访问）
      {
        path: 'archive-query',
        name: 'ArchiveQuery',
        component: () => import('@/views/archive/ArchiveQuery.vue'),
        meta: {
          roles: [2, 4] // 人事经理和人事专员都可以访问
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