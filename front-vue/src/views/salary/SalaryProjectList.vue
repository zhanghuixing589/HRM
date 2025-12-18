<template>
  <div class="salary-project-list">
    <div class="page-header">
      <h2>薪酬项目管理</h2>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" @click="handleCreate" v-if="[3,1].includes(userInfo.roleType)"  >
          新增项目
        </el-button>
        <el-button :disabled="!selectedRows.length" @click="handleBatchDelete" v-if="[3,1].includes(userInfo.roleType)">
          批量删除
        </el-button>
        <el-button icon="el-icon-refresh" @click="fetchProjectList">
          刷新
        </el-button>
        <el-button icon="el-icon-back" @click="handleBack">
          返回
        </el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :inline="true" :model="searchForm" size="small">
        <el-form-item label="项目名称">
          <el-input
            v-model="searchForm.projectName"
            placeholder="请输入项目名称"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="项目类型">
          <el-select
            v-model="searchForm.projectType"
            placeholder="请选择项目类型"
            clearable
            @change="handleSearch"
          >
            <el-option
              v-for="type in projectTypes"
              :key="type.code"
              :label="type.name"
              :value="type.code"
            />
          </el-select>
        </el-form-item>
        <!-- 在搜索表单中添加 -->
<el-form-item label="状态">
  <el-select
    v-model="searchForm.status"
    placeholder="请选择状态"
    clearable
    @change="handleSearch"
  >
    <el-option label="启用" :value="1" />
    <el-option label="禁用" :value="0" />
  </el-select>
