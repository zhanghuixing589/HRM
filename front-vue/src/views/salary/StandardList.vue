<template>
  <div class="standard-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>薪酬标准管理</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item to="/salary/manage">薪酬管理</el-breadcrumb-item>
        <el-breadcrumb-item>薪酬标准管理</el-breadcrumb-item>
      </el-breadcrumb>
      <div class="header-action" style="float: right; margin-top: -42px;">
        <el-button icon="el-icon-back" @click="handleBack">
          返回
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" ref="searchForm" label-width="100px" inline>
        <el-form-item label="标准编码">
          <el-input 
            v-model="searchForm.standardCode" 
            placeholder="请输入标准编码" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="标准名称">
          <el-input 
            v-model="searchForm.standardName" 
            placeholder="请输入标准名称" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="标准状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择状态" 
            clearable
            style="width: 150px"
          >
            <el-option label="草稿" :value="1" />
            <el-option label="待审核" :value="2" />
            <el-option label="已生效" :value="3" />
            <el-option label="已驳回" :value="0" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">
            查询
          </el-button>
          <el-button icon="el-icon-refresh" @click="resetSearch">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 操作按钮区域 -->
    <div class="action-bar">
      <el-button 
        type="primary" 
        icon="el-icon-plus" 
        @click="handleCreate"
        v-if="canCreate"
      >
        新建标准
      </el-button>
      <el-button 
        type="danger" 
        icon="el-icon-delete" 
        @click="handleBatchDelete"
        :disabled="selectedRows.length === 0"
        v-if="canDelete"
      >
        批量删除
      </el-button>
      
      <div class="action-right">
        <el-button 
          icon="el-icon-refresh" 
          @click="refreshData"
          :loading="loading"
        >
          刷新
        </el-button>
      </div>
    </div>
    
    <!-- 数据表格 -->
    <el-card shadow="never">
      <el-table
        :data="tableData"
        border
        stripe
        style="width: 100%"
        v-loading="loading"
        element-loading-text="数据加载中..."
        @selection-change="handleSelectionChange"
      >
        <el-table-column 
          type="selection" 
          width="55" 
          align="center" 
          v-if="canDelete"
        />
        
        <el-table-column label="标准编码" prop="standardCode" width="180" fixed="left">
          <template slot-scope="scope">
            <el-link 
              type="primary" 
              @click="handleView(scope.row)"
              :underline="false"
            >
              {{ scope.row.standardCode }}
            </el-link>
          </template>
        </el-table-column>
        
        <el-table-column label="标准名称" prop="standardName" min-width="200">
          <template slot-scope="scope">
            <div class="standard-name">
              <span>{{ scope.row.standardName }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="关联职位" width="180">
          <template slot-scope="scope">
            <el-popover 
              placement="right" 
              width="300" 
              trigger="hover"
              v-if="scope.row.positions && scope.row.positions.length > 0"
            >
              <div class="position-list">
                <div v-for="position in scope.row.positions" :key="position.posId" class="position-item">
                  <el-tag size="mini" type="info">
                    {{ position.posCode }}
                  </el-tag>
                  <span>{{ position.posName }}</span>
                </div>
              </div>
              <div slot="reference" class="position-trigger">
                <el-tag size="small" type="info">
                  {{ scope.row.positions.length }}个职位
                </el-tag>
                <i class="el-icon-view" style="margin-left: 5px; color: #409eff;"></i>
              </div>
            </el-popover>
            <span v-else style="color: #c0c4cc;">--</span>
          </template>
        </el-table-column>
        
        <el-table-column label="当前状态" width="120" align="center">
          <template slot-scope="scope">
            <el-tag :type="getStatusTagType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="创建人" prop="creatorName" width="120" />
        
        <el-table-column label="创建时间" width="180">
          <template slot-scope="scope">
            <span v-if="scope.row.createdAt">
              {{ formatDate(scope.row.createdAt) }}
            </span>
            <span v-else style="color: #c0c4cc;">--</span>
          </template>
        </el-table-column>
        
        <el-table-column label="登记人" prop="registrarName" width="120">
          <template slot-scope="scope">
            <span v-if="scope.row.registrarName">
              {{ scope.row.registrarName }}
            </span>
            <span v-else style="color: #c0c4cc;">--</span>
          </template>
        </el-table-column>
        
        <el-table-column label="登记时间" width="180">
          <template slot-scope="scope">
            <span v-if="scope.row.registrationTime">
              {{ formatDate(scope.row.registrationTime) }}
            </span>
            <span v-else style="color: #c0c4cc;">--</span>
          </template>
        </el-table-column>
        
        <!-- 操作列 -->
        <el-table-column label="操作" width="350" fixed="right" align="center">
          <template slot-scope="scope">
            
            <!-- 编辑按钮 -->
            <el-button 
              v-if="canEditStandard(scope.row)"
              type="primary"
              size="mini"
              icon="el-icon-edit"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            
            <!-- 提交审核按钮 -->
            <el-button 
              v-if="canSubmitStandard(scope.row)"
              type="warning"
              size="mini"
              icon="el-icon-upload2"
              @click="handleSubmit(scope.row)"
            >
              提交审核
            </el-button>
            
            <!-- 审核按钮 -->
            <el-button
              v-if="scope.row.status === 2 && hasApprovePermission"
              type="success"
              size="mini"
              icon="el-icon-check"
              @click="handleApprove(scope.row)"
            >
              审核
            </el-button>
            
            <!-- 查看审核历史按钮 -->
            <el-button
              v-if="hasViewApprovalHistoryPermission"
              type="info"
              size="mini"
              icon="el-icon-time"
              @click="handleViewApprovalHistory(scope.row)"
            >
              审核历史
            </el-button>
            
            <!-- 删除按钮 -->
            <el-button 
              v-if="canDeleteStandard(scope.row)"
              type="danger"
              size="mini"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 空状态 -->
      <div v-if="tableData.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无数据">
          <el-button 
            type="primary" 
            @click="handleCreate"
            v-if="canCreate"
          >
            创建第一个标准
          </el-button>
        </el-empty>
      </div>
      
      <!-- 分页 -->
      <div v-if="tableData.length > 0" class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          background
        />
      </div>
    </el-card>

    <!-- 提交审核对话框 -->
    <el-dialog
      title="提交审核"
      :visible.sync="submitDialog.visible"
      width="500px"
      @close="handleSubmitDialogClose"
    >
      <el-form :model="submitDialog.form" ref="submitForm">
        <el-form-item label="提交备注" prop="remark">
          <el-input
            v-model="submitDialog.form.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入提交备注（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="submitDialog.visible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleSubmitConfirm"
          :loading="submitDialog.loading"
        >
          确定提交
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getAllStandards, 
  deleteStandard, 
  batchDeleteStandards,
  submitForApproval,
  
} from '@/api/salary/standards'

