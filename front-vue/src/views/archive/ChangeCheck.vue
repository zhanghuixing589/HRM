<template>
  <div>
    <el-card shadow="never">
      <div slot="header" class="table-header">
        <span>档案变更复核</span>
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
        </el-radio-group>
      </div>

      <!-- 变更申请列表 -->
      <el-table :data="changeList" stripe v-loading="loading" size="small" style="width: 100%;"
        :empty-text="tableEmptyText">
        <el-table-column label="变更编码" width="120" align="center" header-align="center">
          <template slot-scope="{ row }">
            {{ row.changeCode || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="档案编号" width="120" align="center" header-align="center">
          <template slot-scope="{ row }">
            {{ row.archiveCode || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="姓名" width="100" align="center" header-align="center">
          <template slot-scope="{ row }">
            {{ row.archiveName || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="变更原因" min-width="180" align="center" header-align="center">
          <template slot-scope="{ row }">
            <el-tooltip :content="row.changeReason" placement="top">
              <span
                style="display: inline-block; max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                {{ row.changeReason || '--' }}
              </span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="变更字段" width="150" align="center" header-align="center">
          <template slot-scope="{ row }">
            <el-tag v-if="row.changedFieldsParsed && row.changedFieldsParsed.length > 0" type="info" size="small">
              {{ row.changedFieldsParsed.length }}个字段
            </el-tag>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column label="申请人" width="100" align="center" header-align="center">
          <template slot-scope="{ row }">
            {{ row.applicantName || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="150" align="center" header-align="center">
          <template slot-scope="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center" header-align="center">
          <template slot-scope="{ row }">
            <el-tag :type="getStatusColor(row.status)" size="small" style="min-width: 80px;">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" header-align="center" fixed="right">
          <template slot-scope="{ row }">
            <!-- 待复核状态显示复核按钮 -->
            <el-button v-if="row.status === 1 || row.status === 4" type="primary" size="mini"
              @click="showReviewDialog(row)" style="margin-right: 5px;">
              复核
            </el-button>

            <!-- 详情按钮 -->
            <el-button type="text" size="mini" @click="showDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination v-if="total > 0" :current-page="query.page + 1" :page-size="query.size" :total="total"
        layout="total, sizes, prev, pager, next, jumper" :page-sizes="[10, 20, 50, 100]" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" style="margin-top: 20px; text-align: right;" />
    </el-card>

    <!-- 复核对话框 -->
    <el-dialog
      :title="`档案变更复核 - ${reviewData ? reviewData.archiveCode : ''} (${reviewData ? reviewData.archiveName : ''})`"
      :visible.sync="reviewDialogVisible" width="900px" top="5vh" :close-on-click-modal="false"
      @close="handleReviewDialogClose">
      <div v-if="reviewData">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="2" border size="small" style="margin-bottom: 20px;">
          <el-descriptions-item label="变更编码">
            <span style="color: #606266; font-weight: 500;">
              {{ reviewData.changeCode }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="档案编号">
            <span style="color: #606266; font-weight: 500;">
              {{ reviewData.archiveCode }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="姓名">
            <span style="color: #606266; font-weight: 500;">
              {{ reviewData.archiveName }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="所属机构">
            <span style="color: #606266; font-weight: 500;">
              {{ getOrgPath(reviewData.archive) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="职位">
            <span style="color: #606266; font-weight: 500;">
              {{ reviewData.archive.positionName }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="变更原因">
            <span style="color: #606266; font-weight: 500;">
              {{ reviewData.changeReason }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="申请人">
            <span style="color: #606266; font-weight: 500;">
              {{ reviewData.applicantName }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            <span style="color: #606266; font-weight: 500;">
              {{ formatDate(reviewData.createTime) }}
            </span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 可修改信息 -->
        <h4 style="margin: 0 0 15px 0; color: #409EFF;">
          <i class="el-icon-edit" style="margin-right: 5px;"></i>
          变更后档案信息
        </h4>

        <el-form ref="reviewForm" :model="reviewForm" label-width="100px" size="small">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="姓名">
                <el-input v-model="reviewForm.name" placeholder="请输入姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别">
                <el-select v-model="reviewForm.sex" placeholder="请选择性别" style="width: 100%;">
                  <el-option label="男" :value="1" />
                  <el-option label="女" :value="2" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="身份证号">
                <el-input v-model="reviewForm.idCard" placeholder="请输入身份证号" maxlength="18" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="职称">
                <el-input v-model="reviewForm.title" placeholder="请输入职称" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="薪酬标准">
                <el-input v-model="reviewForm.salaryStandard" placeholder="请输入薪酬标准" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="出生日期">
                <el-date-picker v-model="reviewForm.birDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd"
                  style="width: 100%;" />
              </el-form-item>
            </el-col>
          </el-row>

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
                  <el-option label="研究生" value="研究生" />
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
              <el-form-item label="Email">
                <el-input v-model="reviewForm.email" placeholder="请输入Email" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机">
                <el-input v-model="reviewForm.phone" placeholder="请输入手机号" maxlength="11" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="电话">
                <el-input v-model="reviewForm.telephone" placeholder="请输入电话" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="国籍">
                <el-input v-model="reviewForm.country" placeholder="请输入国籍" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="出生地">
                <el-input v-model="reviewForm.birAddress" placeholder="请输入出生地" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="专业">
                <el-input v-model="reviewForm.major" placeholder="请输入专业" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="住址">
            <el-input v-model="reviewForm.address" placeholder="请输入住址" />
          </el-form-item>

          <el-form-item label="邮编">
            <el-input v-model="reviewForm.zipCode" placeholder="请输入邮编" />
          </el-form-item>

          <!-- 审核操作 -->
          <el-form-item label="审核操作" required>
            <el-radio-group v-model="reviewForm.action">
              <el-radio :label="2">通过</el-radio>
              <el-radio :label="3">驳回</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 驳回原因 -->
          <el-form-item label="驳回原因" v-if="reviewForm.action === 3" required
            :rules="[{ required: true, message: '请填写驳回原因', trigger: 'blur' }]" prop="comment">
            <el-input v-model="reviewForm.comment" type="textarea" :rows="3" placeholder="请填写驳回原因，以便专员修改后重新提交"
              maxlength="100" show-word-limit />
          </el-form-item>

          <!-- 审核备注 -->
          <el-form-item label="审核备注" v-if="reviewForm.action === 2">
            <el-input v-model="reviewForm.comment" type="textarea" :rows="2" placeholder="可填写审核备注（可选）" maxlength="100"
              show-word-limit />
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="reviewDialogVisible = false" size="medium">取 消</el-button>
        <el-button type="primary" @click="handleReviewSubmit" :loading="reviewSubmitting" size="medium"
          :disabled="!reviewData">
          确 定
        </el-button>
      </span>
    </el-dialog>

    <!-- 变更详情对话框 -->
    <el-dialog title="变更详情" :visible.sync="detailDialogVisible" width="900px" top="5vh"
      @close="handleDetailDialogClose">
      <div v-if="currentChange">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="2" border size="small" style="margin-bottom: 20px;">
          <el-descriptions-item label="变更编码">
            <span style="color: #606266; font-weight: 500;">
              {{ currentChange.changeCode }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="档案编号">
            <span style="color: #606266; font-weight: 500;">
              {{ currentChange.archiveCode }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="姓名">
            <span style="color: #606266; font-weight: 500;">
              {{ currentChange.archiveName }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="变更原因">
            <span style="color: #606266; font-weight: 500;">
              {{ currentChange.changeReason }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="申请人">
            <span style="color: #606266; font-weight: 500;">
              {{ currentChange.applicantName }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            <span style="color: #606266; font-weight: 500;">
              {{ formatDate(currentChange.createTime) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusColor(currentChange.status)" size="small">
              {{ getStatusText(currentChange.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="复核人" v-if="currentChange.reviewerName">
            <span style="color: #606266; font-weight: 500;">
              {{ currentChange.reviewerName }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="复核备注" v-if="currentChange.reviewComment" :span="2">
            <span style="color: #606266; font-weight: 500;">
              {{ currentChange.reviewComment }}
            </span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 变更详情对比 -->
        <h4 style="margin: 20px 0 10px 0; color: #409EFF;">
          <i class="el-icon-edit" style="margin-right: 5px;"></i>
          变更详情对比
        </h4>

        <el-table :data="getDetailChangedFieldsData()" stripe size="small" style="width: 100%; margin-bottom: 20px;">
          <el-table-column label="字段" width="150" align="center" header-align="center">
            <template slot-scope="{ row }">
              <span style="font-weight: 500;">{{ getFieldLabel(row.field) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="变更前" min-width="200" align="center" header-align="center">
            <template slot-scope="{ row }">
              <div style="background: #f0f9eb; padding: 5px 10px; border-radius: 3px; color: #67c23a;">
                {{ row.beforeValue || '空' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="变更后" min-width="200" align="center" header-align="center">
            <template slot-scope="{ row }">
              <div style="background: #ecf5ff; padding: 5px 10px; border-radius: 3px; color: #409eff;">
                {{ row.afterValue || '空' }}
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 审核流程时间轴 -->
        <h4 style="margin: 20px 0 10px 0; color: #666;">
          <i class="el-icon-time" style="margin-right: 5px;"></i>
          审核流程
        </h4>
        <el-timeline style="padding-left: 20px;" v-if="processList.length > 0">
          <el-timeline-item v-for="op in processList" :key="op.reviewId" :timestamp="formatDate(op.operationTime)"
            :color="getProcessColor(op.operationType)" placement="top">
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
import { reviewChange, getChangeDetail, getChangeProcess, queryChanges } from '@/api/change'

export default {
  name: 'ChangeCheck',
  data() {
    return {
      loading: false,
      changeList: [],
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
      currentChange: null,
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

    // 状态筛选变化处理
    handleStatusFilterChange() {
      this.query.page = 0
      // 如果选择的是"全部"，则status为null
      if (this.statusFilter === null) {
        delete this.query.status
      } else {
        this.query.status = this.statusFilter
      }
      this.loadData()
    },

    // 加载数据
    async loadData() {
      try {
        this.loading = true
        this.tableEmptyText = '加载中...'

        const params = {
          page: this.query.page,
          size: this.query.size
        }

        // 根据状态筛选设置参数
        if (this.statusFilter !== null) {
          params.status = this.statusFilter
        }

        console.log('查询变更参数:', params)

        const response = await queryChanges(params)

        console.log('查询变更结果:', response)

        if (response && response.code === 200) {
          const data = response.data

          // 处理不同的数据结构
          if (data && data.list) {
            this.changeList = data.list
            this.total = data.total || 0
          } else if (data && data.content) {
            this.changeList = data.content
            this.total = data.totalElements || 0
          } else if (Array.isArray(data)) {
            this.changeList = data
            this.total = data.length
          } else {
            this.changeList = []
            this.total = 0
          }

          this.tableEmptyText = this.changeList.length === 0 ? '暂无数据' : ''
        } else {
          this.$message.error(response ? response.message : '加载数据失败')
          this.changeList = []
          this.total = 0
          this.tableEmptyText = '加载失败，请刷新重试'
        }
      } catch (error) {
        console.error('加载变更申请失败:', error)
        this.$message.error('加载变更申请失败')
        this.changeList = []
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
        1: '待复核',
        2: '已通过',
        3: '已驳回',
        4: '重新提交待复核'
      }
      return map[status] || '未知'
    },

    getStatusColor(status) {
      const map = {
        1: 'warning',     // 待复核 - 黄色
        2: 'success',     // 已通过 - 绿色
        3: 'danger',      // 已驳回 - 红色
        4: 'info'         // 重新提交待审核 - 蓝色
      }
      return map[status] || 'info'
    },

    // 显示复核对话框
    showReviewDialog(row) {
      if (!row) {
        this.$message.error('变更数据异常')
        return
      }

      // 创建深拷贝
      this.reviewData = JSON.parse(JSON.stringify(row))
      this.reviewForm = {
        action: 2,
        comment: '',
        changedData: {}
      }

      // 初始化表单数据
      const afterData = this.reviewData.afterDataParsed || {}

      // 将变更后的数据赋值给表单
      this.reviewForm.name = afterData.name || ''
      this.reviewForm.sex = afterData.sex || 1
      this.reviewForm.idCard = afterData.idCard || ''
      this.reviewForm.title = afterData.title || ''
      this.reviewForm.salaryStandard = afterData.salaryStandard || ''
      this.reviewForm.birDate = afterData.birDate || ''
      this.reviewForm.nationality = afterData.nationality || ''
      this.reviewForm.qualification = afterData.qualification || ''
      this.reviewForm.identity = afterData.identity || ''
      this.reviewForm.belief = afterData.belief || ''
      this.reviewForm.email = afterData.email || ''
      this.reviewForm.phone = afterData.phone || ''
      this.reviewForm.telephone = afterData.telephone || ''
      this.reviewForm.address = afterData.address || ''
      this.reviewForm.zipCode = afterData.zipCode || ''
      this.reviewForm.country = afterData.country || ''
      this.reviewForm.birAddress = afterData.birAddress || ''
      this.reviewForm.major = afterData.major || ''

      // 设置changedData初始值
      Object.keys(afterData).forEach(key => {
        if (!['arcCode', 'firstOrgId', 'secondOrgId', 'thirdOrgId', 'positionName'].includes(key)) {
          this.reviewForm.changedData[key] = afterData[key]
        }
      })

      this.reviewDialogVisible = true
    },

    // 加载变更详情用于复核
    async loadChangeDetailForReview(changeId) {
      try {
        const response = await getChangeDetail(changeId)
        if (response && response.code === 200) {
          this.reviewData = response.data
        }
      } catch (error) {
        console.error('获取变更详情失败:', error)
        this.$message.error('获取变更详情失败')
      }
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

    // 获取变更字段数据
    getChangedFieldsData() {
      if (!this.reviewData) return []

      const afterData = this.reviewData.afterDataParsed || {}

      // 定义字段配置
      const fieldConfigs = [
        { field: 'arcCode', label: '档案编号', editable: false, type: 'text' },
        { field: 'name', label: '姓名', editable: false, type: 'text' },
        { field: 'sex', label: '性别', editable: false, type: 'select' },
        { field: 'idCard', label: '身份证号', editable: true, type: 'text' },
        { field: 'firstOrgId', label: '一级机构', editable: false, type: 'text' },
        { field: 'secondOrgId', label: '二级机构', editable: false, type: 'text' },
        { field: 'thirdOrgId', label: '三级机构', editable: false, type: 'text' },
        { field: 'positionName', label: '职位', editable: false, type: 'text' },
        { field: 'title', label: '职称', editable: true, type: 'text' },
        { field: 'salaryStandard', label: '薪酬标准', editable: true, type: 'text' },
        { field: 'birDate', label: '出生日期', editable: true, type: 'date' },
        { field: 'nationality', label: '民族', editable: true, type: 'text' },
        { field: 'qualification', label: '学历', editable: true, type: 'select' },
        { field: 'email', label: 'Email', editable: true, type: 'text' },
        { field: 'telephone', label: '电话', editable: true, type: 'text' },
        { field: 'phone', label: '手机', editable: true, type: 'text' },
        { field: 'address', label: '住址', editable: true, type: 'text' },
        { field: 'zipCode', label: '邮编', editable: true, type: 'text' },
        { field: 'country', label: '国籍', editable: true, type: 'text' },
        { field: 'birAddress', label: '出生地', editable: true, type: 'text' },
        { field: 'belief', label: '宗教信仰', editable: true, type: 'text' },
        { field: 'identity', label: '政治面貌', editable: true, type: 'text' },
        { field: 'major', label: '专业', editable: true, type: 'text' },
        { field: 'photoPath', label: '照片', editable: true, type: 'text' }
      ]

      return fieldConfigs.map(config => {
        let value = afterData[config.field]

        // 格式化值
        if (config.field === 'sex') {
          value = value === 1 ? '男' : value === 2 ? '女' : value
        }

        return {
          field: config.field,
          label: config.label,
          value: value || '--',
          editable: config.editable,
          type: config.type
        }
      })
    },

    // 字段值改变处理
    handleFieldChange(field, value) {
      if (!this.reviewForm.changedData) {
        this.reviewForm.changedData = {}
      }

      // 处理数据类型转换
      let processedValue = value
      if (field === 'sex') {
        processedValue = parseInt(value) || value
      } else if (field === 'salaryStandard') {
        processedValue = parseInt(value) || value
      }

      this.reviewForm.changedData[field] = processedValue
      console.log(`字段 ${field} 修改为:`, processedValue, '类型:', typeof processedValue)
    },

    // 获取字段显示名称
    getFieldLabel(field) {
      const map = {
        'arcCode': '档案编号',
        'name': '姓名',
        'sex': '性别',
        'idCard': '身份证号',
        'firstOrgId': '一级机构',
        'secondOrgId': '二级机构',
        'thirdOrgId': '三级机构',
        'positionName': '职位',
        'title': '职称',
        'salaryStandard': '薪酬标准',
        'birDate': '出生日期',
        'nationality': '民族',
        'qualification': '学历',
        'identity': '政治面貌',
        'belief': '宗教信仰',
        'email': 'Email',
        'phone': '手机',
        'telephone': '电话',
        'address': '住址',
        'zipCode': '邮编',
        'country': '国籍',
        'birAddress': '出生地',
        'major': '专业',
        'photoPath': '照片'
      }
      return map[field] || field
    },

    // 格式化字段值
    formatFieldValue(field, value) {
      if (value === null || value === undefined) return '空'

      // 特殊字段处理
      if (field === 'sex') {
        return value === 1 ? '男' : value === 2 ? '女' : value
      }

      return String(value)
    },

    // 获取机构路径
    getOrgPath(archive) {
      if (!archive) return '--'

      // 检查是否直接有机构名称字段
      if (archive.firstOrgName || archive.secondOrgName || archive.thirdOrgName) {
        const first = archive.firstOrgName || ''
        const second = archive.secondOrgName || ''
        const third = archive.thirdOrgName || ''
        return `${first} / ${second} / ${third}`
      }

      // 检查是否有机构对象
      if (archive.archive && (archive.archive.firstOrgName || archive.archive.secondOrgName || archive.archive.thirdOrgName)) {
        const first = archive.archive.firstOrgName || ''
        const second = archive.archive.secondOrgName || ''
        const third = archive.archive.thirdOrgName || ''
        return `${first} / ${second} / ${third}`
      }

      return '--'
    },

    // 提交复核
    async handleReviewSubmit() {
      try {
        if (!this.reviewData) {
          this.$message.error('变更数据异常')
          return
        }

        // 验证表单
        if (this.reviewForm.action === 3 && !this.reviewForm.comment.trim()) {
          this.$message.error('请填写驳回原因')
          return
        }

        this.reviewSubmitting = true

        // 构建提交数据 - 根据后端接口要求的数据结构
        const submitData = {
          changeId: this.reviewData.changeId,
          action: this.reviewForm.action,
          reviewComment: this.reviewForm.comment,
          changedData: this.reviewForm.changedData || null
        }

        console.log('提交复核数据:', JSON.stringify(submitData, null, 2))

        // 调用复核接口
        const response = await reviewChange(submitData)

        console.log('复核接口响应:', response)

        if (response && response.code === 200) {
          this.$message.success(response.message || '复核成功')
          this.reviewDialogVisible = false
          this.loadData()
        } else {
          this.$message.error(response ? (response.message || '复核失败') : '复核失败')
        }
      } catch (error) {
        console.error('复核失败:', error)
        console.error('错误详情:', error.response || error.message)

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

    // 显示详情
    async showDetail(row) {
      try {
        this.loading = true
        this.currentChange = null
        this.processList = []

        // 获取变更详情
        const detailResponse = await getChangeDetail(row.changeId)
        if (detailResponse && detailResponse.code === 200) {
          this.currentChange = detailResponse.data
        } else {
          this.$message.error('获取变更详情失败')
          return
        }

        // 获取审核流程
        const processResponse = await getChangeProcess(row.changeId)
        if (processResponse && processResponse.code === 200) {
          this.processList = processResponse.data
        }

        this.detailDialogVisible = true
      } catch (error) {
        console.error('获取变更详情失败:', error)
        this.$message.error('获取变更详情失败')
      } finally {
        this.loading = false
      }
    },

    // 详情对话框关闭处理
    handleDetailDialogClose() {
      this.currentChange = null
      this.processList = []
    },

    // 获取详情变更字段数据
    getDetailChangedFieldsData() {
      if (!this.currentChange) return []

      const changedFields = this.currentChange.changedFieldsParsed || []
      const beforeData = this.currentChange.beforeDataParsed || {}
      const afterData = this.currentChange.afterDataParsed || {}

      return changedFields.map(field => {
        const beforeValue = this.formatFieldValue(field, beforeData[field])
        const afterValue = this.formatFieldValue(field, afterData[field])
        const isChanged = beforeValue !== afterValue

        return {
          field: field,
          beforeValue: beforeValue,
          afterValue: afterValue,
          changed: isChanged
        }
      })
    },

    // 流程类型文本和颜色
    getProcessTypeText(type) {
      const map = {
        1: '提交变更申请',
        2: '审核通过',
        3: '审核驳回',
        4: '重新提交'
      }
      return map[type] || '未知操作'
    },

    getProcessColor(type) {
      const map = {
        1: '#E6A23C', // 提交 - 橙色
        2: '#67C23A', // 通过 - 绿色
        3: '#F56C6C', // 驳回 - 红色
        4: '#409EFF'  // 重新提交 - 蓝色
      }
      return map[type] || '#409EFF'
    },

    getRowClassName({ row }) {
      return row.editable ? 'editable-row' : 'locked-row'
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

.editable-row {
  background-color: #f0f9ff;
}

.locked-row {
  background-color: #f5f7fa;
}

::v-deep .editable-row:hover {
  background-color: #e6f7ff;
}

::v-deep .locked-row:hover {
  background-color: #ebeef5;
}
</style>