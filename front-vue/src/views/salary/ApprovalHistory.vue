<template>
  <div class="approval-history-page">
    <!-- 加载遮罩 -->
    <div v-if="loading" class="loading-overlay">
      <el-card shadow="never" style="padding: 30px; text-align: center;">
        <div style="margin-bottom: 20px;">
          <i class="el-icon-loading" style="font-size: 40px; color: #409EFF;"></i>
        </div>
        <h3>正在加载审核历史...</h3>
        <p>请稍候，正在获取历史记录</p>
      </el-card>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2>审核历史记录</h2>
        <div class="standard-info">
          <span class="standard-code">标准编码：{{ standardCode }}</span>
          <span class="standard-name">{{ standardName }}</span>
          <el-tag :type="getStatusTagType(currentStatus)">
            {{ getStatusText(currentStatus) }}
          </el-tag>
        </div>
      </div>
      <div class="header-actions">
        <el-button icon="el-icon-back" @click="handleBack">
          返回
        </el-button>
        <el-button icon="el-icon-refresh" @click="loadApprovalHistory">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 审核历史时间线 -->
    <div v-if="!loading && approvalHistory.length > 0" class="history-content">
      <el-timeline>
        <el-timeline-item
          v-for="approval in approvalHistory"
          :key="approval.id"
          :timestamp="formatDateTime(approval.submitTime)"
          placement="top"
        >
          <el-card class="approval-card">
            <div class="approval-header">
              <div class="approval-status">
                <el-tag 
                  :type="getApprovalStatusTagType(approval.approvalStatus)" 
                  size="medium"
                  effect="dark"
                >
                  {{ getApprovalStatusText(approval.approvalStatus) }}
                </el-tag>
                <span class="approval-submitter">提交人：{{ approval.submitterName }}</span>
              </div>
              <div class="approval-time">
                <span v-if="approval.approvalTime">
                  审核时间：{{ formatDateTime(approval.approvalTime) }}
                </span>
                <span v-else style="color: #909399;">待审核</span>
              </div>
            </div>

            <div class="approval-info">
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="info-item">
                    <label>审核人：</label>
                    <span>{{ approval.approverName || '--' }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="info-item">
                    <label>审核状态：</label>
                    <el-tag 
                      :type="getApprovalStatusTagType(approval.approvalStatus)" 
                      size="small"
                    >
                      {{ getApprovalStatusText(approval.approvalStatus) }}
                    </el-tag>
                  </div>
                </el-col>
              </el-row>
              
              <div v-if="approval.approvalOpinion" class="opinion-section">
                <label>审核意见：</label>
                <div class="opinion-content">
                  {{ approval.approvalOpinion }}
                </div>
              </div>

              <!-- 操作日志 -->
              <div v-if="approval.logs && approval.logs.length > 0" class="logs-section">
                <div class="logs-header">
                  <span>操作记录</span>
                  <el-tag size="mini" type="info">
                    {{ approval.logs.length }} 条
                  </el-tag>
                </div>
                <div class="logs-content">
                  <div 
                    v-for="log in approval.logs" 
                    :key="log.id" 
                    class="log-item"
                  >
                    <div class="log-time">
                      {{ formatDateTime(log.operationTime) }}
                    </div>
                    <div class="log-detail">
                      <span class="log-operator">{{ log.operatorName }}</span>
                      <el-tag 
                        :type="getLogTypeTagType(log.operationType)" 
                        size="mini"
                      >
                        {{ getOperationTypeText(log.operationType) }}
                      </el-tag>
                      <span class="log-content">{{ log.operationContent }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading" class="empty-state">
      <el-card shadow="never" style="padding: 40px; text-align: center;">
        <i class="el-icon-document" style="font-size: 60px; color: #c0c4cc;"></i>
        <h3>暂无审核历史</h3>
        <p>该标准尚未有审核记录</p>
        <el-button type="primary" @click="handleBack" style="margin-top: 20px;">
          返回标准列表
        </el-button>
      </el-card>
    </div>
  </div>
</template>

<script>
import { getDetailedApprovalHistory } from '@/api/salary/standards'
import { getStandardDetail } from '@/api/salary/standards'

export default {
  name: 'ApprovalHistory',
  data() {
    return {
      loading: false,
      standardId: null,
      standardCode: '',
      standardName: '',
      currentStatus: null,
      approvalHistory: []
    }
  },
  
  created() {
    this.standardId = this.$route.params.id
    this.loadData()
  },
  
  methods: {
    // 加载数据
    async loadData() {
      try {
        this.loading = true
        
        // 并行加载标准信息和审核历史
        await Promise.all([
          this.loadStandardInfo(),
          this.loadApprovalHistory()
        ])
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('加载数据失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    // 加载标准信息
    async loadStandardInfo() {
      try {
        const response = await getStandardDetail(this.standardId)
        if (response && response.success) {
          this.standardCode = response.data.standardCode
          this.standardName = response.data.standardName
          this.currentStatus = response.data.status
        }
      } catch (error) {
        console.error('加载标准信息失败:', error)
      }
    },
    
    // 加载审核历史
    async loadApprovalHistory() {
      try {
        const response = await getDetailedApprovalHistory(this.standardId)
        if (response && response.success) {
          this.approvalHistory = response.data
          console.log('审核历史加载成功:', this.approvalHistory)
        } else {
          throw new Error(response?.message || '加载失败')
        }
      } catch (error) {
        console.error('加载审核历史失败:', error)
        this.$message.error('加载审核历史失败: ' + error.message)
      }
    },
    
    // 处理返回
    handleBack() {
      this.$router.go(-1)
    },
    
    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      return new Date(dateTime).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },
    
    // 获取标准状态标签类型
    getStatusTagType(status) {
      switch (status) {
        case 1: return 'info'    // 草稿
        case 2: return 'warning' // 待审核
        case 3: return 'success' // 已生效
        case 0: return 'danger'  // 驳回
        default: return ''
      }
    },
    
    // 获取标准状态文本
    getStatusText(status) {
      switch (status) {
        case 1: return '草稿'
        case 2: return '待审核'
        case 3: return '已生效'
        case 0: return '已驳回'
        default: return '未知'
      }
    },
    
    // 获取审核状态标签类型
    getApprovalStatusTagType(approvalStatus) {
      switch (approvalStatus) {
        case 2: return 'primary'  // 待审核
        case 3: return 'success'  // 通过
        case 0: return 'danger'   // 驳回
        default: return 'info'
      }
    },
    
    // 获取审核状态文本
    getApprovalStatusText(approvalStatus) {
      switch (approvalStatus) {
        case 2: return '待审核'
        case 3: return '已通过'
        case 0: return '已驳回'
        default: return '未知'
      }
    },
    
    // 获取操作类型文本
    getOperationTypeText(operationType) {
      const typeMap = {
        'Submit': '提交审核',
        'Approve': '审核通过',
        'Reject': '审核驳回',
        'Recall': '撤回提交',
        'Update': '更新',
        'Create': '创建'
      }
      return typeMap[operationType] || operationType
    },
    
    // 获取日志类型标签
    getLogTypeTagType(operationType) {
      const typeMap = {
        'Submit': 'primary',
        'Approve': 'success',
        'Reject': 'danger',
        'Recall': 'warning',
        'Update': 'info',
        'Create': ''
      }
      return typeMap[operationType] || 'info'
    }
  }
}
</script>

<style scoped>
.approval-history-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.page-header {
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-left h2 {
  margin: 0 0 15px 0;
  color: #303133;
}

.standard-info {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.standard-code {
  color: #606266;
  font-weight: 500;
}

.standard-name {
  color: #409EFF;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* 时间线样式 */
.history-content {
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 审核卡片样式 */
.approval-card {
  margin: 10px 0;
  border-radius: 8px;
}

.approval-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.approval-status {
  display: flex;
  align-items: center;
  gap: 15px;
}

.approval-submitter {
  color: #606266;
  font-size: 14px;
}

.approval-time {
  color: #909399;
  font-size: 13px;
}

/* 信息项样式 */
.info-item {
  margin: 10px 0;
  padding: 8px 0;
}

.info-item label {
  display: inline-block;
  width: 80px;
  color: #909399;
  font-weight: 500;
}

/* 意见区域 */
.opinion-section {
  margin-top: 15px;
}

.opinion-section label {
  display: block;
  color: #909399;
  font-weight: 500;
  margin-bottom: 8px;
}

.opinion-content {
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border-left: 4px solid #67C23A;
  color: #606266;
  line-height: 1.6;
}

/* 日志区域 */
.logs-section {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
}

.logs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #dcdfe6;
}

.logs-header span {
  font-weight: 600;
  color: #303133;
}

.log-item {
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  color: #909399;
  font-size: 12px;
  margin-bottom: 5px;
}

.log-detail {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.log-operator {
  color: #409EFF;
  font-weight: 500;
}

.log-content {
  color: #606266;
  flex: 1;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-state h3 {
  margin: 20px 0 10px 0;
  color: #909399;
}

.empty-state p {
  color: #c0c4cc;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .approval-history-page {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 15px;
    padding: 15px;
  }
  
  .standard-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .approval-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .log-detail {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
}
</style>