export default {
  name: 'StandardList',
  data() {
    return {
      // 搜索表单
      searchForm: {
        standardCode: '',
        standardName: '',
        status: null
      },
      
      // 表格数据
      tableData: [],
      selectedRows: [],
      standards: [],
      
      // 分页
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      },
      
      // 加载状态
      loading: false,
      
      // 提交审核对话框
      submitDialog: {
        visible: false,
        loading: false,
        currentRow: null,
        form: {
          remark: ''
        }
      }
    }
  },
  
  computed: {
    // 是否有审核权限（财务经理=2, 管理员=1）
    hasApprovePermission() {
      return [1, 3].includes(this.userRole)
    },
    
    // 是否可以查看审核历史（所有管理角色）
    hasViewApprovalHistoryPermission() {
      return [1, 2, 3, 5].includes(this.userRole)
    },
    
    // 当前用户信息
    user() {
      try {
        const userStr = localStorage.getItem('user') || localStorage.getItem('userInfo')
        if (userStr) {
          return JSON.parse(userStr)
        }
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
      return {}
    },
    
    // 当前用户角色
    userRole() {
      return this.user.roleType || parseInt(localStorage.getItem('userRole') || '6')
    },
    
    // 权限判断 - 根据角色类型
    isRole1() {
      return this.userRole === 1 // 超级管理员
    },
    isRole2() {
      return this.userRole === 2 // 财务经理
    },
    isRole3() {
      return this.userRole === 3 // 部门经理/主管
    },
    isRole5() {
      return this.userRole === 5 // 薪酬专员
    },
    
    // 是否可以管理薪酬标准（增删改）
    canManageSalaryStandard() {
      return [1, 3, 5].includes(this.userRole)
    },
    
    // 是否可以创建标准
    canCreate() {
      return this.canManageSalaryStandard
    },
    
    // 是否可以批量删除
    canDelete() {
      return this.canManageSalaryStandard
    },
    
    // 是否可以审核
    canApprove() {
      return [1, 2].includes(this.userRole) // 管理员和财务经理可以审核
    }
  },
  
  created() {

    console.log('====== StandardList 组件创建 ======')
    console.log('当前路由:', this.$route.path)
    
    // 检查权限
    const canView = [1, 2, 3, 4, 5].includes(this.userRole)
    console.log('是否有权限:', canView)
    
    if (!canView) {
      console.log('权限不足，跳转到 dashboard')
      this.$message.warning('您没有权限访问薪酬标准管理页面')
      this.$router.push('/dashboard')
      return
    }
    
    console.log('权限通过，开始加载数据')
    this.loadData()
    console.log('loadData 调用完成')
  },
  
  methods: {
    // 加载数据
    async loadData() {
      console.log('====== 开始加载数据 ======')
      console.log('当前用户角色:', this.userRole)
      console.log('搜索表单参数:', this.searchForm)
      this.loading = true
      
      try {
        console.log('发送 API 请求...')
        const response = await getAllStandards()
        console.log('API 响应原始数据:', response)
        console.log('API 响应类型:', typeof response)
        console.log('API 响应 success:', response.success)
        console.log('API 响应 data:', response.data)
        
        // 检查响应结构
        if (response && response.success) {
          console.log('API 调用成功')
          this.standards = response.data || []
          this.tableData = response.data || []
          console.log('standards 数据:', this.standards)
          console.log('standards 数量:', this.standards.length)
          
          // 应用过滤和分页
          this.applyFilterAndPagination()
          
          this.$message.success('数据加载成功')
        } else {
          console.log('API 调用失败或返回数据格式不正确')
          console.log('错误信息:', response?.message)
          this.standards = []
          this.tableData = []
          this.pagination.total = 0
          
          // 显示后端返回的错误信息
          if (response && response.message) {
            this.$message.error(response.message)
          } else {
            this.$message.error('获取数据失败，请检查网络连接')
          }
        }
      } catch (error) {
        console.error('加载数据失败 - 详细错误:', error)
        console.error('错误名称:', error.name)
        console.error('错误消息:', error.message)
        console.error('错误堆栈:', error.stack)
        
        // 检查是否有响应数据
        if (error.response) {
          console.error('错误状态码:', error.response.status)
          console.error('错误响应数据:', error.response.data)
          this.$message.error(`服务器错误: ${error.response.status} - ${error.response.data?.message || '未知错误'}`)
        } else if (error.request) {
          console.error('请求已发出但无响应:', error.request)
          this.$message.error('网络请求失败，请检查网络连接')
        } else {
          console.error('请求配置错误:', error.message)
          this.$message.error('请求配置错误: ' + error.message)
        }
        
        this.standards = []
        this.tableData = []
        this.pagination.total = 0
      } finally {
        console.log('加载完成，loading 状态改为 false')
        this.loading = false
      }
    },
    
    // 应用过滤和分页
    applyFilterAndPagination() {
      // 如果 standards 为空，直接返回
      if (!this.standards || this.standards.length === 0) {
        console.log('没有数据需要过滤')
        this.tableData = []
        this.pagination.total = 0
        return
      }
      
      // 1. 过滤数据
      let filteredData = [...this.standards]
      
      if (this.searchForm.standardCode) {
        const code = this.searchForm.standardCode.trim()
        filteredData = filteredData.filter(item => 
          item.standardCode && item.standardCode.includes(code)
        )
        console.log('按编码过滤后:', filteredData.length)
      }
      
      if (this.searchForm.standardName) {
        const name = this.searchForm.standardName.trim()
        filteredData = filteredData.filter(item => 
          item.standardName && item.standardName.includes(name)
        )
        console.log('按名称过滤后:', filteredData.length)
      }
      
      if (this.searchForm.status !== null && this.searchForm.status !== undefined) {
        filteredData = filteredData.filter(item => item.status === this.searchForm.status)
        console.log('按状态过滤后:', filteredData.length)
      }
      
      // 2. 更新分页信息
      this.pagination.total = filteredData.length
      console.log('过滤后总数:', this.pagination.total)
      
      // 3. 调整页码
      const totalPages = Math.ceil(filteredData.length / this.pagination.pageSize)
      if (this.pagination.pageNum > totalPages && totalPages > 0) {
        this.pagination.pageNum = totalPages
      } else if (totalPages === 0) {
        this.pagination.pageNum = 1
      }
      
      // 4. 应用分页
      const start = (this.pagination.pageNum - 1) * this.pagination.pageSize
      const end = start + this.pagination.pageSize
      this.tableData = filteredData.slice(start, end)
      
      console.log('当前页码:', this.pagination.pageNum)
      console.log('每页大小:', this.pagination.pageSize)
      console.log('分页后数据:', this.tableData.length)
    },
    
    // ========== 页面操作 ==========
    
    // 处理返回
    handleBack() {
      this.$router.push('/salary/manage')
    },
    
    // 查询
    handleSearch() {
      this.pagination.pageNum = 1
      this.loadData()
    },
    
    // 重置搜索
    resetSearch() {
      this.$refs.searchForm.resetFields()
      this.pagination.pageNum = 1
      this.loadData()
    },
    
    // 刷新数据
    refreshData() {
      this.loadData()
    },
    
    // 表格选择变化
    handleSelectionChange(selection) {
      this.selectedRows = selection
    },
    
    // 分页大小变化
    handleSizeChange(size) {
      this.pagination.pageSize = size
      this.pagination.pageNum = 1
      this.loadData()
    },
    
    // 页码变化
    handleCurrentChange(page) {
      this.pagination.pageNum = page
      this.loadData()
    },
    
    // ========== 具体操作 ==========
    
    // 查看详情
    handleView(row) {
      // 跳转到查看页面（只读模式）
      this.$router.push({
        path: '/salary/standards/create',
        query: { 
          id: row.standardId, 
          mode: 'view',
          readonly: 'true' 
        }
      })
    },
    
    // 创建标准
    handleCreate() {
      if (!this.canCreate) {
        this.$message.warning('您没有权限创建薪酬标准')
        return
      }
      this.$router.push('/salary/standards/create')
    },
    
    // 编辑标准
    handleEdit(row) {
      if (!this.canEditStandard(row)) {
        this.$message.warning('您没有权限编辑此标准')
        return
      }
      this.$router.push({
        path: '/salary/standards/create',
        query: { id: row.standardId, mode: 'edit' }
      })
    },

    
    
    // 提交审核
    handleSubmit(row) {
      if (!this.canSubmitStandard(row)) {
        this.$message.warning('当前状态不能提交审核')
        return
      }
      
      this.submitDialog = {
        visible: true,
        loading: false,
        currentRow: row,
        form: { remark: '' }
      }
    },
    
    // 处理审核操作
    handleApprove(row) {
      this.$router.push({
        name: 'StandardApprovalDetail',
        params: { id: row.standardId }
      })
    },
    
    // 查看审核历史
    handleViewApprovalHistory(row) {
      this.$router.push({
        name: 'ApprovalHistory',
        params: { id: row.standardId }
      })
    },
    
    // 删除标准
    async handleDelete(row) {
      if (!this.canDeleteStandard(row)) {
        this.$message.warning('您没有权限删除此标准')
        return
      }
      
      try {
        await this.$confirm(`确定删除标准 "${row.standardName}" 吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await deleteStandard(row.standardId)
        this.$message.success('删除成功')
        this.loadData()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          this.$message.error('删除失败: ' + (error.response?.data?.message || error.message))
        }
      }
    },
    
    // 批量删除
    async handleBatchDelete() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请选择要删除的数据')
        return
      }
      
      // 检查权限
      if (!this.canDelete) {
        this.$message.warning('您没有批量删除权限')
        return
      }
      
      // 检查是否包含不能删除的数据
      const canDeleteRows = this.selectedRows.filter(row => this.canDeleteStandard(row))
      if (canDeleteRows.length !== this.selectedRows.length) {
        this.$message.warning('选中的数据中包含不可删除的标准')
        return
      }
      
      try {
        await this.$confirm(`确定删除选中的 ${this.selectedRows.length} 个标准吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const ids = this.selectedRows.map(row => row.standardId)
        await batchDeleteStandards(ids)
        this.$message.success('批量删除成功')
        this.selectedRows = []
        this.loadData()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量删除失败:', error)
          this.$message.error('批量删除失败: ' + (error.response?.data?.message || error.message))
        }
      }
    },
    
    // ========== 对话框操作 ==========
    
    // 提交审核对话框关闭
    handleSubmitDialogClose() {
      this.submitDialog = {
        visible: false,
        loading: false,
        currentRow: null,
        form: { remark: '' }
      }
    },
    
    // 提交审核确认
    async handleSubmitConfirm() {
      try {
        this.submitDialog.loading = true
        
        const { currentRow, form } = this.submitDialog
        
        // 提交审核
        await submitForApproval(currentRow.standardId, form.remark)
        this.$message.success('提交审核成功')
        
        this.submitDialog.visible = false
        this.loadData()
      } catch (error) {
        console.error('提交审核失败:', error)
        this.$message.error('提交审核失败: ' + (error.response?.data?.message || error.message))
      } finally {
        this.submitDialog.loading = false
      }
    },
    
    // ========== 权限判断方法 ==========
    
    // 是否可以编辑标准
    canEditStandard(row) {
      // 角色5：可以编辑草稿(1)或驳回(0)状态
      if (this.userRole === 5) {
        return row.status === 1 || row.status === 0
      }
      
      // 角色3：不可以编辑
      if (this.userRole === 3) {
        return false
      }
      
      // 角色1：可以编辑所有状态
      if (this.userRole === 1) {
        return [0, 1, 2, 3].includes(row.status)
      }
      
      return false
    },
    
    // 是否可以提交审核
    canSubmitStandard(row) {
      // 角色5：可以编辑草稿(1)或驳回(0)状态
      if (this.userRole === 5) {
        return row.status === 1 || row.status === 0
      }
      
      // 角色1：可以编辑所有状态
      if (this.userRole === 1) {
        return [0, 1, 2].includes(row.status)
      }
      
      return false
    },
    
    // 是否可以删除
    canDeleteStandard(row) {
      if (this.userRole === 5) {
        return row.status === 1 || row.status === 0
      }
      
      if (this.userRole === 1) {
        return [0, 1, 2, 3].includes(row.status)
      }
      
      return false
    },
    
    // ========== 工具方法 ==========
    
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
    
    // 格式化日期
    formatDate(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    }
  }
}
</script>

<style scoped>
.standard-list {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
  background: white;
  padding: 20px;
  border-radius: 4px;
  position: relative;
}

.page-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
}

.search-card {
  margin-bottom: 20px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px 20px;
  background: white;
  border-radius: 4px;
}

.action-right {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
  padding: 20px 0;
}

.empty-state {
  padding: 60px 0;
}

.standard-name {
  font-weight: 500;
  color: #303133;
}

.position-list {
  max-height: 300px;
  overflow-y: auto;
}

.position-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}

.position-item:last-child {
  border-bottom: none;
}

.position-item .el-tag {
  margin-right: 8px;
  flex-shrink: 0;
}

.position-trigger {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.position-trigger:hover .el-tag {
  border-color: #409eff;
  color: #409eff;
}

.dialog-footer {
  text-align: right;
}
</style>