</el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        :data="projectList"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        stripe
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="projectCode" label="项目编码" width="120" />
        <el-table-column prop="projectName" label="项目名称" min-width="150" />
        <el-table-column prop="projectType" label="项目类型" width="100">
          <template slot-scope="scope">
            <el-tag
              :type="scope.row.projectType === 1 ? 'success' : 'danger'"
              size="small"
            >
              {{ getProjectTypeName(scope.row.projectType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="项目类别" width="120" />
        <el-table-column prop="calculationMethod" label="计算方法" width="120" />
        <el-table-column prop="sortOrder" label="排序顺序" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template slot-scope="scope">
            <el-tag
              :type="scope.row.status === 1 ? 'success' : 'danger'"
              size="small"
            >
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" v-if="[3,1].includes(userInfo.roleType)">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="handleEdit(scope.row)" v-if="[3,1].includes(userInfo.roleType)"
            >
              编辑
            </el-button>
            <el-button
              type="text"
              size="small"
              @click="scope.row.status === 1 ? handleDisable(scope.row) : handleEnable(scope.row)"
              :style="scope.row.status === 1 ? 'color: #f56c6c' : 'color: #67c23a'" v-if="[3,1].includes(userInfo.roleType)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              type="text"
              size="small"
              @click="handleDelete(scope.row)"
              style="color: #f56c6c"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
        :current-page="pagination.pageNum"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script>
import { 
  getAllProjects,
  deleteProject,
  batchDeleteProjects,
  getProjectEnums,
  enableProject,
  disableProject
} from '@/api/salary/projects'

export default {
  name: 'SalaryProjectList',
  data() {
    return {
      // 搜索表单
      searchForm: {
        projectName: '',
        projectType: null,
        status: null
      },
      
      // 表格数据
      allProjects: [],
      projectList: [],
      selectedRows: [],
      loading: false,
      
      // 分页
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      },
        userInfo: {},
      // 枚举数据
      projectTypes: [],
      projectCategories: [],
      calculationMethods: []
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
    this.fetchEnums()
    this.fetchProjectList()
    this.loadUserInfo()
  },
  
  methods: {
    //加载用户信息
    loadUserInfo() {
      const data = localStorage.getItem('user')
      if(data){
        this.userInfo = JSON.parse(data)
      }
    },
    logout(){
      localStorage.clear()
      this.$router.replace('/')
    },
    // 获取枚举数据
    async fetchEnums() {
      try {
        const response = await getProjectEnums()
        if (response.success) {
          this.projectTypes = response.data.projectTypes || []
          this.projectCategories = response.data.projectCategories || []
          this.calculationMethods = response.data.calculationMethods || []
        }
      } catch (error) {
        console.error('获取枚举数据失败:', error)
      }
    },

    // 获取项目列表（前端分页）
    async fetchProjectList() {
      this.loading = true
      try {
        const response = await getAllProjects()
        
        console.log('API响应:', response)
        
        if (response.success) {
          this.allProjects = response.data || []
          this.applyFilterAndPagination()
        } else {
          this.$message.error(response.message || '获取失败')
          this.allProjects = []
          this.projectList = []
          this.pagination.total = 0
        }
      } catch (error) {
        console.error('获取项目列表失败:', error)
        this.$message.error('获取项目列表失败: ' + (error.message || '网络错误'))
        this.allProjects = []
        this.projectList = []
        this.pagination.total = 0
      } finally {
        this.loading = false
      }
    },
    
    // 应用搜索过滤和分页
    applyFilterAndPagination() {
      // 1. 应用搜索过滤
      let filteredData = this.allProjects
      
      if (this.searchForm.projectName) {
        const keyword = this.searchForm.projectName.toLowerCase()
        filteredData = filteredData.filter(item => 
          item.projectName && item.projectName.toLowerCase().includes(keyword)
        )
      }
      
      if (this.searchForm.projectType) {
        filteredData = filteredData.filter(item => 
          item.projectType === this.searchForm.projectType
        )
      }
      
      if (this.searchForm.status !== null) {
        filteredData = filteredData.filter(item => 
          item.status === this.searchForm.status
        )
      }
      
      // 2. 更新总记录数
      this.pagination.total = filteredData.length
      
      // 3. 确保当前页码有效
      const totalPages = Math.ceil(filteredData.length / this.pagination.pageSize)
      if (this.pagination.pageNum > totalPages && totalPages > 0) {
        this.pagination.pageNum = totalPages
      } else if (totalPages === 0) {
        this.pagination.pageNum = 1
      }
      
      // 4. 计算分页数据
      const start = (this.pagination.pageNum - 1) * this.pagination.pageSize
      const end = start + this.pagination.pageSize
      
      // 5. 设置当前页显示的数据
      this.projectList = filteredData.slice(start, end)
    },
    
    // 处理搜索
    handleSearch() {
      this.pagination.pageNum = 1
      this.applyFilterAndPagination()
    },
    
    // 处理重置
    handleReset() {
      this.searchForm = {
        projectName: '',
        projectType: null,
        status: null
      }
      this.pagination.pageNum = 1
      this.applyFilterAndPagination()
    },
    
    // 处理创建
    handleCreate() {
      this.$router.push('/salary/projects/create')
    },

    // 处理编辑 - 跳转到创建页面并传递ID
    handleEdit(row) {
      this.$router.push({
        path: '/salary/projects/create',
        query: { id: row.projectId }
      })
    },

    // 处理返回
    handleBack() {
      this.$router.push('/salary/manage')
    },
    
    // 处理删除
    handleDelete(row) {
      this.$confirm('确定要删除该薪酬项目吗？', '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(async () => {
        try {
          const response = await deleteProject(row.projectId)
          if (response.success) {
            this.$message.success('删除成功')
            this.fetchProjectList()
          }
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error(error.response?.data?.message || '删除失败')
        }
      }).catch(() => {
        console.log('用户取消了删除操作')
      })
    },
    
    // 处理启用
    async handleEnable(row) {
      try {
        const response = await enableProject(row.projectId)
        if (response.success) {
          this.$message.success('启用成功')
          this.fetchProjectList()
        }
      } catch (error) {
        console.error('启用失败:', error)
        this.$message.error(error.response?.data?.message || '启用失败')
      }
    },
    
    // 处理禁用
    async handleDisable(row) {
      try {
        const response = await disableProject(row.projectId)
        if (response.success) {
          this.$message.success('禁用成功')
          this.fetchProjectList()
        }
      } catch (error) {
        console.error('禁用失败:', error)
        this.$message.error(error.response?.data?.message || '禁用失败')
      }
    },
    
    // 处理批量删除
    handleBatchDelete() {
      if (!this.selectedRows.length) {
        this.$message.warning('请选择要删除的项目')
        return
      }
      
      this.$confirm(`确定要删除选中的 ${this.selectedRows.length} 个项目吗？`, '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(async () => {
        try {
          const projectIds = this.selectedRows.map(row => row.projectId)
          const response = await batchDeleteProjects(projectIds)
          if (response.success) {
            this.$message.success('批量删除成功')
            this.selectedRows = []
            this.fetchProjectList()
          }
        } catch (error) {
          console.error('批量删除失败:', error)
          this.$message.error(error.response?.data?.message || '批量删除失败')
        }
      }).catch(() => {
        console.log('用户取消了删除操作')
      })
    },
    
    // 处理分页大小变化
    handleSizeChange(size) {
      this.pagination.pageSize = size
      this.pagination.pageNum = 1
      this.applyFilterAndPagination()
    },
    
    // 处理页码变化
    handleCurrentChange(page) {
      this.pagination.pageNum = page
      this.applyFilterAndPagination()
    },
    
    // 处理行选择
    handleSelectionChange(selection) {
      this.selectedRows = selection
    },
    
    // 获取项目类型名称
    getProjectTypeName(type) {
      const typeObj = this.projectTypes.find(t => t.code === type)
      return typeObj ? typeObj.name : '未知'
    },
    
    // 格式化日期
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.salary-project-list {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.table-container {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>