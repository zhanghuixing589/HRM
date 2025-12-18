<template>
  <div>
    <el-card shadow="never">
      <div slot="header" class="table-header">
        <span>我的变更记录</span>
      </div>
      <!-- 变更记录列表 -->
      <el-table :data="changeList" stripe v-loading="loading" size="small" style="width: 100%;">
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
        <el-table-column label="状态" width="100" align="center" header-align="center">
          <template slot-scope="{ row }">
            <el-tag :type="getStatusColor(row.status)" size="small" style="min-width: 80px;">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="150" align="center" header-align="center">
          <template slot-scope="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" header-align="center" fixed="right">
          <template slot-scope="{ row }">
            <!-- 已驳回状态显示重新提交按钮 -->
            <el-button v-if="row.status === 3" type="primary" size="mini" @click="handleResubmit(row)"
              style="margin-right: 5px;">
              重新提交
            </el-button>

            <!-- 待复核和已通过状态显示详情按钮 -->
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

      <!-- 当没有数据时显示提示 -->
      <div v-if="!loading && changeList.length === 0 && searched" style="text-align: center; padding: 40px;">
        <el-empty description="暂无变更记录" />
      </div>
    </el-card>

    <!-- 变更详情对话框 -->
    <el-dialog :title="`变更详情 - ${currentChange ? currentChange.changeCode : ''}`" :visible.sync="detailDialogVisible"
      width="900px" top="5vh" @close="handleDetailDialogClose">
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

        <el-table :data="getChangedFieldsData()" stripe size="small" style="width: 100%; margin-bottom: 20px;">
          <el-table-column label="字段" width="150" align="center" header-align="center">
            <template slot-scope="{ row }">
              <span style="font-weight: 500;">{{ getFieldLabel(row.field) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="变更前" min-width="200" align="center" header-align="center">
            <template slot-scope="{ row }">
              <div style="background: #f0f9eb; padding: 5px 10px; border-radius: 3px;">
                {{ row.beforeValue || '空' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="变更后" min-width="200" align="center" header-align="center">
            <template slot-scope="{ row }">
              <div style="background: #ecf5ff; padding: 5px 10px; border-radius: 3px;">
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
        <el-button v-if="currentChange && currentChange.status === 3" type="primary"
          @click="handleResubmit(currentChange)" size="medium">
          重新提交
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
// 需要创建对应的API文件
import { getMyChangeRecords, getChangeDetail, getChangeProcess } from '@/api/change'

export default {
  name: 'MyChangeRecords',
  data() {
    return {
      loading: false,
      showSearchForm: false,
      searched: false,
      changeList: [],
      total: 0,
      query: {
        page: 0,
        size: 10
      },
      queryForm: {
        archiveCode: '',
        archiveName: '',
        changeReason: '',
        status: null
      },
      createTimeRange: [],

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

    // 加载数据
    async loadData() {
      try {
        this.loading = true

        // 构建查询参数
        const params = {
          ...this.query,
          ...this.queryForm
        }

        // 处理时间范围
        if (this.createTimeRange && this.createTimeRange.length === 2) {
          params.createTimeStart = this.createTimeRange[0]
          params.createTimeEnd = this.createTimeRange[1]
        }

        // 清空空值参数
        Object.keys(params).forEach(key => {
          if (params[key] === '' || params[key] === null) {
            delete params[key]
          }
        })

        const response = await getMyChangeRecords(params)

        if (response && response.code === 200) {
          const data = response.data

          if (data && data.list) {
            this.changeList = data.list || []
            this.total = data.total || 0
          } else if (data && data.content) {
            this.changeList = data.content || []
            this.total = data.totalElements || 0
          } else {
            this.changeList = []
            this.total = 0
          }
        } else {
          this.changeList = []
          this.total = 0
          this.$message.error(response ? response.message : '获取数据失败')
        }
      } catch (error) {
        console.error('加载变更记录失败:', error)
        this.$message.error('加载变更记录失败')
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

          // 如果变更字段为空，尝试手动解析
          if ((!this.currentChange.changedFieldsParsed || this.currentChange.changedFieldsParsed.length === 0)
            && this.currentChange.beforeData && this.currentChange.afterData) {
            try {
              // 尝试对比JSON数据找出变更字段
              const beforeData = this.currentChange.beforeDataParsed || {}
              const afterData = this.currentChange.afterDataParsed || {}
              const changedFields = []

              const allFields = ['name', 'sex', 'idCard', 'title', 'salaryStandard', 'birDate',
                'nationality', 'qualification', 'email', 'phone', 'telephone',
                'address', 'zipCode', 'country', 'birAddress', 'belief',
                'identity', 'major', 'photoPath']

              allFields.forEach(field => {
                const beforeValue = beforeData[field]
                const afterValue = afterData[field]

                // 使用JSON.stringify比较对象
                if (JSON.stringify(beforeValue) !== JSON.stringify(afterValue)) {
                  changedFields.push(field)
                }
              })

              this.currentChange.changedFieldsParsed = changedFields
              console.log('手动计算的变更字段:', changedFields)
            } catch (e) {
              console.error('手动计算变更字段失败:', e)
            }
          }
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

    // 获取变更字段数据
    getChangedFieldsData() {
      if (!this.currentChange) return []

      const beforeData = this.currentChange.beforeDataParsed || {}
      const afterData = this.currentChange.afterDataParsed || {}
      const changedFields = this.currentChange.changedFieldsParsed || []

      // 如果没有明确的变更字段列表，比较所有字段
      let fieldsToShow = changedFields
      if (!changedFields || changedFields.length === 0) {
        // 比较所有可能的字段，找出有差异的字段
        fieldsToShow = []
        const allFields = [
          'arcCode', 'name', 'sex', 'idCard', 'firstOrgId', 'secondOrgId', 'thirdOrgId',
          'positionName', 'title', 'salaryStandard', 'birDate', 'nationality', 'qualification',
          'email', 'telephone', 'phone', 'address', 'zipCode', 'country', 'birAddress',
          'belief', 'identity', 'major', 'photoPath'
        ]

        allFields.forEach(field => {
          const beforeValue = beforeData[field]
          const afterValue = afterData[field]

          // 判断是否变更（考虑null/undefined和空字符串）
          const before = beforeValue === null || beforeValue === undefined ? '' : String(beforeValue)
          const after = afterValue === null || afterValue === undefined ? '' : String(afterValue)

          if (before !== after) {
            fieldsToShow.push(field)
          }
        })
      }

      // 格式化显示数据
      return fieldsToShow.map(field => {
        let beforeValue = beforeData[field]
        let afterValue = afterData[field]

        // 特殊字段格式化
        if (field === 'sex') {
          beforeValue = beforeValue === 1 ? '男' : beforeValue === 2 ? '女' : beforeValue
          afterValue = afterValue === 1 ? '男' : afterValue === 2 ? '女' : afterValue
        }

        // 机构ID转换为名称（如果可能）
        if (field === 'firstOrgId' && this.currentChange.archive && this.currentChange.archive.firstOrgName) {
          beforeValue = this.currentChange.archive.firstOrgName
          afterValue = this.currentChange.archive.firstOrgName // 机构一般不修改
        }
        if (field === 'secondOrgId' && this.currentChange.archive && this.currentChange.archive.secondOrgName) {
          beforeValue = this.currentChange.archive.secondOrgName
          afterValue = this.currentChange.archive.secondOrgName
        }
        if (field === 'thirdOrgId' && this.currentChange.archive && this.currentChange.archive.thirdOrgName) {
          beforeValue = this.currentChange.archive.thirdOrgName
          afterValue = this.currentChange.archive.thirdOrgName
        }

        return {
          field: field,
          label: this.getFieldLabel(field),
          beforeValue: beforeValue === null || beforeValue === undefined ? '空' : String(beforeValue),
          afterValue: afterValue === null || afterValue === undefined ? '空' : String(afterValue)
        }
      })
    },

    // 获取字段显示名称
    getFieldLabel(field) {
      const map = {
        'idCard': '身份证号',
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
        'photoPath': '照片',
        'title': '职称',
        'salaryStandard': '薪酬标准'
      }
      return map[field] || field
    },

    // 重新提交
    handleResubmit(row) {
      // 跳转到档案变更页面
      this.$router.push({
        path: '/hr/archive/staff-change',
        query: {
          id: row.arcId,
          changeId: row.changeId
        }
      })
    },

    // 流程类型文本和颜色
    getProcessTypeText(type) {
      const map = {
        1: '提交变更申请',
        2: '审核通过',
        3: '审核驳回',
        4: '重新提交待复核'
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