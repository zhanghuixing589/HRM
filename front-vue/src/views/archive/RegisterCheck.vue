<template>
  <div>
    <el-card shadow="never">
      <div slot="header" class="table-header">
        <span>档案登记复核</span>
        <div style="float: right;">
          <el-button type="text" icon="el-icon-refresh" @click="loadData">刷新</el-button>
        </div>
      </div>
      
      <!-- 状态筛选 -->
      <div style="margin-bottom: 15px;">
        <el-radio-group v-model="statusFilter" @change="handleStatusFilterChange">
          <el-radio-button :label="null">全部</el-radio-button>
          <el-radio-button :label="1">待复核</el-radio-button>
          <el-radio-button :label="4">重新提交待审核</el-radio-button>
          <el-radio-button :label="2">已通过</el-radio-button>
          <el-radio-button :label="3">已驳回</el-radio-button>
          <el-radio-button :label="0">已删除</el-radio-button>
        </el-radio-group>
      </div>
      
      <!-- 档案列表 -->
      <el-table 
        :data="archiveList" 
        stripe 
        v-loading="loading"
        size="small"
        style="width: 100%;"
        :empty-text="tableEmptyText"
      >
        <el-table-column 
          label="档案编号" 
          width="150"
          align="center"
          header-align="center"
        >
          <template slot-scope="{ row }">
            {{ row.arcCode || '--' }}
          </template>
        </el-table-column>
        <el-table-column 
          label="姓名" 
          width="100"
          align="center"
          header-align="center"
        >
          <template slot-scope="{ row }">
            {{ row.name || '--' }}
          </template>
        </el-table-column>
        <el-table-column 
          label="所属机构" 
          min-width="200"
          align="center"
          header-align="center"
        >
          <template slot-scope="{ row }">
            {{ getOrgPath(row) }}
          </template>
        </el-table-column>
        <el-table-column 
          label="职位" 
          width="120"
          align="center"
          header-align="center"
        >
          <template slot-scope="{ row }">
            {{ row.positionName || '--' }}
          </template>
        </el-table-column>
        <el-table-column 
          label="登记人" 
          width="100"
          align="center"
          header-align="center"
        >
          <template slot-scope="{ row }">
            {{ row.writerName || '--' }}
          </template>
        </el-table-column>
        <el-table-column 
          label="提交时间" 
          width="150"
          align="center"
          header-align="center"
        >
          <template slot-scope="{ row }">
            {{ formatDate(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column 
          label="状态" 
          width="110"
          align="center"
          header-align="center"
        >
          <template slot-scope="{ row }">
            <el-tag 
              :type="getStatusColor(row.status)" 
              size="small"
              style="min-width: 80px;"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column 
          label="操作" 
          width="180"
          align="center"
          header-align="center"
          fixed="right"
        >
          <template slot-scope="{ row }">
            <!-- 待复核和重新提交待审核状态显示复核按钮 -->
            <el-button 
              v-if="row.status === 1 || row.status === 4"
              type="primary" 
              size="mini" 
              @click="showReviewDialog(row)"
              style="margin-right: 5px;"
            >
              复核
            </el-button>
            
            <!-- 已删除状态显示恢复按钮 -->
            <el-button 
              v-else-if="row.status === 0"
              type="success" 
              size="mini" 
              @click="handleRestore(row)"
              style="margin-right: 5px;"
            >
              恢复
            </el-button>
            
            <!-- 非待复核和重新提交状态显示删除按钮 -->
            <el-button 
              v-else-if="row.status === 2"
              type="danger" 
              size="mini" 
              @click="handleDelete(row)"
              style="margin-right: 5px;"
            >
              删除
            </el-button>
            
            <!-- 详情按钮 -->
            <el-button 
              type="text" 
              size="mini" 
              @click="showDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination 
        v-if="total > 0" 
        :current-page="query.page + 1" 
        :page-size="query.size" 
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; text-align: right;"
      />
      
      <!-- 当没有数据时显示提示 -->
      <div v-if="!loading && archiveList.length === 0" style="text-align: center; padding: 40px;">
        <el-empty :description="tableEmptyText" />
      </div>
    </el-card>
    
    <!-- 复核对话框 -->
    <el-dialog 
      :title="reviewData ? `档案复核 - ${reviewData.name} (${reviewData.arcCode})` : '档案复核'"
      :visible.sync="reviewDialogVisible" 
      width="800px"
      top="5vh"
      :close-on-click-modal="false"
      @close="handleReviewDialogClose"
    >
      <div v-if="reviewData">
        <el-form ref="reviewForm" :model="reviewForm" label-width="100px" size="small">
          <!-- 基本信息 -->
          <el-descriptions 
            title="基本信息" 
            :column="2" 
            border
            size="small"
            style="margin-bottom: 20px;"
          >
            <el-descriptions-item label="档案编号">
              <span style="color: #606266; font-weight: 500;">
                {{ reviewData.arcCode }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="姓名">
              <span style="color: #606266; font-weight: 500;">
                {{ reviewData.name }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="性别">
              <span style="color: #606266; font-weight: 500;">
                {{ reviewData.sex === 1 ? '男' : '女' }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="身份证号">
              <span style="color: #606266; font-weight: 500;">
                {{ reviewData.idCard }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="所属机构">
              <span style="color: #606266; font-weight: 500;">
                {{ getOrgPath(reviewData) }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="职位">
              <span style="color: #606266; font-weight: 500;">
                {{ reviewData.positionName }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="职称">
              <span style="color: #606266; font-weight: 500;">
                {{ reviewData.title }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="登记人">
              <span style="color: #606266; font-weight: 500;">
                {{ reviewData.writerName }}
              </span>
            </el-descriptions-item>
          </el-descriptions>
          
          <!-- 可修改字段 -->
          <h4 style="margin: 0 0 15px 0; color: #409EFF;">
            <i class="el-icon-edit" style="margin-right: 5px;"></i>
            可修改信息
          </h4>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="民族">
                <el-input v-model="reviewForm.nationality" placeholder="请输入民族" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="学历">
                <el-select v-model="reviewForm.qualification" placeholder="请选择学历" style="width: 100%;">
                  <el-option label="小学" value="小学" />
                  <el-option label="初中" value="初中" />
                  <el-option label="高中" value="高中" />
                  <el-option label="专科" value="专科" />
                  <el-option label="本科" value="本科" />
                  <el-option label="硕士" value="硕士" />
                  <el-option label="博士" value="博士" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="政治面貌">
                <el-input v-model="reviewForm.identity" placeholder="请输入政治面貌" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="宗教信仰">
                <el-input v-model="reviewForm.belief" placeholder="请输入宗教信仰" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="手机">
                <el-input v-model="reviewForm.phone" placeholder="请输入手机号" maxlength="11" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Email">
                <el-input v-model="reviewForm.email" placeholder="请输入Email" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="住址">
            <el-input v-model="reviewForm.address" placeholder="请输入住址" />
          </el-form-item>
          
          <!-- 审核操作 -->
          <el-form-item label="审核操作" required>
            <el-radio-group v-model="reviewForm.action">
              <el-radio :label="2">通过审核</el-radio>
              <el-radio :label="3">驳回申请</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <!-- 驳回原因 -->
          <el-form-item 
            label="驳回原因" 
            v-if="reviewForm.action === 3"
            required
            :rules="[{ required: true, message: '请填写驳回原因', trigger: 'blur' }]"
            prop="comment"
          >
            <el-input 
              v-model="reviewForm.comment" 
              type="textarea" 
              :rows="3" 
              placeholder="请填写驳回原因，以便专员修改后重新提交"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>
          
          <!-- 审核备注（通过时可选） -->
          <el-form-item label="审核备注" v-if="reviewForm.action === 2">
            <el-input 
              v-model="reviewForm.comment" 
              type="textarea" 
              :rows="2" 
              placeholder="可填写审核备注（可选）"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      <div v-else style="text-align: center; padding: 40px;">
        <i class="el-icon-loading" style="font-size: 40px;"></i>
        <div style="margin-top: 20px;">加载中...</div>
      </div>
      
      <span slot="footer" class="dialog-footer">
        <el-button @click="reviewDialogVisible = false" size="medium">取 消</el-button>
        <el-button 
          type="primary" 
          @click="handleReviewSubmit" 
          :loading="reviewSubmitting"
          size="medium"
          :disabled="!reviewData"
        >
          确 定
        </el-button>
      </span>
    </el-dialog>
    
    <!-- 档案详情对话框 -->
    <el-dialog 
      title="档案详情" 
      :visible.sync="detailDialogVisible" 
      width="700px"
      top="5vh"
      @close="handleDetailDialogClose"
    >
      <div v-if="currentArchive">
        <el-descriptions 
          :column="2" 
          border
          size="small"
        >
          <el-descriptions-item label="档案编号">{{ currentArchive.arcCode }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentArchive.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ currentArchive.sex === 1 ? '男' : '女' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ currentArchive.idCard }}</el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ currentArchive.birDate }}</el-descriptions-item>
          <el-descriptions-item label="民族">{{ currentArchive.nationality }}</el-descriptions-item>
          <el-descriptions-item label="学历">{{ currentArchive.qualification }}</el-descriptions-item>
          <el-descriptions-item label="政治面貌">{{ currentArchive.identity }}</el-descriptions-item>          <el-descriptions-item label="一级机构">{{ currentArchive.firstOrgName }}</el-descriptions-item>
          <el-descriptions-item label="二级机构">{{ currentArchive.secondOrgName }}</el-descriptions-item>
          <el-descriptions-item label="三级机构">{{ currentArchive.thirdOrgName }}</el-descriptions-item>
          <el-descriptions-item label="职位">{{ currentArchive.positionName }}</el-descriptions-item>
          <el-descriptions-item label="职称">{{ currentArchive.title }}</el-descriptions-item>
          <el-descriptions-item label="薪酬标准">{{ currentArchive.salaryStandardName || currentArchive.salaryStandard || '--' }}</el-descriptions-item>
          <el-descriptions-item label="手机">{{ currentArchive.phone }}</el-descriptions-item>
          <el-descriptions-item label="Email">{{ currentArchive.email }}</el-descriptions-item>
          <el-descriptions-item label="住址">{{ currentArchive.address }}</el-descriptions-item>
          <el-descriptions-item label="国籍">{{ currentArchive.country }}</el-descriptions-item>
          <el-descriptions-item label="出生地">{{ currentArchive.birAddress }}</el-descriptions-item>
          <el-descriptions-item label="宗教信仰">{{ currentArchive.belief }}</el-descriptions-item>
          <el-descriptions-item label="邮编">{{ currentArchive.zipCode }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ currentArchive.major }}</el-descriptions-item>
          <el-descriptions-item label="登记人">{{ currentArchive.writerName }}</el-descriptions-item>
          <el-descriptions-item label="登记时间">{{ formatDate(currentArchive.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="复核人" v-if="currentArchive.reviewerName">{{ currentArchive.reviewerName }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDate(currentArchive.submitTime) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusColor(currentArchive.status)" size="small">
              {{ getStatusText(currentArchive.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="驳回原因" v-if="currentArchive.reason" :span="2">
            {{ currentArchive.reason }}
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 审核流程时间轴 -->
        <h4 style="margin: 20px 0 10px 0; color: #666;">
          <i class="el-icon-time" style="margin-right: 5px;"></i>
          审核流程
        </h4>
        <el-timeline style="padding-left: 20px;" v-if="processList.length > 0">
          <el-timeline-item 
            v-for="op in processList" 
            :key="op.reviewId" 
            :timestamp="formatDate(op.operationTime)"
            :color="getProcessColor(op.operationType)"
            placement="top"
          >
            <el-card shadow="hover" body-style="padding: 10px;">
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span style="font-weight: bold;">{{ getProcessTypeText(op.operationType) }}</span>
                <span style="color: #909399; font-size: 12px;">{{ op.operatorName }}</span>
              </div>
              <div v-if="op.operationComment" style="margin-top: 5px; font-size: 12px; color: #666;">
                {{ op.operationComment }}
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <div v-else style="text-align: center; color: #999; padding: 20px;">
          暂无审核记录
        </div>
      </div>
      <div v-else style="text-align: center; padding: 40px;">
        <i class="el-icon-loading" style="font-size: 40px;"></i>
        <div style="margin-top: 20px;">加载中...</div>
      </div>
      
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false" size="medium">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { queryArchives, reviewArchive, deleteArchive, restoreArchive, getArchiveDetail, getArchiveProcess } from '@/api/archive'
import { getAllActiveSalaryStandards } from '@/api/archiveSalary'

export default {
  name: 'RegisterCheck',
  data() {
    return {
      loading: false,
      archiveList: [],
      total: 0,
      query: {
        page: 0,
        size: 10
      },
      statusFilter: null,
      tableEmptyText: '暂无数据',
      
      // 复核相关
      reviewDialogVisible: false,
      reviewSubmitting: false,
      reviewData: null,
      reviewForm: {
        action: 2, // 2:通过, 3:驳回
        comment: '',
        changedData: {}
      },
      
      // 详情相关
      detailDialogVisible: false,
      currentArchive: null,
      processList: []
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return '--'
      try {
        const date = new Date(dateStr)
        if (isNaN(date.getTime())) {
          return dateStr.replace('T', ' ')
        }
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false
        }).replace(/\//g, '-')
      } catch (e) {
        return dateStr
      }
    },
    
    // 获取机构路径
    getOrgPath(row) {
      if (!row) return '--'
      const first = row.firstOrgName || ''
      const second = row.secondOrgName || ''
      const third = row.thirdOrgName || ''
      return `${first} / ${second} / ${third}`
    },
    
    // 状态筛选变化处理
    handleStatusFilterChange() {
      this.query.page = 0
      this.loadData()
    },
    
    // 加载数据
    async loadData() {
      try {
        this.loading = true
        this.tableEmptyText = '加载中...'
        
        // 构建查询参数
        const params = {
          page: this.query.page,
          size: this.query.size
        }
        
        // 根据状态筛选
        if (this.statusFilter !== null) {
          params.status = this.statusFilter
        }
        
        console.log('查询参数:', params)
        
        // 使用 GET 方式查询接口
        const response = await queryArchives(params)
        
        console.log('API响应数据:', response)
        
        if (response && response.code === 200) {
          const data = response.data
          
          if (data && data.list) {
            // 使用 list 字段（后端返回的数据结构）
            this.archiveList = data.list || []
            this.total = data.total || 0
            
            console.log('成功获取数据，列表长度:', this.archiveList.length)
            console.log('数据详情:', this.archiveList)
          } else if (data && data.content) {
            // 兼容 content 字段
            this.archiveList = data.content || []
            this.total = data.totalElements || 0
          } else if (Array.isArray(data)) {
            // 直接是数组格式
            this.archiveList = data
            this.total = data.length
          } else {
            console.warn('数据格式异常:', data)
            this.archiveList = []
            this.total = 0
          }
          
          this.tableEmptyText = this.archiveList.length === 0 ? '暂无数据' : ''
          console.log('最终设置的数据:', this.archiveList)
        } else {
          this.archiveList = []
          this.total = 0
          this.tableEmptyText = '暂无数据'
          const errorMsg = response ? response.message : '获取数据失败'
          this.$message.error(errorMsg)
        }
      } catch (error) {
        console.error('加载档案列表失败:', error)
        this.$message.error('加载档案列表失败: ' + (error.message || '未知错误'))
        this.archiveList = []
        this.total = 0
        this.tableEmptyText = '加载失败，请刷新重试'
      } finally {
        this.loading = false
      }
    },
    
    // 分页处理
    handleSizeChange(size) {
      this.query.size = size
      this.query.page = 0
      this.loadData()
    },
    
    handleCurrentChange(page) {
      this.query.page = page - 1
      this.loadData()
    },
    
    // 状态文本和颜色
    getStatusText(status) {
      const map = {
        0: '已删除',
        1: '待复核',
        2: '已通过',
        3: '已驳回',
        4: '重新提交待审核'
      }
      return map[status] || '未知'
    },
    
    getStatusColor(status) {
      const map = {
        0: 'info',
        1: 'warning',
        2: 'success',
        3: 'danger',
        4: 'warning'
      }
      return map[status] || 'info'
    },
    
    // 显示复核对话框
    showReviewDialog(row) {
      if (!row) {
        console.error('传入的档案数据为空')
        this.$message.error('档案数据异常')
        return
      }
      
      console.log('打开复核对话框，档案数据:', row)
      
      // 创建深拷贝，避免引用问题
      this.reviewData = JSON.parse(JSON.stringify(row))
      this.reviewForm = {
        action: 2,
        comment: '',
        changedData: {}
      }
      
      // 初始化可修改字段
      this.reviewForm.changedData = {
        nationality: row.nationality || '',
        qualification: row.qualification || '',
        identity: row.identity || '',
        belief: row.belief || '',
        phone: row.phone || '',
        email: row.email || '',
        address: row.address || ''
      }
      
      // 将数据复制到表单模型
      Object.keys(this.reviewForm.changedData).forEach(key => {
        this.reviewForm[key] = this.reviewForm.changedData[key]
      })
      
      this.reviewDialogVisible = true
    },
    
    // 复核对话框关闭处理
    handleReviewDialogClose() {
      this.reviewData = null
      this.reviewForm = {
        action: 2,
        comment: '',
        changedData: {}
      }
      if (this.$refs.reviewForm) {
        this.$refs.reviewForm.clearValidate()
      }
    },
    
    // 提交复核
    async handleReviewSubmit() {
      try {
        if (!this.reviewData) {
          this.$message.error('档案数据异常')
          return
        }
        
        // 验证表单
        if (this.reviewForm.action === 3 && !this.reviewForm.comment.trim()) {
          this.$message.error('请填写驳回原因')
          return
        }
        
        this.reviewSubmitting = true
        
        // 收集修改的字段
        const changedData = {}
        Object.keys(this.reviewForm.changedData).forEach(key => {
          if (this.reviewForm[key] !== this.reviewData[key]) {
            changedData[key] = this.reviewForm[key]
          }
        })
        
        // 构建提交数据
        const submitData = {
          arcId: this.reviewData.arcId,
          action: this.reviewForm.action,
          comment: this.reviewForm.comment,
          changedData: Object.keys(changedData).length > 0 ? changedData : null
        }
        
        console.log('提交复核数据:', submitData)
        
        // 调用复核接口
        const response = await reviewArchive(submitData)
        
        if (response && response.code === 200) {
          this.$message.success(response.message || '复核成功')
          this.reviewDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(response ? response.message : '复核失败')
        }
      } catch (error) {
        console.error('复核失败:', error)
        let errorMsg = '复核失败'
        if (error.response && error.response.data) {
          errorMsg = error.response.data.message || errorMsg
        } else if (error.message) {
          errorMsg = error.message
        }
        this.$message.error(errorMsg)
      } finally {
        this.reviewSubmitting = false
      }
    },
    
    // 删除档案
    handleDelete(row) {
      if (!row || !row.arcId) {
        this.$message.error('档案数据异常')
        return
      }
      
      this.$confirm(`确定要删除档案【${row.name} (${row.arcCode})】吗？`, '确认删除', {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }).then(async () => {
        try {
          const response = await deleteArchive(row.arcId)
          
          if (response && response.code === 200) {
            this.$message.success(response.message || '删除成功')
            this.loadData()
          } else {
            this.$message.error(response ? response.message : '删除失败')
          }
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    
    // 恢复档案
    handleRestore(row) {
      if (!row || !row.arcId) {
        this.$message.error('档案数据异常')
        return
      }
      
      this.$confirm(`确定要恢复档案【${row.name} (${row.arcCode})】吗？`, '确认恢复', {
        type: 'info',
        confirmButtonText: '确定恢复',
        cancelButtonText: '取消'
      }).then(async () => {
        try {
          const response = await restoreArchive(row.arcId)
          
          if (response && response.code === 200) {
            this.$message.success(response.message || '恢复成功')
            this.loadData()
          } else {
            this.$message.error(response ? response.message : '恢复失败')
          }
        } catch (error) {
          console.error('恢复失败:', error)
          this.$message.error('恢复失败')
        }
      }).catch(() => {})
    },
    
    // 显示详情
    async showDetail(row) {
      if (!row || !row.arcId) {
        this.$message.error('档案数据异常')
        return
      }
      
      try {
        this.loading = true
        this.currentArchive = null
        this.processList = []
        
        // 获取档案详情
        const detailResponse = await getArchiveDetail(row.arcId)
        if (detailResponse && detailResponse.code === 200) {
          this.currentArchive = detailResponse.data

          // 如果后端没有返回薪酬标准名称，则尝试获取
          if (this.currentArchive.salaryStandard && !this.currentArchive.salaryStandardName) {
            try {
              const salaryResponse = await getAllActiveSalaryStandards()
              if (salaryResponse && salaryResponse.code === 200) {
                const standards = salaryResponse.data || []
                const standard = standards.find(s => s.standardId == this.currentArchive.salaryStandard)
                if (standard) {
                  this.currentArchive.salaryStandardName = standard.standardName
                }
              }
            } catch (error) {
              console.error('获取薪酬标准名称失败:', error)
            }
          }

        } else {
          this.$message.error('获取档案详情失败')
          return
        }
        
        // 获取审核流程
        const processResponse = await getArchiveProcess(row.arcId)
        if (processResponse && processResponse.code === 200) {
          this.processList = processResponse.data
        }
        
        this.detailDialogVisible = true
      } catch (error) {
        console.error('获取档案详情失败:', error)
        this.$message.error('获取档案详情失败')
      } finally {
        this.loading = false
      }
    },
    
    // 详情对话框关闭处理
    handleDetailDialogClose() {
      this.currentArchive = null
      this.processList = []
    },
    
    // 流程类型文本和颜色
    getProcessTypeText(type) {
      const map = {
        0: '删除',
        1: '提交',
        2: '审核通过',
        3: '审核驳回',
        4: '重新提交',
        5: '恢复'
      }
      return map[type] || '未知操作'
    },
    
    getProcessColor(type) {
      const map = {
        0: '#909399', // 删除 - 灰色
        1: '#E6A23C', // 提交 - 橙色
        2: '#67C23A', // 通过 - 绿色
        3: '#F56C6C', // 驳回 - 红色
        4: '#409EFF', // 重新提交 - 蓝色
        5: '#67C23A'  // 恢复 - 绿色
      }
      return map[type] || '#409EFF'
    }
  }
}
</script>

<style scoped lang="scss">
.table-header {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-table {
  ::v-deep .cell {
    padding: 0 10px;
  }
  
  ::v-deep th {
    background-color: #f5f7fa;
    font-weight: 600;
    color: #303133;
  }
  
  ::v-deep .el-table__row:hover {
    background-color: #f5f7fa;
  }
}

.el-pagination {
  ::v-deep .el-pagination__total {
    margin-right: 20px;
  }
  
  ::v-deep .el-pagination__sizes {
    margin-right: 20px;
  }
}

.dialog-footer {
  text-align: center;
}
</style>