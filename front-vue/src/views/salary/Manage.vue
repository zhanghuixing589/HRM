<template>
  <div class="salary-manage-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>薪酬管理系统</h2>
      <div class="user-info">
        <span class="username">{{ userInfo.userName || '用户' }}</span>
        <span class="userrole">{{ userRole }}</span>
      </div>
    </div>

    <!-- 功能卡片区域 -->
    <div class="function-cards">
      <!-- 薪酬项目管理卡片 -->
      <el-card class="function-card" shadow="hover">
        <div class="card-header">
          <i class="el-icon-collection card-icon"></i>
          <h3>薪酬项目管理</h3>
        </div>
        <div class="card-content">
          <p>管理薪酬项目的增删改查，设置薪酬计算公式和规则</p>
          <ul class="feature-list">
            <li>• 创建/编辑薪酬项目</li>
            <li>• 设置计算公式</li>
            <li>• 管理项目状态</li>
          </ul>
        </div>
        <div class="card-actions">
          <el-button 
            type="primary" 
            icon="el-icon-s-operation"
            @click="goToProjects"
            size="medium"
          >
            进入管理
          </el-button>
          <el-button 
            type="success" 
            icon="el-icon-plus"
            @click="goToCreateProject"
            size="medium"
          >
            新建项目
          </el-button>
        </div>
      </el-card>

      <!-- 薪酬标准表卡片 -->
      <el-card class="function-card" shadow="hover">
        <div class="card-header">
          <i class="el-icon-document card-icon"></i>
          <h3>薪酬标准表</h3>
        </div>
        <div class="card-content">
          <p>设置不同岗位、级别的薪酬标准，管理薪酬体系</p>
          <ul class="feature-list">
            <li>• 岗位薪酬标准</li>
            <li>• 级别薪酬标准</li>
            <li>• 薪酬体系管理</li>
          </ul>
        </div>
        <div class="card-actions">
          <el-button 
            type="primary" 
            icon="el-icon-s-data"
            @click="goToSalaryStandards"
            size="medium"
          >
            查看标准表
          </el-button>
          <el-button 
            type="warning" 
            icon="el-icon-edit"
            @click="handleEditStandards"
            v-if="userInfo.roleType === 3"  
            size="medium"
          >
            编辑标准
          </el-button>
        </div>
      </el-card>

      <!-- 薪酬发放卡片 -->
      <el-card class="function-card" shadow="hover">
        <div class="card-header">
          <i class="el-icon-shopping-cart-full card-icon"></i>
          <h3>薪酬发放</h3>
        </div>
        <div class="card-content">
          <p>管理员工薪酬发放流程，包括计算、审核和发放</p>
          <ul class="feature-list">
            <li>• 薪酬计算</li>
            <li>• 发放审核</li>
            <li>• 发放记录</li>
          </ul>
        </div>
        <div class="card-actions">
          <el-button 
            type="primary" 
            icon="el-icon-money"
            @click="goToSalaryPayment"
            size="medium"
          >
            薪酬发放
          </el-button>
          <el-button 
            type="info" 
            icon="el-icon-notebook-2"
            @click="goToPaymentHistory"
            size="medium"
          >
            发放记录
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 快捷操作面板 -->
    <div class="quick-actions-panel">
      <el-card shadow="never">
        <div slot="header" class="clearfix">
          <span>快捷操作</span>
        </div>
        <div class="quick-buttons">
          <el-button 
            type="text" 
            icon="el-icon-refresh" 
            @click="refreshData"
          >
            刷新数据
          </el-button>
          <el-button 
            type="text" 
            icon="el-icon-download" 
            @click="exportData"
          >
            导出报表
          </el-button>
          <el-button 
            type="text" 
            icon="el-icon-setting" 
            @click="goToSettings"
          >
            系统设置
          </el-button>
          <el-button 
            type="text" 
            icon="el-icon-question" 
            @click="showHelp"
          >
            帮助文档
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SalaryManage',
  data() {
    return {
      userInfo: {}
    }
  },
  computed: {
    userRole() {
      const roleMap = {
        1: '系统管理员',
        2: '人事经理',
        3: '薪酬经理',
        4: '人事专员',
        5: '薪酬专员',
        6: '普通员工'
      }
      return roleMap[this.userInfo.roleType] || '用户'
    }
  },
  created() {
    this.loadUserInfo()
  },
  methods: {
    // 加载用户信息
    loadUserInfo() {
      const userData = localStorage.getItem('user')
      if (userData) {
        this.userInfo = JSON.parse(userData)
      }
    },
    
    // 跳转到薪酬项目管理
    goToProjects() {
      this.$router.push('/salary/projects')
    },
    
    // 跳转到创建薪酬项目
    goToCreateProject() {
      this.$router.push('/salary/projects/create')
    },
    
    // 跳转到薪酬标准表
    goToSalaryStandards() {
      this.$router.push('/salary/standards')
    },
    
    // 编辑薪酬标准
    handleEditStandards() {
      this.$router.push('/salary/standards/edit')
    },
    
    // 跳转到薪酬发放
    goToSalaryPayment() {
      this.$router.push('/salary/payment')
    },
    
    // 跳转到发放记录
    goToPaymentHistory() {
      this.$router.push('/salary/payment/history')
    },
    
    // 刷新数据
    refreshData() {
      this.$message.success('数据已刷新')
    },
    
    // 导出数据
    exportData() {
      this.$message.info('导出功能开发中')
    },
    
    // 系统设置
    goToSettings() {
      this.$router.push('/salary/settings')
    },
    
    // 显示帮助
    showHelp() {
      this.$message.info('帮助文档开发中')
    }
  }
}
</script>

<style scoped>
.salary-manage-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.page-header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.username {
  font-weight: 500;
  color: #606266;
  margin-bottom: 4px;
}

.userrole {
  font-size: 12px;
  color: #909399;
  background-color: #ecf5ff;
  padding: 2px 8px;
  border-radius: 10px;
}

.function-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.function-card {
  transition: transform 0.3s ease;
  height: 100%;
}

.function-card:hover {
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.card-icon {
  font-size: 24px;
  color: #409EFF;
  margin-right: 10px;
}

.card-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.card-content {
  margin-bottom: 20px;
}

.card-content p {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 15px;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.feature-list li {
  color: #909399;
  padding: 2px 0;
  font-size: 14px;
}

.card-actions {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.quick-actions-panel {
  margin-top: 30px;
}

.quick-buttons {
  display: flex;
  justify-content: space-around;
}

@media (max-width: 768px) {
  .function-cards {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .user-info {
    align-items: flex-start;
  }
  
  .quick-buttons {
    flex-direction: column;
    gap: 10px;
  }
}
</style>