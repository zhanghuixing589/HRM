<!-- EmployeeDashboard.vue -->
<template>
  <div class="employee-dashboard">
    <!-- 页面头部 -->
    <div class="dashboard-header">
      <h1>员工个人中心</h1>
      <div class="header-actions">
        <el-button @click="refreshData" icon="el-icon-refresh">刷新</el-button>
      </div>
    </div>

    <div class="dashboard-content">
      <!-- 个人信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <h2>个人信息</h2>
        </div>
        <div class="card-body">
          <!-- 头像和基本信息 -->
          <div class="basic-info">
            <div class="avatar-section">
              <el-avatar 
                :size="120" 
                :src="employeeInfo.photoPath || '/default-avatar.png'"
                fit="cover"
              ></el-avatar>
              <div class="employee-name">
                <h3>{{ employeeInfo.name }}</h3>
                <el-tag type="success">{{ employeeInfo.positionName }}</el-tag>
              </div>
            </div>
            
            <div class="info-grid">
              <!-- 第一列 -->
              <div class="info-column">
                <div class="info-item">
                  <span class="info-label">员工编号：</span>
                  <span class="info-value">{{ employeeInfo.arcCode }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">性别：</span>
                  <span class="info-value">{{ formatSex(employeeInfo.sex) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">出生日期：</span>
                  <span class="info-value">{{ formatDate(employeeInfo.birDate) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">民族：</span>
                  <span class="info-value">{{ employeeInfo.nationality }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">学历：</span>
                  <span class="info-value">{{ employeeInfo.qualification }}</span>
                </div>
              </div>
              
              <!-- 第二列 -->
              <div class="info-column">
                <div class="info-item">
                  <span class="info-label">身份证号：</span>
                  <span class="info-value">{{ employeeInfo.idCard }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">联系电话：</span>
                  <span class="info-value">{{ employeeInfo.phone }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">邮箱：</span>
                  <span class="info-value">{{ employeeInfo.email }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">住址：</span>
                  <span class="info-value">{{ employeeInfo.address }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">专业：</span>
                  <span class="info-value">{{ employeeInfo.major }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 组织架构信息 -->
          <div class="org-info">
            <h4>组织信息</h4>
            <div class="org-path">
              <el-tag>{{ employeeInfo.firstOrgName }}</el-tag>
              <i class="el-icon-right"></i>
              <el-tag type="info">{{ employeeInfo.secondOrgName }}</el-tag>
              <i class="el-icon-right"></i>
              <el-tag type="warning">{{ employeeInfo.thirdOrgName }}</el-tag>
            </div>
          </div>

          <!-- 薪资信息 -->
          <div class="salary-info">
            <h4>薪资信息</h4>
            <div class="salary-details">
              <div class="salary-item">
                <span class="salary-label">薪资标准：</span>
                <span class="salary-value">{{ employeeInfo.salaryStandardName }}</span>
              </div>
              <div class="salary-item">
                <span class="salary-label">基本工资：</span>
                <span class="salary-value highlight">¥{{ employeeInfo.baseSalary?.toFixed(2) }}</span>
              </div>
              <div class="salary-item">
                <span class="salary-label">职称：</span>
                <span class="salary-value">{{ employeeInfo.title }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 薪酬记录表格 -->
      <div class="salary-card">
        <div class="card-header">
          <h2>薪酬发放记录</h2>
        </div>
        <div class="card-body">
          <el-table
            :data="employeeInfo.recentSalaryRecords"
            style="width: 100%"
            stripe
            v-loading="loading"
          >
            <el-table-column prop="recordCode" label="薪酬编号" width="180" />
            <el-table-column prop="salaryMonth" label="发薪月份" width="120">
              <template #default="{ row }">
                {{ row.salaryMonth }}
              </template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="实发总额" width="120">
              <template #default="{ row }">
                <span class="amount">¥{{ row.totalAmount?.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="releaseDate" label="发放时间" width="120">
              <template #default="{ row }">
                {{ formatDate(row.releaseDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag
                  :type="getStatusType(row.status)"
                  size="small"
                >
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button
                  type="text"
                  size="small"
                  @click="viewSalaryDetail(row)"
                >
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 查看更多按钮 -->
          <div class="view-more">
            <el-button 
              type="primary" 
              plain 
              @click="goToSalaryRecords"
            >
              查看更多记录
            </el-button>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions">
        <h3>快捷操作</h3>
        <div class="action-buttons">
          <el-button 
            type="primary" 
            icon="el-icon-document"
            @click="goToDocuments"
          >
            我的文档
          </el-button>
          <el-button 
            type="success" 
            icon="el-icon-tickets"
            @click="goToLeave"
          >
            请假申请
          </el-button>
          <el-button 
            type="warning" 
            icon="el-icon-bell"
            @click="goToNotifications"
          >
            消息通知
          </el-button>
        </div>
      </div>
    </div>

    <!-- 薪酬详情对话框 -->
    <el-dialog
      title="薪酬详情"
      v-model="detailDialogVisible"
      width="600px"
    >
      <div v-if="selectedRecord">
        <!-- 薪酬详情内容 -->
        <div class="salary-detail">
          <div class="detail-section">
            <h4>基本信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="detail-label">薪酬编号：</span>
                <span class="detail-value">{{ selectedRecord.recordCode }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">发薪月份：</span>
                <span class="detail-value">{{ selectedRecord.salaryMonth }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">发放时间：</span>
                <span class="detail-value">{{ formatDate(selectedRecord.releaseDate) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">状态：</span>
                <el-tag :type="getStatusType(selectedRecord.status)">
                  {{ selectedRecord.status }}
                </el-tag>
              </div>
            </div>
          </div>
          
          <div class="detail-section">
            <h4>金额明细</h4>
            <div class="amount-details">
              <div class="amount-item">
                <span>基本工资：</span>
                <span class="amount-value">¥{{ formatAmount(selectedRecord.baseSalary) }}</span>
              </div>
              <div class="amount-item">
                <span>绩效奖金：</span>
                <span class="amount-value">+ ¥{{ formatAmount(selectedRecord.performanceBonus) }}</span>
              </div>
              <div class="amount-item">
                <span>津贴补助：</span>
                <span class="amount-value">+ ¥{{ formatAmount(selectedRecord.allowance) }}</span>
              </div>
              <div class="amount-item">
                <span>扣款项：</span>
                <span class="amount-value negative">- ¥{{ formatAmount(selectedRecord.deductions) }}</span>
              </div>
              <div class="amount-divider"></div>
              <div class="amount-item total">
                <span>实发总额：</span>
                <span class="amount-value total-amount">¥{{ formatAmount(selectedRecord.totalAmount) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="printSalaryDetail" v-if="selectedRecord?.status === '已发放'">
            打印工资条
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

export default {
  name: 'EmployeeDashboard',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const employeeInfo = ref({
      recentSalaryRecords: []
    })
    const detailDialogVisible = ref(false)
    const selectedRecord = ref(null)

    // 获取员工信息
    const fetchEmployeeInfo = async () => {
      try {
        loading.value = true
        const response = await axios.get('/employee/dashboard/info')
        if (response.data.code === 200) {
          employeeInfo.value = response.data.data
        } else {
          ElMessage.error(response.data.message || '获取信息失败')
        }
      } catch (error) {
        console.error('获取员工信息失败:', error)
        ElMessage.error('网络错误，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    // 格式化日期
    const formatDate = (dateStr) => {
      if (!dateStr) return ''
      return new Date(dateStr).toLocaleDateString('zh-CN')
    }

    // 格式化性别
    const formatSex = (sex) => {
      return sex === 1 ? '男' : sex === 2 ? '女' : '未知'
    }

    // 获取状态标签类型
    const getStatusType = (status) => {
      switch (status) {
        case '已发放': return 'success'
        case '待发放': return 'warning'
        case '已取消': return 'danger'
        default: return 'info'
      }
    }

    // 格式化金额
    const formatAmount = (amount) => {
      return amount ? amount.toFixed(2) : '0.00'
    }

    // 查看薪酬详情
    const viewSalaryDetail = (record) => {
      selectedRecord.value = record
      detailDialogVisible.value = true
    }

    // 打印工资条
    const printSalaryDetail = () => {
      window.print()
    }

    // 跳转到薪酬记录页面
    const goToSalaryRecords = () => {
      router.push('/employee/salary-records')
    }

    // 跳转到文档页面
    const goToDocuments = () => {
      router.push('/employee/documents')
    }

    // 跳转到请假页面
    const goToLeave = () => {
      router.push('/employee/leave-apply')
    }

    // 跳转到通知页面
    const goToNotifications = () => {
      router.push('/employee/notifications')
    }

    // 刷新数据
    const refreshData = () => {
      fetchEmployeeInfo()
    }

    onMounted(() => {
      fetchEmployeeInfo()
    })

    return {
      loading,
      employeeInfo,
      detailDialogVisible,
      selectedRecord,
      formatDate,
      formatSex,
      getStatusType,
      formatAmount,
      viewSalaryDetail,
      printSalaryDetail,
      goToSalaryRecords,
      goToDocuments,
      goToLeave,
      goToNotifications,
      refreshData
    }
  }
}
</script>

<style scoped>
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

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-card, .salary-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.card-body {
  padding: 24px;
}

.basic-info {
  display: flex;
  gap: 40px;
  margin-bottom: 30px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.employee-name {
  text-align: center;
}

.employee-name h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
}

.info-grid {
  flex: 1;
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
  width: 80px;
  color: #909399;
  font-size: 14px;
}

.info-value {
  flex: 1;
  color: #303133;
  font-size: 14px;
}

.org-info, .salary-info {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.org-info h4, .salary-info h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.org-path {
  display: flex;
  align-items: center;
  gap: 12px;
}

.salary-details {
  display: flex;
  gap: 40px;
}

.salary-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.salary-label {
  color: #909399;
}

.salary-value {
  color: #303133;
  font-size: 14px;
}

.salary-value.highlight {
  color: #e6a23c;
  font-weight: bold;
  font-size: 16px;
}

.amount {
  color: #67c23a;
  font-weight: bold;
}

.view-more {
  margin-top: 20px;
  text-align: center;
}

.quick-actions {
  background: white;
  border-radius: 8px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.quick-actions h3 {
  margin: 0 0 20px 0;
  color: #303133;
  font-size: 18px;
}

.action-buttons {
  display: flex;
  gap: 16px;
}

/* 薪酬详情样式 */
.salary-detail {
  padding: 10px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-label {
  color: #909399;
  font-size: 14px;
}

.detail-value {
  color: #303133;
  font-size: 14px;
}

.amount-details {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed #ebeef5;
}

.amount-item:last-child {
  border-bottom: none;
}

.amount-item.total {
  border-top: 2px solid #409eff;
  margin-top: 8px;
  padding-top: 16px;
}

.amount-value {
  font-weight: 500;
}

.amount-value.negative {
  color: #f56c6c;
}

.amount-value.total-amount {
  color: #67c23a;
  font-size: 18px;
  font-weight: bold;
}

.amount-divider {
  height: 1px;
  background: #ebeef5;
  margin: 8px 0;
}

@media print {
  .employee-dashboard > *:not(.salary-detail) {
    display: none !important;
  }
  .salary-detail {
    padding: 0;
    margin: 0;
  }
  .dialog-footer {
    display: none !important;
  }
}
</style>