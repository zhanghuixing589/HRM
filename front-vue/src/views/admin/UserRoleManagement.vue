<template>
  <div class="user-role-management">
    <el-card shadow="never">
      <div slot="header" class="table-header">
        <span>员工角色管理</span>
        <el-button type="primary" size="small" @click="refresh" icon="el-icon-refresh">
          刷新
        </el-button>
      </div>

      <!-- 查询条件 -->
      <el-card shadow="never" style="margin-bottom: 20px;">
        <div slot="header">
          <span>查询条件</span>
        </div>
        <el-form :model="searchForm" label-width="100px" size="small" ref="searchForm">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="员工工号">
                <el-input v-model="searchForm.userCode" placeholder="请输入员工工号" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="员工姓名">
                <el-input v-model="searchForm.userName" placeholder="请输入员工姓名" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="状态">
                <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%;">
                  <el-option label="全部" :value="null" />
                  <el-option label="在职" :value="1" />
                  <el-option label="禁用" :value="2" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="角色类型">
                <el-select v-model="searchForm.roleType" placeholder="请选择角色类型" clearable style="width: 100%;">
                  <el-option label="全部" :value="null" />
                  <el-option label="人事经理" :value="2" />
                  <el-option label="薪酬经理" :value="3" />
                  <el-option label="人事专员" :value="4" />
                  <el-option label="薪酬专员" :value="5" />
                  <el-option label="普通员工" :value="6" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-form-item label="职位">
                <el-select 
                  v-model="searchForm.posId" 
                  placeholder="请选择职位" 
                  clearable 
                  filterable
                  style="width: 100%;"
                  :loading="positionLoading"
                >
                  <el-option label="全部" :value="null" />
                  <el-option
                    v-for="position in positionList"
                    :key="position.posId"
                    :label="position.posName"
                    :value="position.posId"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="18" style="text-align: center; padding-top: 40px;">
              <el-button type="primary" @click="handleSearch" icon="el-icon-search">查询</el-button>
              <el-button @click="handleReset" icon="el-icon-refresh">重置</el-button>
            </el-col>
          </el-row>
        </el-form>
      </el-card>

      <!-- 员工列表 -->
      <el-table 
        :data="list" 
        stripe 
        v-loading="loading" 
        size="small" 
        style="width: 100%"
      >
        <el-table-column prop="userCode" label="工号" width="120" align="center" header-align="center" />
        <el-table-column prop="userName" label="姓名" width="100" align="center" header-align="center" />
        <el-table-column prop="orgPath" label="所属机构" min-width="250" align="center" header-align="center">
          <template slot-scope="scope">
            <el-tooltip :content="scope.row.orgPath || '--'" placement="top" v-if="scope.row.orgPath">
              <span class="org-path">{{ scope.row.orgPath || '--' }}</span>
            </el-tooltip>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="positionName" label="职位" min-width="120" align="center" header-align="center">
          <template slot-scope="scope">
            {{ scope.row.positionName || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="当前角色" width="100" align="center" header-align="center">
          <template slot-scope="scope">
            <el-tag :type="getRoleTagType(scope.row.roleType)" size="small">
              {{ getRoleText(scope.row.roleType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center" header-align="center">
          <template slot-scope="scope">
            <el-tag :type="getStatusTagType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="入职日期" width="120" align="center" header-align="center">
          <template slot-scope="scope">
            {{ scope.row.entryDate || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button type="primary" size="mini" @click="showAssignDialog(scope.row)" 
                       style="margin-right: 5px; margin-bottom: 5px;" :disabled="scope.row.status !== 1">
              分配角色
            </el-button>
            
            <!-- 状态操作按钮 -->
            <template v-if="scope.row.status === 1">
              <el-button type="warning" size="mini" @click="handleDisable(scope.row)" 
                         style="margin-bottom: 5px;">
                禁用
              </el-button>
            </template>
            
            <template v-if="scope.row.status === 2">
              <el-button type="success" size="mini" @click="handleRestore(scope.row)" 
                         style="margin-bottom: 5px;">
                恢复
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination v-if="total > 0" 
                     :current-page="query.page + 1" 
                     :page-size="query.size" 
                     :total="total"
                     layout="total, sizes, prev, pager, next, jumper" 
                     :page-sizes="[10, 20, 50, 100]"
                     @size-change="handleSizeChange"
                     @current-change="handleCurrentChange"
                     style="margin-top: 20px; text-align: right;" />
    </el-card>

    <!-- 分配角色对话框 -->
    <el-dialog title="分配角色" :visible.sync="assignDialogVisible" width="400px" top="20vh">
      <el-form :model="assignForm" label-width="80px" ref="assignForm">
        <el-form-item label="员工工号">
          <el-input v-model="assignForm.userCode" disabled />
        </el-form-item>
        <el-form-item label="员工姓名">
          <el-input v-model="assignForm.userName" disabled />
        </el-form-item>
        <el-form-item label="分配角色" prop="roleType" required>
          <el-select v-model="assignForm.roleType" placeholder="请选择角色" style="width: 100%;">
            <el-option label="人事经理" :value="2" />
            <el-option label="薪酬经理" :value="3" />
            <el-option label="人事专员" :value="4" />
            <el-option label="薪酬专员" :value="5" />
            <el-option label="普通员工" :value="6" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="assignDialogVisible = false" size="medium">取消</el-button>
        <el-button type="primary" @click="handleAssign" size="medium" :loading="assignLoading">
          确认分配
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getEmployeeList, 
  assignUserRole, 
  restoreUser, 
  disableUser,
  getAllPositions
} from '@/api/admin'

export default {
  name: 'UserRoleManagement',
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
        userCode: '',
        userName: '',
        status: null,
        roleType: null,
        posId: null
      },
      positionList: [],
      positionLoading: false,
      assignDialogVisible: false,
      assignLoading: false,
      assignForm: {
        userId: null,
        userCode: '',
        userName: '',
        roleType: null
      },
      currentRow: null
    }
  },
  created() {
    this.fetchData()
    this.loadAllPositions()
  },
  methods: {
    async fetchData() {
      try {
        this.loading = true
        
        // 构建查询参数
        const params = {
          page: this.query.page,
          size: this.query.size,
          ...this.searchForm
        }
        
        // 移除空值
        Object.keys(params).forEach(key => {
          if (params[key] === '' || params[key] === null || params[key] === undefined) {
            delete params[key]
          }
        })
        
        console.log('查询参数:', params)
        
        const response = await getEmployeeList(params)
        
        if (response && response.code === 200) {
          const data = response.data
          this.list = data.content || data.list || []
          this.total = data.totalElements || data.total || this.list.length
        } else {
          this.$message.error(response ? response.message : '获取员工列表失败')
          this.list = []
          this.total = 0
        }
      } catch (error) {
        console.error('获取员工列表失败:', error)
        this.$message.error('获取员工列表失败')
        this.list = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    
    // 加载所有职位
    async loadAllPositions() {
      try {
        this.positionLoading = true
        const response = await getAllPositions()
        if (response && response.code === 200) {
          this.positionList = response.data || []
        }
      } catch (error) {
        console.error('加载职位列表失败:', error)
        this.$message.error('加载职位列表失败')
      } finally {
        this.positionLoading = false
      }
    },
    
    handleSearch() {
      this.query.page = 0
      this.fetchData()
    },
    
    handleReset() {
      this.searchForm = {
        userCode: '',
        userName: '',
        status: null,
        roleType: null,
        posId: null
      }
      this.query.page = 0
      this.fetchData()
    },
    
    refresh() {
      this.fetchData()
    },
    
    showAssignDialog(row) {
      this.currentRow = row
      this.assignForm = {
        userId: row.userId,
        userCode: row.userCode,
        userName: row.userName,
        roleType: row.roleType
      }
      this.assignDialogVisible = true
    },
    
    async handleAssign() {
      if (!this.assignForm.roleType) {
        this.$message.warning('请选择要分配的角色')
        return
      }
      
      try {
        this.assignLoading = true
        
        const response = await assignUserRole({
          userId: this.assignForm.userId,
          roleType: this.assignForm.roleType
        })
        
        if (response && response.code === 200) {
          this.$message.success('角色分配成功')
          this.assignDialogVisible = false
          this.fetchData()
        } else {
          this.$message.error(response ? response.message : '角色分配失败')
        }
      } catch (error) {
        console.error('角色分配失败:', error)
        this.$message.error('角色分配失败')
      } finally {
        this.assignLoading = false
      }
    },
    
    async handleRestore(row) {
      try {
        await this.$confirm('确定要恢复该员工吗？恢复后员工可以正常登录系统', '提示', {
          type: 'warning'
        })
        
        const response = await restoreUser(row.userId)
        if (response && response.code === 200) {
          this.$message.success('员工已恢复')
          this.fetchData()
        } else {
          this.$message.error(response ? response.message : '恢复失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('恢复员工失败:', error)
          this.$message.error('恢复员工失败')
        }
      }
    },
    
    async handleDisable(row) {
      try {
        await this.$confirm('确定要禁用该员工吗？禁用后员工将无法登录系统', '提示', {
          type: 'warning'
        })
        
        const response = await disableUser(row.userId)
        if (response && response.code === 200) {
          this.$message.success('员工已禁用')
          this.fetchData()
        } else {
          this.$message.error(response ? response.message : '禁用失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('禁用员工失败:', error)
          this.$message.error('禁用员工失败')
        }
      }
    },
    
    handleSizeChange(size) {
      this.query.size = size
      this.query.page = 0
      this.fetchData()
    },
    
    handleCurrentChange(page) {
      this.query.page = page - 1
      this.fetchData()
    },
    
    getRoleText(roleType) {
      const map = {
        1: '系统管理员',
        2: '人事经理',
        3: '薪酬经理',
        4: '人事专员',
        5: '薪酬专员',
        6: '普通员工'
      }
      return map[roleType] || '未知'
    },
    
    getRoleTagType(roleType) {
      const map = {
        1: 'danger',
        2: 'success',
        3: 'warning',
        4: 'info',
        5: 'info',
        6: ''
      }
      return map[roleType] || ''
    },
    
    getStatusText(status) {
      const map = {
        0: '离职',
        1: '在职',
        2: '禁用'
      }
      return map[status] || '未知'
    },
    
    getStatusTagType(status) {
      const map = {
        0: 'info',
        1: 'success',
        2: 'danger'
      }
      return map[status] || 'info'
    }
  }
}
</script>

<style scoped lang="scss">
.user-role-management {
  padding: 20px;
  
  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: bold;
    color: #303133;
  }
}

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

::v-deep .el-pagination {
  .el-pagination__total {
    margin-right: 20px;
  }
  
  .el-pagination__sizes {
    margin-right: 20px;
  }
}

.org-path {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 250px;
  cursor: pointer;
  
  &:hover {
    color: #409EFF;
  }
}
</style>