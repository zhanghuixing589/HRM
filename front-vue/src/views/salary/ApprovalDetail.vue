<template>
  <div class="approval-detail-page">
    <!-- 加载遮罩 -->
    <div v-if="loading" class="loading-overlay">
      <el-card shadow="never" style="padding: 30px; text-align: center;">
        <div style="margin-bottom: 20px;">
          <i class="el-icon-loading" style="font-size: 40px; color: #409EFF;"></i>
        </div>
        <h3>正在加载审核详情...</h3>
        <p>请稍候，正在获取标准审核信息</p>
      </el-card>
    </div>

    <!-- 审核详情内容 -->
    <div v-if="!loading && detailData" class="approval-content">
      <!-- 页面标题和操作 -->
      <div class="page-header">
        <div class="header-left">
          <h2>薪酬标准审核</h2>
          <div class="standard-info">
            <el-tag :type="getStatusTagType(detailData.status)">
              {{ getStatusText(detailData.status) }}
            </el-tag>
            <span class="standard-code">{{ detailData.standardCode }}</span>
            <span class="standard-name">{{ detailData.standardName }}</span>
          </div>
        </div>
        <div class="header-actions">
          <el-button icon="el-icon-back" @click="handleBack">
            返回列表
          </el-button>
        </div>
      </div>

      <!-- 审核信息卡片 -->
      <el-card class="approval-info-card">
        <div slot="header" class="card-header">
          <span>审核信息</span>
        </div>
        
        <div v-if="detailData.currentApproval" class="approval-info">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label>提交人：</label>
                <span>{{ detailData.currentApproval.submitterName }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>提交时间：</label>
                <span>{{ formatDateTime(detailData.currentApproval.submitTime) }}</span>
              </div>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label>审核人：</label>
                <span>{{ detailData.currentApproval.approverName || '待审核' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>审核时间：</label>
                <span>{{ formatDateTime(detailData.currentApproval.approvalTime) || '未审核' }}</span>
              </div>
            </el-col>
          </el-row>
          
          <div class="info-item">
            <label>审核意见：</label>
            <div class="approval-opinion">
              {{ detailData.currentApproval.approvalOpinion || '暂无审核意见' }}
            </div>
          </div>
        </div>
        
        <div v-else class="no-approval-info">
          <el-alert
            title="暂无审核记录"
            type="info"
            description="该标准尚未提交审核"
            show-icon
          />
        </div>
      </el-card>

      <!-- 标准详情卡片 -->
      <el-card class="standard-detail-card">
        <div slot="header" class="card-header">
          <span>标准详情</span>
        </div>
        
        <!-- 基本信息 -->
        <div class="basic-info">
          <h3>基本信息</h3>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="info-item">
                <label>标准编码：</label>
                <span>{{ detailData.standardCode }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <label>标准名称：</label>
                <span>{{ detailData.standardName }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <label>当前状态：</label>
                <el-tag :type="getStatusTagType(detailData.status)" size="medium">
                  {{ getStatusText(detailData.status) }}
                </el-tag>
              </div>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="info-item">
                <label>创建人：</label>
                <span>{{ detailData.creatorName }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <label>创建时间：</label>
                <span>{{ formatDateTime(detailData.createdAt) }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <label>登记人：</label>
                <span>{{ detailData.registrarName || '未登记' }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 适用职位 -->
        <div class="positions-section">
          <h3>适用职位</h3>
          <el-table
            :data="detailData.positions"
            border
            stripe
            size="small"
            style="width: 100%"
          >
            <el-table-column label="职位编码" prop="posCode" width="120" />
            <el-table-column label="职位名称" prop="posName" width="150" />
            <el-table-column label="所属机构" prop="orgName" />
            <el-table-column label="状态" width="80">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="mini">
                  {{ scope.row.status === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 薪酬项目明细 -->
        <div class="salary-items-section">
          <h3>薪酬项目明细</h3>
          <el-table
            :data="detailData.items"
            border
            stripe
            size="small"
            style="width: 100%"
          >
            <el-table-column label="序号" prop="sortOrder" width="60" align="center" />
            <el-table-column label="项目编码" prop="projectCode" width="120" />
            <el-table-column label="项目名称" prop="projectName" />
            <el-table-column label="项目类型" width="100">
              <template slot-scope="scope">
                <el-tag 
                  :type="scope.row.projectType === 1 ? 'success' : 'danger'" 
                  size="small"
                >
                  {{ getProjectTypeText(scope.row.projectType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="计算方式" width="120" prop="calculationMethod" />
            <el-table-column label="金额/比例" width="150">
              <template slot-scope="scope">
                <span v-if="scope.row.calculationMethod === 'fixed'">
                  ¥ {{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}
                </span>
                <span v-else-if="scope.row.calculationMethod === 'percentage'">
                  {{ scope.row.amount ? scope.row.amount + '%' : '0%' }}
                </span>
                <span v-else>--</span>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 合计信息 -->
          <div class="summary-section">
            <div class="summary-item">
              <span>收入项：</span>
              <strong>{{ getIncomeItemsCount }} 项</strong>
            </div>
            <div class="summary-item">
              <span>扣除项：</span>
              <strong>{{ getDeductionItemsCount }} 项</strong>
            </div>
            <div class="summary-item">
              <span>固定金额合计：</span>
              <strong>¥ {{ getFixedAmountTotal.toFixed(2) }}</strong>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 审核操作按钮区域 - 新增，包含正确的条件判断 -->
      <el-card class="approval-actions-card" v-if="hasApprovePermission && detailData.status === 2">
        <div class="action-buttons">
          <el-button 
            type="success" 
            icon="el-icon-check"
            size="medium"
            @click="handleShowApproveDialog"
          >
            审核通过
          </el-button>
          <el-button 
            type="danger" 
            icon="el-icon-close"
            size="medium"
            @click="handleShowRejectDialog"
          >
            审核驳回
          </el-button>
        </div>
      </el-card>

      <!-- 审核日志卡片 -->
      <el-card class="approval-logs-card">
        <div slot="header" class="card-header">
          <span>审核日志</span>
          <el-button 
            type="text" 
            icon="el-icon-refresh"
            @click="refreshLogs"
          >
            刷新
          </el-button>
        </div>
        
        <el-timeline v-if="detailData.approvalLogs && detailData.approvalLogs.length > 0">
          <el-timeline-item
            v-for="log in sortedLogs"
            :key="log.id"
            :timestamp="formatDateTime(log.operationTime)"
            placement="top"
          >
            <el-card>
              <div class="log-item">
                <div class="log-header">
                  <span class="log-operator">{{ log.operatorName || '未知用户' }}</span>
                  <el-tag 
                    :type="getLogTypeTagType(log.operationType)" 
                    size="small"
                  >
                    {{ getOperationTypeText(log.operationType) }}
                  </el-tag>
                </div>
                <div class="log-content">
                  {{ log.operationContent }}
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        
        <div v-else class="no-logs">
          <el-alert
            title="暂无审核日志"
            type="info"
            description="该标准尚未有操作记录"
            show-icon
          />
        </div>
      </el-card>
    </div>

    <!-- 审核通过对话框 -->
    <el-dialog
      title="审核通过"
      :visible.sync="approveDialog.visible"
      width="500px"
      @close="approveDialog.opinion = ''"
    >
      <div class="dialog-content">
        <el-alert
          title="确认审核通过吗？"
          type="success"
          description="审核通过后，该薪酬标准将生效"
          show-icon
          style="margin-bottom: 20px;"
        />
        
        <el-form>
          <el-form-item label="审核意见" required>
            <el-input
              type="textarea"
              v-model="approveDialog.opinion"
              placeholder="请输入审核意见（必填）"
              :rows="4"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="approveDialog.visible = false">取消</el-button>
        <el-button 
          type="success" 
          @click="handleApprove"
          :loading="approving"
          :disabled="!approveDialog.opinion.trim()"
        >
          确认通过
        </el-button>
      </div>
    </el-dialog>

    <!-- 审核驳回对话框 -->
    <el-dialog
      title="审核驳回"
      :visible.sync="rejectDialog.visible"
      width="500px"
      @close="rejectDialog.opinion = ''"
    >
      <div class="dialog-content">
        <el-alert
          title="确认审核驳回吗？"
          type="warning"
          description="驳回后，提交人需要修改后重新提交"
          show-icon
          style="margin-bottom: 20px;"
        />
        
        <el-form>
          <el-form-item label="驳回意见" required>
            <el-input
              type="textarea"
              v-model="rejectDialog.opinion"
              placeholder="请输入驳回理由和修改建议（必填）"
              :rows="4"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="rejectDialog.visible = false">取消</el-button>
        <el-button 
          type="danger" 
          @click="handleReject"
          :loading="rejecting"
          :disabled="!rejectDialog.opinion.trim()"
        >
          确认驳回
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getStandardApprovalDetail, 
  getDetailedApprovalLogs,
  approveStandard,
  rejectStandard
} from '@/api/salary/standards'

export default {
  name: 'ApprovalDetail',
  data() {
    return {
      loading: false,
      detailData: null,
      
      // 审核对话框
      approveDialog: {
        visible: false,
        opinion: ''
      },
      
      rejectDialog: {
        visible: false,
        opinion: ''
      },
      
      // 操作状态
      approving: false,
      rejecting: false,
      
      // 用户角色
      userRoles: []
    }
  },
  
  computed: {
    // 排序后的日志（按时间倒序）
    sortedLogs() {
      if (!this.detailData || !this.detailData.approvalLogs) {
        return []
      }
      return [...this.detailData.approvalLogs].sort((a, b) => 
        new Date(b.operationTime) - new Date(a.operationTime)
      )
    },
    
  // 是否有审核权限（roleType 为 1 或 3 的用户都可以审核）
  hasApprovePermission() {
    // 强制转换为数字
    const roles = this.userRoles.map(r => Number(r));
    const allowedRoles = [1, 3]; // HR_MANAGER=1, FINANCE_MANAGER=3
    
    // 检查用户角色是否包含允许的角色
    const hasPermission = roles.some(r => allowedRoles.includes(r));
    console.log('用户角色数组:', roles, '允许的角色:', allowedRoles, '是否有审核权限:', hasPermission);
    
    return hasPermission;
  },
    
    // 统计信息
    getIncomeItemsCount() {
      if (!this.detailData || !this.detailData.items) return 0
      return this.detailData.items.filter(item => item.projectType === 1).length
    },
    
    getDeductionItemsCount() {
      if (!this.detailData || !this.detailData.items) return 0
      return this.detailData.items.filter(item => item.projectType === 2).length
    },
    
    getFixedAmountTotal() {
      if (!this.detailData || !this.detailData.items) return 0
      return this.detailData.items
        .filter(item => item.calculationMethod === 'fixed' && item.amount)
        .reduce((sum, item) => sum + Number(item.amount), 0)
    }
  },
  
  created() {
    this.loadUserRoles()
    this.loadApprovalDetail()
  },
  
  methods: {
    // 加载用户角色 - 确保转换为数字
    loadUserRoles() {
      try {
        const userStr = localStorage.getItem('user') || sessionStorage.getItem('user')
        if (userStr) {
          const user = JSON.parse(userStr)
          // 强制转换为数字
          if (user.roleType) {
            this.userRoles = [Number(user.roleType)]
          } else if (user.role && user.role.roleId) {
            this.userRoles = [Number(user.role.roleId)]
          } else {
            this.userRoles = []
          }
        }
        console.log('加载的用户角色:', this.userRoles);
      } catch (error) {
        console.error('加载用户角色失败:', error)
        this.userRoles = []
      }
    },
    
    // 加载审核详情
    async loadApprovalDetail() {
      try {
        this.loading = true
        const standardId = this.$route.params.id
        
        const response = await getStandardApprovalDetail(standardId)
        if (response && response.success) {
          this.detailData = response.data
          console.log('审核详情加载成功:', this.detailData)
          console.log('标准状态:', this.detailData.status)
        } else {
          throw new Error(response?.message || '加载失败')
        }
      } catch (error) {
        console.error('加载审核详情失败:', error)
        this.$message.error('加载审核详情失败: ' + error.message)
        this.$router.push('/salary/standards')
      } finally {
        this.loading = false
      }
    },
    
    // 刷新日志
    async refreshLogs() {
      try {
        const standardId = this.$route.params.id
        const response = await getDetailedApprovalLogs(standardId)
        if (response && response.success) {
          this.detailData.approvalLogs = response.data
          this.$message.success('日志已刷新')
        }
      } catch (error) {
        console.error('刷新日志失败:', error)
      }
    },
    
    // 显示审核通过对话框
    handleShowApproveDialog() {
      console.log('显示审核通过对话框');
      this.approveDialog.visible = true
    },
    
    // 显示审核驳回对话框
    handleShowRejectDialog() {
      console.log('显示审核驳回对话框');
      this.rejectDialog.visible = true
    },
    
    // 处理审核通过
    async handleApprove() {
      if (!this.approveDialog.opinion.trim()) {
        this.$message.warning('请输入审核意见')
        return
      }
      
      try {
        this.approving = true
        const standardId = this.$route.params.id
        
        const response = await approveStandard(standardId, this.approveDialog.opinion)
        if (response && response.success) {
          this.$message.success('审核通过成功')
          this.approveDialog.visible = false
          this.approveDialog.opinion = ''
          
          // 刷新页面数据
          await this.loadApprovalDetail()
        } else {
          throw new Error(response?.message || '审核失败')
        }
      } catch (error) {
        console.error('审核通过失败:', error)
        this.$message.error('审核失败: ' + error.message)
      } finally {
        this.approving = false
      }
    },
    
    // 处理审核驳回
    async handleReject() {
      if (!this.rejectDialog.opinion.trim()) {
        this.$message.warning('请输入驳回意见')
        return
      }
      
      try {
        this.rejecting = true
        const standardId = this.$route.params.id
        
        const response = await rejectStandard(standardId, this.rejectDialog.opinion)
        if (response && response.success) {
          this.$message.success('审核驳回成功')
          this.rejectDialog.visible = false
          this.rejectDialog.opinion = ''
          
          // 刷新页面数据
          await this.loadApprovalDetail()
        } else {
          throw new Error(response?.message || '驳回失败')
        }
      } catch (error) {
        console.error('审核驳回失败:', error)
        this.$message.error('驳回失败: ' + error.message)
      } finally {
        this.rejecting = false
      }
    },
    
    // 处理返回
    handleBack() {
      this.$router.push('/salary/standards')
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
    
    // 获取状态标签类型
    getStatusTagType(status) {
      switch (status) {
        case 1: return 'info'    // 草稿
        case 2: return 'warning' // 待审核
        case 3: return 'success' // 已生效
        case 0: return 'danger'  // 驳回
        default: return ''
      }
    },
    
    // 获取状态文本
    getStatusText(status) {
      switch (status) {
        case 1: return '草稿'
        case 2: return '待审核'
        case 3: return '已生效'
        case 0: return '已驳回'
        default: return '未知'
      }
    },
    
    // 获取项目类型文本
    getProjectTypeText(projectType) {
      switch (projectType) {
        case 1: return '收入项'
        case 2: return '扣除项'
        default: return '其他'
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
.approval-detail-page {
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-left h2 {
  margin: 0 0 10px 0;
  color: #303133;
}

.standard-info {
  display: flex;
  align-items: center;
  gap: 15px;
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

/* 卡片样式 */
.approval-info-card,
.standard-detail-card,
.approval-actions-card,
.approval-logs-card {
  margin-bottom: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
  background: #f8f9fa;
  border-radius: 8px 8px 0 0;
}

.card-header span {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 信息项样式 */
.info-item {
  margin: 15px 0;
  padding: 10px 0;
  border-bottom: 1px dashed #ebeef5;
}

.info-item label {
  display: inline-block;
  width: 100px;
  color: #909399;
  font-weight: 500;
}

.info-item span {
  color: #606266;
}

.approval-opinion {
  margin-top: 10px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border-left: 4px solid #67C23A;
  color: #606266;
  line-height: 1.6;
}

/* 详情卡片内容 */
.basic-info,
.positions-section,
.salary-items-section {
  margin-bottom: 30px;
}

.basic-info h3,
.positions-section h3,
.salary-items-section h3 {
  margin: 0 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #409EFF;
  color: #303133;
  font-size: 18px;
}

/* 合计信息 */
.summary-section {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.summary-item {
  text-align: center;
}

.summary-item span {
  display: block;
  color: #909399;
  font-size: 14px;
  margin-bottom: 5px;
}

.summary-item strong {
  display: block;
  color: #409EFF;
  font-size: 18px;
}

/* 审核操作按钮样式 */
.approval-actions-card {
  margin: 20px 0;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.action-buttons {
  padding: 20px;
  text-align: center;
  display: flex;
  justify-content: center;
  gap: 20px;
}

/* 日志样式 */
.log-item {
  padding: 10px;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.log-operator {
  font-weight: 600;
  color: #303133;
}

.log-content {
  color: #606266;
  line-height: 1.6;
}

.no-logs {
  text-align: center;
  padding: 40px;
  color: #909399;
}

/* 对话框样式 */
.dialog-content {
  padding: 10px;
}

.dialog-footer {
  text-align: right;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .approval-detail-page {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
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
  
  .summary-section {
    flex-direction: column;
    gap: 15px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 10px;
  }
}
</style>