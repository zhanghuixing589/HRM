<template>
  <div>
    <el-card shadow="never">
      <div slot="header" class="table-header">
        <span>我的档案提交记录</span>
      </div>

      <el-card shadow="never" style="margin-bottom: 20px;">
        <div slot="header">
          <span>查询条件</span>
        </div>
        <el-form :model="searchForm" label-width="100px" size="small">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="档案编号">
                <el-input v-model="searchForm.arcCode" placeholder="请输入档案编号" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="姓名">
                <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="职位">
                <el-input v-model="searchForm.positionName" placeholder="请输入职位" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="状态">
                <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%;">
                  <el-option label="全部" :value="null" />
                  <el-option label="待复核" :value="1" />
                  <el-option label="已通过" :value="2" />
                  <el-option label="已驳回" :value="3" />
                  <el-option label="重新提交待审核" :value="4" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24" style="text-align: center;">
              <el-button type="primary" @click="handleSearch" icon="el-icon-search">查询</el-button>
              <el-button @click="handleReset" icon="el-icon-refresh">重置</el-button>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <el-table :data="list" stripe v-loading="loading" size="small" style="width: 100%">
        <el-table-column prop="arcCode" label="档案编号" width="150" align="center" header-align="center" />
        <el-table-column prop="name" label="姓名" width="100" align="center" header-align="center" />
        <el-table-column label="所属机构" min-width="200" align="center" header-align="center">
          <template slot-scope="scope">
            {{ getOrgPath(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column prop="positionName" label="职位" min-width="150" align="center" header-align="center" />
        <el-table-column label="状态" width="110" align="center" header-align="center">
          <template slot-scope="scope">
            <el-tag :type="statusColor(scope.row.status)" size="small" style="min-width: 80px;">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="180" align="center" header-align="center">
          <template slot-scope="scope">
            {{ scope.row.submitTime | date }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template slot-scope="scope">
            <!-- 已通过状态显示变更按钮 -->
            <el-button v-if="scope.row.status === 2" type="warning" size="mini" @click="handleChange(scope.row)"
              style="margin-right: 5px; margin-bottom: 5px;">
              变更
            </el-button>

            <!-- 已驳回状态显示重新提交按钮 -->
            <el-button v-if="scope.row.status === 3" type="primary" size="mini" @click="handleResubmit(scope.row)"
              style="margin-bottom: 5px;">
              重新提交
            </el-button>

            <!-- 详情按钮 -->
            <el-button type="text" size="mini" @click="showDetail(scope.row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-if="total > 0" :current-page="query.page + 1" :page-size="query.size" :total="total"
        layout="total, sizes, prev, pager, next, jumper" :page-sizes="[5, 10, 20, 50]" @size-change="handleSizeChange"
        @current-change="handleCurrentChange" style="margin-top: 20px; text-align: right;" />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog title="档案详情 & 审核进度" :visible.sync="show" width="900px" top="5vh" :close-on-click-modal="false">
      <div v-if="row">
        <el-descriptions :column="2" size="medium" border class="archive-details">
          <el-descriptions-item label="档案编号" label-class-name="desc-label">
            {{ row.arcCode }}
          </el-descriptions-item>
          <el-descriptions-item label="姓名" label-class-name="desc-label">
            {{ row.name }}
          </el-descriptions-item>
          <el-descriptions-item label="性别" label-class-name="desc-label">
            {{ row.sex === 1 ? '男' : '女' }}
          </el-descriptions-item>
          <el-descriptions-item label="身份证号" label-class-name="desc-label">
            {{ row.idCard }}
          </el-descriptions-item>
          <el-descriptions-item label="一级机构" label-class-name="desc-label">
            {{ row.firstOrgName || row.firstOrgId }}
          </el-descriptions-item>
          <el-descriptions-item label="二级机构" label-class-name="desc-label">
            {{ row.secondOrgName || row.secondOrgId }}
          </el-descriptions-item>
          <el-descriptions-item label="三级机构" label-class-name="desc-label">
            {{ row.thirdOrgName || row.thirdOrgId }}
          </el-descriptions-item>
          <el-descriptions-item label="职位" label-class-name="desc-label">
            {{ row.positionName }}
          </el-descriptions-item>
          <el-descriptions-item label="职称" label-class-name="desc-label">
            {{ row.title }}
          </el-descriptions-item>
          <el-descriptions-item label="薪酬标准" label-class-name="desc-label">
            {{ row.salaryStandardName || row.salaryStandard }}
          </el-descriptions-item>
          <el-descriptions-item label="出生日期" label-class-name="desc-label">
            {{ row.birDate }}
          </el-descriptions-item>
          <el-descriptions-item label="民族" label-class-name="desc-label">
            {{ row.nationality }}
          </el-descriptions-item>
          <el-descriptions-item label="学历" label-class-name="desc-label">
            {{ row.qualification }}
          </el-descriptions-item>
          <el-descriptions-item label="政治面貌" label-class-name="desc-label">
            {{ row.identity }}
          </el-descriptions-item>
          <el-descriptions-item label="宗教信仰" label-class-name="desc-label">
            {{ row.belief }}
          </el-descriptions-item>
          <el-descriptions-item label="Email" label-class-name="desc-label">
            {{ row.email }}
          </el-descriptions-item>
          <el-descriptions-item label="手机" label-class-name="desc-label">
            {{ row.phone }}
          </el-descriptions-item>
          <el-descriptions-item label="电话" label-class-name="desc-label">
            {{ row.telephone }}
          </el-descriptions-item>
          <el-descriptions-item label="住址" label-class-name="desc-label">
            {{ row.address }}
          </el-descriptions-item>
          <el-descriptions-item label="邮编" label-class-name="desc-label">
            {{ row.zipCode }}
          </el-descriptions-item>
          <el-descriptions-item label="国籍" label-class-name="desc-label">
            {{ row.country }}
          </el-descriptions-item>
          <el-descriptions-item label="出生地" label-class-name="desc-label">
            {{ row.birAddress }}
          </el-descriptions-item>
          <el-descriptions-item label="专业" label-class-name="desc-label">
            {{ row.major }}
          </el-descriptions-item>
          <el-descriptions-item label="登记人" label-class-name="desc-label">
            {{ row.writerName || row.writeId }}
          </el-descriptions-item>
          <el-descriptions-item label="复核人" label-class-name="desc-label" v-if="row.reviewerName || row.reviewId">
            {{ row.reviewerName || row.reviewId }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusColor(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="驳回原因" label-class-name="desc-label" v-if="row.reason">
            {{ row.reason }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 时间轴：审核进度 -->
        <h4 style="margin: 25px 0 15px 0; padding-bottom: 10px; border-bottom: 1px solid #ebeef5;">
          <i class="el-icon-time" style="margin-right: 8px; color: #409EFF;"></i>
          审核进度
        </h4>
        <el-timeline style="padding-left: 20px;">
          <el-timeline-item v-for="(op, index) in process" :key="index" :timestamp="op.operationTime | date"
            :color="getTimelineColor(op.operationType)" placement="top" size="large"
            :hide-timestamp="!op.operationTime">
            <el-card shadow="hover" body-style="padding: 12px 15px;">
              <div class="op-line">
                <span class="op-type">{{ op.operationTypeName }}</span>
                <span class="op-user">操作人：{{ op.operatorName }}</span>
              </div>
              <div v-if="op.operationComment" class="op-comment">
                {{ op.operationComment }}
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="show = false" size="medium">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getMyArchives } from '@/api/archive'
import { getArchiveProcess } from '@/api/archive'

export default {
  name: 'MyArchives',
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      query: {
        page: 0,
        size: 10
      },
      searchForm: {
        arcCode: '',
        name: '',
        positionName: '',
        status: null
      },
      show: false,
      row: null,
      process: []
    }
  },
  created() {
    this.fetch()
  },
  filters: {
    date(v) {
      if (!v) return '-'
      // 处理不同格式的时间字符串
      const date = new Date(v)
      if (isNaN(date.getTime())) {
        return v.replace('T', ' ')
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
    }
  },
  methods: {
    fetch(page = 1) {
      this.query.page = page - 1
      this.loading = true

      // 构建查询参数，包含搜索条件
      const params = {
        page: this.query.page,
        size: this.query.size
      }

      // 添加搜索条件
      if (this.searchForm.arcCode) {
        params.arcCode = this.searchForm.arcCode
      }
      if (this.searchForm.name) {
        params.name = this.searchForm.name
      }
      if (this.searchForm.positionName) {
        params.positionName = this.searchForm.positionName
      }
      if (this.searchForm.status !== null && this.searchForm.status !== undefined) {
        params.status = this.searchForm.status
      }

      console.log('查询参数:', params)

      getMyArchives(params).then(res => {
        console.log('查询结果:', res)
        if (res && res.code === 200) {
          const data = res.data
          this.list = Array.isArray(data) ? data : (data.content || data.list || [])
          this.total = data.totalElements || data.total || this.list.length
        } else {
          this.$message.error(res ? res.message : '获取档案列表失败')
          this.list = []
          this.total = 0
        }
      }).catch(error => {
        console.error('获取档案列表失败:', error)
        this.$message.error('获取档案列表失败')
        this.list = []
        this.total = 0
      }).finally(() => {
        this.loading = false
      })
    },

    // 查询方法
    handleSearch() {
      this.query.page = 0
      this.fetchData()
    },

    // 重置方法
    handleReset() {
      this.searchForm = {
        arcCode: '',
        name: '',
        positionName: '',
        status: null
      }
      this.query.page = 0
      this.fetch(1)
    },

    // 查询方法
    async fetchData() {
      try {
        this.loading = true

        // 构建查询参数
        const params = {
          page: this.query.page,
          size: this.query.size,
          writeId: this.userId // 添加当前用户ID作为查询条件
        }

        // 添加搜索条件
        if (this.searchForm.arcCode) {
          params.arcCode = this.searchForm.arcCode
        }
        if (this.searchForm.name) {
          params.name = this.searchForm.name
        }
        if (this.searchForm.positionName) {
          params.positionName = this.searchForm.positionName
        }
        if (this.searchForm.status !== null && this.searchForm.status !== undefined) {
          params.status = this.searchForm.status
        }

        console.log('查询档案参数:', params)

        // 使用getMyArchives接口
        const response = await getMyArchives(params)

        console.log('查询档案结果:', response)

        if (response && response.code === 200) {
          const data = response.data
          this.list = data.content || data.list || []
          this.total = data.totalElements || data.total || this.list.length
        } else {
          this.$message.error(response ? response.message : '获取档案列表失败')
          this.list = []
          this.total = 0
        }
      } catch (error) {
        console.error('获取档案列表失败:', error)
        this.$message.error('获取档案列表失败')
        this.list = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    // 处理变更操作
    handleChange(row) {
      if (row.status !== 2) {
        this.$message.warning('只有已通过的档案才能变更')
        return
      }

      // 跳转到档案变更申请页面
      this.$router.push({
        path: '/hr/archive/staff-change',
        query: {
          id: row.arcId,
          mode: 'change'  // 添加模式标识
        }
      }).catch(() => { })  // 捕获路由重复导航的异常
    },

    async showDetail(row) {
      this.row = row
      this.show = true

      // 获取审核流程
      getArchiveProcess(row.arcId).then(res => {
        this.process = res.data || res
      }).catch(error => {
        console.error('获取审核进度失败:', error)
        this.$message.error('获取审核进度失败')
      })
    },

    // 获取机构路径
    getOrgPath(row) {
      if (!row) return '--'

      // 优先使用机构名称
      const first = row.firstOrgName || ``
      const second = row.secondOrgName || ``
      const third = row.thirdOrgName || ``

      return `${first} / ${second} / ${third}`
    },

    // 分页处理
    handleSizeChange(size) {
      this.query.size = size
      this.query.page = 0
      this.fetchData()
    },

    handleCurrentChange(page) {
      this.query.page = page - 1
      this.fetchData()
    },

    statusText(s) {
      const map = {
        1: '待复核',
        2: '已通过',
        3: '已驳回',
        4: '重新提交待审核',
        0: '已删除'
      }
      return map[s] || '未知'
    },

    statusColor(s) {
      const map = {
        1: 'info',
        2: 'success',
        3: 'danger',
        4: 'warning',
        0: 'info'
      }
      return map[s] || 'info'
    },

    handleResubmit(row) {
      // 存储当前档案数据到本地存储，在StaffRegister页面中使用
      localStorage.setItem('resubmitArchive', JSON.stringify(row))
      // 跳转到档案登记页面
      this.$router.push({
        path: '/hr/archive/staff-register',
        query: {
          mode: 'resubmit',
          arcId: row.arcId
        }
      })
    },

    getTimelineColor(operationType) {
      const map = {
        1: '#E6A23C', // 提交 - 警告色
        2: '#67C23A', // 审核通过 - 成功色
        3: '#F56C6C', // 审核驳回 - 危险色
        4: '#409EFF', // 重新提交 - 主色
        0: '#909399', // 删除 - 信息色
        5: '#67C23A'  // 恢复 - 成功色
      }
      return map[operationType] || '#409EFF'
    }
  }
}
</script>

<style scoped lang="scss">
.table-header {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.archive-details {
  ::v-deep .desc-label {
    width: 100px;
    font-weight: bold;
    background-color: #fafafa !important;
  }
}

.op-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;

  .op-type {
    font-weight: bold;
    font-size: 14px;
    color: #303133;
  }

  .op-user {
    color: #606266;
    font-size: 12px;
  }
}

.op-comment {
  margin-top: 8px;
  padding: 8px 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

.op-time {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;

  i {
    margin-right: 4px;
  }
}

.dialog-footer {
  text-align: center;
}

/* 调整表格样式 */
::v-deep .el-table {
  .cell {
    padding: 0 10px;
  }

  th {
    background-color: #f5f7fa;
    font-weight: 600;
    color: #303133;
  }

  .el-table__row {
    &:hover {
      background-color: #f5f7fa;
    }
  }
}

/* 调整分页样式 */
::v-deep .el-pagination {
  .el-pagination__total {
    margin-right: 20px;
  }

  .el-pagination__sizes {
    margin-right: 20px;
  }
}
</style>