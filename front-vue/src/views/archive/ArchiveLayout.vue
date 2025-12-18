<template>
  <div class="archive-wrap">
    <header class="top-bar">
      <span class="logo">HRM 人事档案管理</span>
      <span class="user">欢迎，{{ user.userName }}</span>
      <el-button type="text" @click="logout">退出</el-button>
    </header>

    <section class="middle">
      <aside class="side">
        <el-menu 
          :default-active="activeMenu" 
          router 
          unique-opened
          @select="handleMenuSelect"
        >
          <!-- 管理员菜单 -->
          <template v-if="user.roleType === 1">
            <el-menu-item index="/admin/user-role">
              <i class="el-icon-user-solid"></i>
              <span slot="title">员工角色管理</span>
            </el-menu-item>
          </template>

          <!-- 人事经理菜单 -->
          <template v-if="user.roleType === 2">
            <el-menu-item index="/hr/archive/org-pos">
              <i class="el-icon-office-building"></i>
              <span slot="title">机构/职位设置</span>
            </el-menu-item>
            <el-menu-item index="/hr/archive/register-check">
              <i class="el-icon-document-checked"></i>
              <span slot="title">档案登记复核</span>
            </el-menu-item>
            <el-menu-item index="/hr/archive/change-check">
              <i class="el-icon-edit-outline"></i>
              <span slot="title">档案变更复核</span>
            </el-menu-item>
            <el-menu-item index="/hr/archive/archive-query">
              <i class="el-icon-search"></i>
              <span slot="title">档案查询</span>
            </el-menu-item>
          </template>

          <!-- 人事专员菜单 -->
          <template v-if="user.roleType === 4">
            <el-menu-item index="/hr/archive/staff-register">
              <i class="el-icon-document-add"></i>
              <span slot="title">档案登记</span>
            </el-menu-item>
            <el-menu-item index="/hr/archive/my-archives">
              <i class="el-icon-tickets"></i>
              <span slot="title">我的提交记录</span>
            </el-menu-item>
            <el-menu-item index="/hr/archive/staff-change">
              <i class="el-icon-edit"></i>
              <span slot="title">档案变更申请</span>
            </el-menu-item>
            <el-menu-item index="/hr/archive/my-change-records">
              <i class="el-icon-edit"></i>
              <span slot="title">我的变更申请记录</span>
            </el-menu-item>
            <el-menu-item index="/hr/archive/archive-query">
                <i class="el-icon-search"></i>
                <span slot="title">档案查询</span>
              </el-menu-item>
          </template>
        </el-menu>
      </aside>

      <main class="content">
        <router-view />
      </main>
    </section>

    <footer class="bottom-bar">
      © 2025 HRM System
    </footer>
  </div>
</template>

<script>
export default {
  name: 'ArchiveLayout',
  data() {
    return {
      activeMenu: ''
    }
  },
  computed: {
    user() { 
      return JSON.parse(localStorage.getItem('user') || '{}') 
    },
    roleText() {
      const map = { 2: '人事经理', 4: '人事专员' }
      return map[this.user.roleType] || '未知角色'
    }
  },
  created() {
    this.setDefaultActiveMenu()
  },
  watch: {
    '$route.path': {
      immediate: true,
      handler(newPath) {
        this.activeMenu = newPath
      }
    }
  },
  methods: {
    setDefaultActiveMenu() {
      const path = this.$route.path
      
      // 如果当前路径就是布局路径，重定向到默认页面
      if (path === '/hr/archive' || path === '/hr/archive/') {
        if (this.user.roleType === 2) {
          this.$router.replace('/hr/archive/org-pos')
        } else if (this.user.roleType === 4) {
          this.$router.replace('/hr/archive/staff-register')
        }
      } else {
        this.activeMenu = path
      }
    },
    
    handleMenuSelect(index) {
      this.activeMenu = index
    },
    
    logout() {
      localStorage.clear()
      this.$router.replace('/')
    }
  }
}
</script>

<style scoped lang="scss">
.archive-wrap {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f3f3f3;
  
  .top-bar {
    height: 60px;
    line-height: 60px;
    background: #fff;
    padding: 0 20px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    display: flex;
    align-items: center;
    
    .logo {
      font-size: 18px;
      color: #409EFF;
      font-weight: bold;
      flex: 1;
    }
    
    .user {
      margin-right: 15px;
      color: #666;
    }
  }
  
  .middle {
    flex: 1;
    display: flex;
    overflow: hidden;
  }
  
  .side {
    width: 220px;
    background: #fff;
    box-shadow: 2px 0 4px rgba(0, 0, 0, 0.05);
    
    .el-menu {
      border-right: none;
      height: 100%;
      
      .el-menu-item {
        height: 48px;
        line-height: 48px;
        
        &.is-active {
          background-color: #ecf5ff;
          color: #409EFF;
        }
        
        i {
          margin-right: 8px;
        }
      }
    }
  }
  
  .content {
    flex: 1;
    overflow: auto;
  }
  
  .bottom-bar {
    height: 40px;
    line-height: 40px;
    text-align: center;
    background: #fff;
    border-top: 1px solid #e6e6e6;
    font-size: 12px;
    color: #999;
  }
}
</style>