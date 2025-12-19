<template>
  <div class="employee-dashboard">
    <!-- 页面头部 -->
    <div class="dashboard-header">
      <h1>员工个人中心</h1>
      <div class="header-actions">
        <button class="refresh-btn" @click="refreshData">
          <i class="refresh-icon">↻</i>刷新
        </button>
      </div>
    </div>

    <div class="dashboard-content">
      <!-- 加载中状态 -->
      <div v-if="loading" class="loading">
        <div class="loading-icon">⏳</div>
        <p>加载员工信息中...</p>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="error" class="error">
        <div class="error-icon">❌</div>
        <p>{{ error }}</p>
        <button class="retry-btn" @click="refreshData">重试</button>
      </div>

      <!-- 用户信息卡片 -->
      <div v-else-if="userInfo" class="info-card">
        <div class="card-header">
          <h2>个人信息</h2>
          <div class="header-badges">
            <span class="status-badge" :class="getStatusClass(userInfo.status)">
              {{ getStatusText(userInfo.status) }}
            </span>
            <span class="archive-status" :class="getArchiveStatusClass(userInfo.archiveStatus)" v-if="userInfo.archiveStatus">
              {{ getArchiveStatusText(userInfo.archiveStatus) }}
            </span>
          </div>
        </div>
        <div class="card-body">
          <!-- 头像和基本信息 -->
          <div class="basic-info">
            <div class="avatar-section">
              <div class="avatar">
                <div class="avatar-initial">
                  {{ getInitial(userInfo.userName || userInfo.name) }}
                </div>
              </div>
              <div class="user-name">
                <h3>{{ userInfo.userName || userInfo.name || '用户' }}</h3>
                <div class="user-tags">
                  <span class="role-tag">{{ getUserRole(userInfo.roleType) }}</span>
                  <span class="code-tag" v-if="userInfo.userCode">工号：{{ userInfo.userCode }}</span>
                  <span class="code-tag" v-if="userInfo.arcCode">档案：{{ userInfo.arcCode }}</span>
                </div>
              </div>
            </div>
            
            <!-- 用户表信息 -->
            <div class="section">
              <h4>用户信息</h4>
              <div class="info-grid">
                <div class="info-column">
                  <div class="info-item">
                    <span class="info-label">用户ID：</span>
                    <span class="info-value">{{ userInfo.userId || '未设置' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">邮箱：</span>
                    <span class="info-value">{{ userInfo.email || '未设置' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">手机号：</span>
                    <span class="info-value">{{ userInfo.phone || '未设置' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">所属机构：</span>
                    <span class="info-value">{{ userInfo.orgName || '未设置' }}</span>
                  </div>
                </div>
                
                <div class="info-column">
                  <div class="info-item">
                    <span class="info-label">入职日期：</span>
                    <span class="info-value">{{ formatDate(userInfo.entryDate) }}</span>
                  </div>
                  <div class="info-item" v-if="userInfo.leaveDate">
                    <span class="info-label">离职日期：</span>
                    <span class="info-value">{{ formatDate(userInfo.leaveDate) }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">档案ID：</span>
                    <span class="info-value">{{ userInfo.archiveId || '未关联' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">职位：</span>
                    <span class="info-value">
                      {{ getPositionDisplay(userInfo) }}
                      <span v-if="!positionLoading && !userInfo.posId" class="no-data">未设置</span>
                      <span v-if="positionLoading" class="loading-position">加载中...</span>
                      <span v-if="positionError" class="position-error">加载失败</span>
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 档案表信息（如果有） -->
            <div class="section" v-if="userInfo.arcId">
              <h4>档案信息</h4>
              <div class="info-grid">
                <!-- ... 档案信息保持不变 ... -->
              </div>
            </div>

            <!-- 组织架构信息 -->
            <div class="section" v-if="userInfo.firstOrgName || userInfo.secondOrgName || userInfo.thirdOrgName">
              <h4>组织架构</h4>
              <div class="org-path">
                <template v-if="userInfo.firstOrgName">
                  <span class="org-tag first">{{ userInfo.firstOrgName }}</span>
                  <i class="arrow-icon">→</i>
                </template>
                <template v-if="userInfo.secondOrgName">
                  <span class="org-tag second">{{ userInfo.secondOrgName }}</span>
                  <i class="arrow-icon">→</i>
                </template>
                <span class="org-tag third">{{ userInfo.thirdOrgName || userInfo.orgName || '未设置' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 时间信息 -->
      <div class="time-info" v-if="userInfo && (userInfo.createTime || userInfo.updateTime)">
        <div class="time-item">
          <span class="time-label">创建时间：</span>
          <span class="time-value">{{ formatDateTime(userInfo.createTime) }}</span>
        </div>
        <div class="time-item">
          <span class="time-label">更新时间：</span>
          <span class="time-value">{{ formatDateTime(userInfo.updateTime) }}</span>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions">
        <h3>快捷操作</h3>
        <div class="action-buttons">
          <button class="action-btn primary" @click="goToEdit">
            <i class="btn-icon">✏️</i>编辑信息
          </button>
          <button class="action-btn success" @click="goToChangePassword">
            <i class="btn-icon">🔒</i>修改密码
          </button>
          <button class="action-btn info" @click="goToArchive" v-if="userInfo && userInfo.arcId">
            <i class="btn-icon">📄</i>查看档案
          </button>
          <button class="action-btn warning" @click="logout">
            <i class="btn-icon">🚪</i>退出登录
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { userApi } from '@/api'  // 导入API

export default {
  name: 'EmployeeDashboard',
  
 data() {
    return {
      loading: true,
      error: null,
      userInfo: null,
      positionName: null,      // 职位名称
      positionLoading: false,  // 职位加载状态
      positionError: null,     // 职位加载错误
      positionCache: {}        // 职位名称缓存
    }
  },
  
  mounted() {
    this.loadCurrentUserInfo()
  },
  
  computed: {
    // 或者使用计算属性来获取用户角色
    computedUserRole() {
      const roleMap = {
        1: '系统管理员',
        2: '人事经理',
        3: '薪酬经理',
        4: '人事专员',
        5: '薪酬专员',
        6: '普通员工'
      }
      return roleMap[this.userInfo?.roleType] || '用户'
    }
  },
  
  methods: {
     // 加载当前用户信息 - 使用API
    async loadCurrentUserInfo() {
      try {
        this.loading = true
        this.error = null
        
        console.log('开始加载用户信息...')
        
        // 使用API调用
        const response = await userApi.getCurrentUser()
        
        if (response.code === 200) {
          this.userInfo = response.data
          console.log('获取的用户信息:', this.userInfo)
          
          // 加载职位名称
          if (this.userInfo.posId) {
            await this.loadPositionName(this.userInfo.posId)
          }
        } else {
          throw new Error(response.message || '获取用户信息失败')
        }
        
      } catch (error) {
        console.error('加载用户信息失败:', error)
        this.error = this.formatError(error)
        await this.loadFromLocalStorage()
      } finally {
        this.loading = false
      }
    },

    // 从localStorage加载
    async loadFromLocalStorage() {
      try {
        const storedUser = localStorage.getItem('user')
        if (storedUser) {
          const userData = JSON.parse(storedUser)
          console.log('从localStorage加载用户信息:', userData)
          
          // 如果有用户ID，尝试通过ID获取完整信息
          if (userData.userId || userData.user_id) {
            try {
              const userId = userData.userId || userData.user_id
              const response = await userApi.getUserById(userId)
              
              if (response.code === 200) {
                this.userInfo = response.data
                // 加载职位名称
                if (this.userInfo.posId) {
                  await this.loadPositionName(this.userInfo.posId)
                }
                return
              }
            } catch (apiError) {
              console.warn('通过ID获取用户信息失败:', apiError)
            }
          }
          
          // 如果有工号，尝试通过工号获取
          if (userData.userCode || userData.user_code) {
            try {
              const userCode = userData.userCode || userData.user_code
              const response = await userApi.getUserByCode(userCode)
              
              if (response.code === 200) {
                this.userInfo = response.data
                // 加载职位名称
                if (this.userInfo.posId) {
                  await this.loadPositionName(this.userInfo.posId)
                }
                return
              }
            } catch (apiError) {
              console.warn('通过工号获取用户信息失败:', apiError)
            }
          }
          
          // 如果API都失败，使用本地数据
          this.userInfo = {
            userId: userData.user_id || userData.userId,
            userCode: userData.user_code || userData.userCode,
            userName: userData.user_name || userData.userName,
            email: userData.email,
            phone: userData.phone,
            roleType: userData.role_type || userData.roleType,
            status: userData.status,
            archiveId: userData.arc_id || userData.archiveId,
            entryDate: userData.entry_date || userData.entryDate,
            createTime: userData.create_time || userData.createTime,
            updateTime: userData.update_time || userData.updateTime,
            posId: userData.pos_id || userData.posId
          }
          
          // 加载职位名称
          if (this.userInfo.posId) {
            await this.loadPositionName(this.userInfo.posId)
          }
        } else {
          this.error = '用户未登录'
        }
      } catch (error) {
        console.error('从localStorage加载失败:', error)
        this.error = '加载用户信息失败'
      }
    },

     // 加载职位名称
    async loadPositionName(posId) {
      if (!posId) {
        this.positionName = null
        return
      }
      
      // 检查缓存
      if (this.positionCache[posId]) {
        this.positionName = this.positionCache[posId]
        return
      }
      
      try {
        this.positionLoading = true
        this.positionError = null
        
        console.log('加载职位名称，职位ID:', posId)
        
        // 使用职位API获取职位名称
        const response = await userApi.getPositionNameById(posId)
        
     // 根据实际返回的数据结构调整
    if (response.data.code === 200 && response.data.success) {
      // 如果返回的是包装格式
      this.positionName = response.data.data.posName;
    } else if (response.data.posName) {
      // 如果直接返回职位对象
      this.positionName = response.data.posName;
    } else {
      throw new Error("职位数据格式错误");
    }
    
    console.log("获取的职位名称:", this.positionName);
      } catch (error) {
        this.positionError = this.formatError(error)
        console.error('加载职位名称失败:', error)
      } finally {
        this.positionLoading = false
      }
    },
    
    // 获取职位显示文本
    getPositionDisplay(userInfo) {
      if (!userInfo) return ''
      
      // 如果已经有职位名称，直接显示
      if (this.positionName) {
        return `${this.positionName} (ID: ${userInfo.posId})`
      }
      
      // 如果职位ID存在但名称还没加载完
      if (userInfo.posId && (this.positionLoading || this.positionError)) {
        return ''
      }
      
      // 如果没有职位ID
      if (!userInfo.posId) {
        return ''
      }
      
      // 其他情况显示ID
      return `ID: ${userInfo.posId}`
    },

        // 修改密码
    async goToChangePassword() {
      try {
        // 显示密码修改表单
        const newPassword = prompt('请输入新密码:')
        if (!newPassword) return
        
        const confirmPassword = prompt('请确认新密码:')
        if (!confirmPassword) return
        
        if (newPassword !== confirmPassword) {
          alert('两次输入的密码不一致')
          return
        }
        
        const data = {
          oldPassword: '', // 需要先获取旧密码
          newPassword: newPassword,
          confirmPassword: confirmPassword
        }
        
        const response = await userApi.changePassword(data)
        if (response.code === 200) {
          alert('密码修改成功')
        } else {
          alert(response.message || '密码修改失败')
        }
      } catch (error) {
        console.error('修改密码失败:', error)
        alert('修改密码失败: ' + this.formatError(error))
      }
    },
    
    // 查看档案详情
    async goToArchive() {
      if (!this.userInfo || !this.userInfo.archiveId) {
        alert('该用户没有关联档案')
        return
      }
      
      try {
        const response = await userApi.getUserArchive(this.userInfo.archiveId)
        if (response.code === 200) {
          // 这里可以跳转到档案详情页或者显示档案信息
          console.log('档案详情:', response.data)
          alert(`档案详情:\n${JSON.stringify(response.data, null, 2)}`)
        }
      } catch (error) {
        console.error('获取档案详情失败:', error)
        alert('获取档案详情失败')
      }
    },
    
    // 测试API连接
    async testApiConnection() {
      try {
        const response = await userApi.testApiConnection()
        alert(`API连接正常: ${response.message}`)
      } catch (error) {
        alert(`API连接失败: ${this.formatError(error)}`)
      }
    },
    
    // 格式化错误信息
    formatError(error) {
      if (typeof error === 'string') return error
      if (error.message) return error.message
      if (error.msg) return error.msg
      return '未知错误'
    },
    
    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return '未设置'
      try {
        const date = new Date(dateStr)
        return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
      } catch (e) {
        console.warn('日期格式化失败:', dateStr, e)
        return dateStr || '未设置'
      }
    },
    
    // 格式化日期时间
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return '未设置'
      try {
        const date = new Date(dateTimeStr)
        return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
      } catch (e) {
        console.warn('日期时间格式化失败:', dateTimeStr, e)
        return dateTimeStr || '未设置'
      }
    },
    
    // 获取姓名首字母
    getInitial(name) {
      if (!name) return 'U'
      return name.charAt(0).toUpperCase()
    },
    
    // 格式化身份证号（保护隐私）
    formatIdCard(idCard) {
      if (!idCard) return '未设置'
      if (idCard.length !== 18) return idCard
      return idCard.substring(0, 6) + '********' + idCard.substring(14)
    },
    
    // 获取状态文本
    getStatusText(status) {
      if (status == null) return '未知'
      switch (status) {
        case 1: return '在职'
        case 0: return '离职'
        case 2: return '禁用'
        default: return '未知'
      }
    },
    
    // 获取档案状态文本
    getArchiveStatusText(status) {
      if (status == null) return '未知'
      switch (status) {
        case 1: return '待复核'
        case 2: return '已通过'
        case 3: return '已驳回'
        case 4: return '重新提交待审核'
        case 0: return '已删除'
        default: return '未知'
      }
    },
    
    // 获取性别文本
    getSexText(sex) {
      if (sex == null) return '未设置'
      return sex === 1 ? '男' : sex === 2 ? '女' : '未知'
    },
    
    // 获取用户角色
    getUserRole(roleType) {
      const roleMap = {
        1: '系统管理员',
        2: '人事经理',
        3: '薪酬经理',
        4: '人事专员',
        5: '薪酬专员',
        6: '普通员工'
      }
      return roleMap[roleType] || '用户'
    },
    
    // 获取状态样式类
    getStatusClass(status) {
      switch (status) {
        case 1: return 'status-online'   // 在职
        case 0: return 'status-offline'  // 离职
        case 2: return 'status-disable'  // 禁用
        default: return 'status-unknown'
      }
    },
    
    // 获取档案状态样式类
    getArchiveStatusClass(status) {
      switch (status) {
        case 1: return 'status-pending'   // 待复核
        case 2: return 'status-approved'  // 已通过
        case 3: return 'status-rejected'  // 已驳回
        case 4: return 'status-resubmit'  // 重新提交
        case 0: return 'status-deleted'   // 已删除
        default: return 'status-unknown'
      }
    },
    
    // 刷新数据
    refreshData() {
      this.loadCurrentUserInfo()
    },
    
    // 编辑信息
    goToEdit() {
      if (this.userInfo && this.userInfo.arcId) {
        this.$router.push(`/archive/edit/${this.userInfo.arcCode}`)
      } else {
        this.$router.push('/user/edit')
      }
    },
    
 
    
    // 退出登录 - 添加这个方法
    logout() {
      console.log('退出登录')
      localStorage.removeItem('user')
      localStorage.removeItem('token')
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
/* 基础样式 */
.employee-dashboard {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.dashboard-header h1 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

/* 按钮样式 */
.refresh-btn {
  padding: 8px 16px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: background-color 0.3s;
}

.refresh-btn:hover {
  background: #66b1ff;
}

.refresh-icon {
  font-size: 14px;
}

/* 加载状态 */
.loading {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.loading-icon {
  font-size: 48px;
  margin-bottom: 16px;
  animation: spin 2s linear infinite;
}

.loading p {
  color: #909399;
  font-size: 14px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 错误状态 */
.error {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #f56c6c;
}

.error p {
  color: #f56c6c;
  font-size: 16px;
  margin-bottom: 20px;
}

.retry-btn {
  padding: 8px 20px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.retry-btn:hover {
  background: #66b1ff;
}

/* 卡片样式 */
.info-card, .quick-actions {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.header-badges {
  display: flex;
  gap: 8px;
}

/* 状态徽章 */
.status-badge, .archive-status {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-online {
  background: #f0f9eb;
  color: #67c23a;
}

.status-offline {
  background: #fef0f0;
  color: #f56c6c;
}

.status-disable {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-unknown {
  background: #f4f4f5;
  color: #909399;
}

.status-pending {
  background: #fdf6ec;
  color: #e6a23c;
  border: 1px solid #f5dab1;
}

.status-approved {
  background: #f0f9eb;
  color: #67c23a;
  border: 1px solid #c2e7b0;
}

.status-rejected {
  background: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fbc4c4;
}

.status-resubmit {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.status-deleted {
  background: #f6f6f6;
  color: #8c8c8c;
  border: 1px solid #d9d9d9;
}

.card-body {
  padding: 24px;
}

/* 头像区域 */
.basic-info {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.avatar-section {
  display: flex;
  gap: 24px;
  align-items: center;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.avatar-initial {
  font-size: 36px;
  color: white;
  font-weight: bold;
}

.user-name {
  flex: 1;
}

.user-name h3 {
  margin: 0 0 12px 0;
  font-size: 24px;
  color: #303133;
}

/* 职位相关的样式 */
.no-data {
  color: #909399;
  font-style: italic;
}

.loading-position {
  color: #e6a23c;
  font-size: 12px;
  margin-left: 8px;
}

.position-error {
  color: #f56c6c;
  font-size: 12px;
  margin-left: 8px;
}


.user-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.role-tag, .code-tag {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.role-tag {
  background: #67c23a;
  color: white;
}

.code-tag {
  background: #f4f4f5;
  color: #909399;
}

/* 信息网格 */
.section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.section h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.info-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
}

.info-label {
  width: 100px;
  color: #909399;
  font-size: 14px;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  color: #303133;
  font-size: 14px;
  word-break: break-word;
}

/* 组织架构 */
.org-path {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.org-tag {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
}

.org-tag.first {
  background: #409eff;
  color: white;
}

.org-tag.second {
  background: #909399;
  color: white;
}

.org-tag.third {
  background: #e6a23c;
  color: white;
}

.arrow-icon {
  color: #c0c4cc;
}

/* 时间信息 */
.time-info {
  display: flex;
  gap: 20px;
  background: white;
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-top: 20px;
}

.time-item {
  display: flex;
  gap: 8px;
}

.time-label {
  color: #909399;
  font-size: 14px;
}

.time-value {
  color: #606266;
  font-size: 14px;
}

/* 快捷操作按钮 */
.quick-actions {
  margin-top: 24px;
  padding: 20px 24px;
}

.quick-actions h3 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 18px;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.action-btn.primary {
  background: #409eff;
  color: white;
}

.action-btn.primary:hover {
  background: #66b1ff;
}

.action-btn.success {
  background: #67c23a;
  color: white;
}

.action-btn.success:hover {
  background: #85ce61;
}

.action-btn.warning {
  background: #e6a23c;
  color: white;
}

.action-btn.warning:hover {
  background: #ebb563;
}

.action-btn.info {
  background: #909399;
  color: white;
}

.action-btn.info:hover {
  background: #a6a9ad;
}

.btn-icon {
  font-size: 24px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .avatar-section {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .action-buttons {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .time-info {
    flex-direction: column;
    gap: 12px;
  }
  
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

@media (max-width: 480px) {
  .action-buttons {
    grid-template-columns: 1fr;
  }
  
  .header-badges {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .info-label {
    width: 80px;
  }
}
</style>