<template>
  <div class="salary-payment-total">
    <!-- 加载遮罩 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-content">
        <i class="el-icon-loading"></i>
        <p>正在加载薪酬数据...</p>
      </div>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2>薪酬发放管理</h2>
        <p class="page-subtitle">管理员工薪酬发放流程，包括计算、审核和发放</p>
      </div>
      <div class="header-actions">
        <el-button icon="el-icon-back" @click="handleBack">返回列表</el-button>
        <el-button type="success" icon="el-icon-plus" @click="handleBatchPay">批量发放</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon blue">
            <i class="el-icon-money"></i>
          </div>
          <div class="stat-info">
            <h3>¥{{ totalAmount | currency }}</h3>
            <p>本月待发薪酬总额</p>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon green">
            <i class="el-icon-user"></i>
          </div>
          <div class="stat-info">
            <h3>{{ totalEmployees }}</h3>
            <p>待发薪酬员工总数</p>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon orange">
            <i class="el-icon-time"></i>
          </div>
          <div class="stat-info">
            <h3>{{ pendingCount }}</h3>
            <p>待审核薪酬单</p>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon purple">
            <i class="el-icon-check"></i>
          </div>
          <div class="stat-info">
            <h3>{{ completedCount }}</h3>
            <p>已完成发放</p>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-section" shadow="never">
      <div class="filter-row">
        <div class="filter-item">
          <label>发放月份</label>
          <el-date-picker
            v-model="filters.month"
            type="month"
            placeholder="请选择月份"
            value-format="yyyy-MM"
            format="yyyy年MM月"
            clearable
            @change="handleFilter"
            style="width: 100%"
          />
        </div>
        
        <!-- 修复：只保留一个选择器，使用 el-select -->
        <div class="filter-item">
          <label>机构部门</label>
          <el-select 
            v-model="filters.department" 
            placeholder="请选择三级部门" 
            clearable 
            @change="handleFilter"
            style="width: 100%"
          >
            <el-option
              v-for="org in organizationOptions"
              :key="org.value"
              :label="org.label"
              :value="org.value"
            >
              <span style="float: left">{{ org.label }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                编码: {{ org.orgCode }}
              </span>
            </el-option>
          </el-select>
        </div>
        
        <div class="filter-item">
          <label>审核状态</label>
          <el-select v-model="filters.status" placeholder="请选择状态" clearable @change="handleFilter">
            <el-option label="待审核" value="pending"></el-option>
            <el-option label="已通过" value="approved"></el-option>
            <el-option label="已拒绝" value="rejected"></el-option>
            <el-option label="已发放" value="completed"></el-option>
          </el-select>
        </div>
        
        <div class="filter-item">
          <label>薪酬单号</label>
          <el-input v-model="filters.code" placeholder="请输入薪酬单号" clearable @input="handleFilter"></el-input>
        </div>
        
        <div class="filter-actions">
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
          <el-button plain @click="resetFilters">重置</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-container" shadow="never">
      <div slot="header" class="table-header">
        <span class="table-title">薪酬发放列表</span>
        <el-button type="success" icon="el-icon-plus" size="small" @click="handleAddNew">添加薪酬单</el-button>
      </div>
      
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        empty-text="暂无薪酬发放数据"
        header-cell-class-name="table-header-cell"
        cell-class-name="table-cell"
      >
        <el-table-column prop="id" label="薪酬单号" width="150" align="center"></el-table-column>
        
        <el-table-column label="机构名称" min-width="200">
          <template slot-scope="{ row }">
            <div class="dept-info">
              <div class="dept-name">{{ row.department.name }}</div>
              <div class="dept-path">{{ row.department.path }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="employeeCount" label="总人数" width="80" align="center"></el-table-column>
        
        <el-table-column label="基本薪酬总额" width="130" align="right">
          <template slot-scope="{ row }">
            <span class="amount">¥{{ row.baseSalary | currency }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="绩效奖金" width="120" align="right">
          <template slot-scope="{ row }">
            <span class="amount">¥{{ row.bonus | currency }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="应发总额" width="140" align="right">
          <template slot-scope="{ row }">
            <span class="amount total">¥{{ row.totalAmount | currency }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template slot-scope="{ row }">
            <div class="action-buttons">
              <el-button
                v-if="row.status === 'pending'"
                type="primary"
                size="mini"
                @click="handleRegister(row.id)"
              >登记</el-button>
              
              <el-button
                v-else-if="row.status === 'approved'"
                type="success"
                size="mini"
                @click="handlePay(row.id)"
              >发放</el-button>
              
              <el-button
                v-else-if="row.status === 'rejected'"
                type="warning"
                size="mini"
                @click="handleResubmit(row.id)"
              >重新提交</el-button>
              
              <el-button
                v-else
                type="info"
                size="mini"
                @click="handleView(row.id)"
              >查看</el-button>
              
              <el-button type="text" size="mini" @click="handleDetail(row.id)">详情</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalRecords"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
// 修正导入路径 - 使用独立的 salary payment API
import { 
  getAllThirdLevelOrgs
} from '@/api/salary/payment'

export default {
  name: 'SalaryPaymentTotal',
  data() {
    return {
      loading: false,
      currentPage: 1,
      pageSize: 10,
      totalRecords: 0,
      filters: {
        month: '',
        department: '', // 改为字符串类型
        status: '',
        code: ''
      },
      allData: [],
      filteredData: [],
      tableData: [],
      organizationOptions: [], // 三级机构选项
    };
  },
  
  computed: {
    totalAmount() {
      return this.filteredData.reduce((sum, item) => sum + item.totalAmount, 0);
    },
    totalEmployees() {
      return this.filteredData.reduce((sum, item) => sum + item.employeeCount, 0);
    },
    pendingCount() {
      return this.filteredData.filter(item => item.status === 'pending').length;
    },
    completedCount() {
      return this.filteredData.filter(item => item.status === 'completed').length;
    }
  },
  
  filters: {
    currency(value) {
      if (!value && value !== 0) return '0.00';
      return Number(value).toFixed(2);
    }
  },
  
  created() {
    this.loadData();
    this.loadOrganizations();
  },
  
  methods: {
    async loadData() {
      this.loading = true;
      try {
        this.allData = [];
        this.filteredData = [];
        this.totalRecords = 0;
        this.renderTable();
      } catch (error) {
        this.$message.error('数据加载失败，请稍后重试');
        console.error('加载数据失败:', error);
      } finally {
        this.loading = false;
      }
    },
    
    // 加载三级机构
    async loadOrganizations() {
      try {
        const response = await getAllThirdLevelOrgs();
        console.log('API返回数据:', response); // 调试用
        
        if (response && response.success) {
          // 确保处理正确的数据结构
          const orgData = response.data || [];
          this.organizationOptions = orgData.map(org => ({
            value: org.orgId,
            label: org.orgName,
            orgCode: org.orgCode
          }));
          
          console.log('转换后的选项:', this.organizationOptions); // 调试用
        } else {
          this.$message.warning('获取三级机构数据失败');
          this.organizationOptions = [];
        }
      } catch (error) {
        console.error('加载三级机构数据失败:', error);
        this.$message.error('加载三级机构数据失败');
        this.organizationOptions = [];
      }
    },
    
    renderTable() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      this.tableData = this.filteredData.slice(start, end);
    },
    
    handleFilter() {
      const { month, department, status, code } = this.filters;
      
      this.filteredData = this.allData.filter(item => {
        return (!month || item.id.includes(month.replace('-', ''))) &&
               (!department || item.department.id === department) &&
               (!status || item.status === status) &&
               (!code || item.id.toLowerCase().includes(code.toLowerCase()));
      });
      
      this.currentPage = 1;
      this.totalRecords = this.filteredData.length;
      this.renderTable();
    },
    
    handleSearch() {
      this.handleFilter();
    },
    
    resetFilters() {
      this.filters = {
        month: '',
        department: '',
        status: '',
        code: ''
      };
      this.filteredData = [...this.allData];
      this.currentPage = 1;
      this.totalRecords = this.filteredData.length;
      this.renderTable();
    },
    
    handleSizeChange(size) {
      this.pageSize = size;
      this.renderTable();
    },
    
    handleCurrentChange(page) {
      this.currentPage = page;
      this.renderTable();
    },
    
    getStatusTagType(status) {
      const types = {
        'pending': 'warning',
        'approved': 'success',
        'rejected': 'danger',
        'completed': 'info'
      };
      return types[status] || '';
    },
    
    getStatusText(status) {
      const texts = {
        'pending': '待审核',
        'approved': '已通过',
        'rejected': '已拒绝',
        'completed': '已发放'
      };
      return texts[status] || '未知';
    },
    
    // 操作按钮方法
    handleRegister(id) {
      this.$router.push(`/salary/payment/register/${id}`);
    },
    
    handlePay(id) {
      this.$confirm(`确定要发放薪酬单 ${id} 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$message.success(`发放成功: ${id}`);
      }).catch(() => {});
    },
    
    handleResubmit(id) {
      this.$router.push(`/salary/payment/resubmit/${id}`);
    },
    
    handleView(id) {
      this.$router.push(`/salary/payment/detail/${id}`);
    },
    
    handleDetail(id) {
      this.$router.push(`/salary/payment/detail/${id}`);
    },
    
    handleBatchPay() {
      const selected = this.$refs.salaryTable?.selection || [];
      if (selected.length === 0) {
        this.$message.warning('请先选择要发放的薪酬单');
        return;
      }
      this.$message.info(`批量发放: ${selected.map(item => item.id).join(', ')}`);
    },
    
    handleAddNew() {
      this.$router.push('/salary/payment/create');
    },
    
    handleBack() {
      if (this.hasUnsavedChanges()) {
        this.$confirm('您有未保存的筛选条件，确定要离开吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
                this.$router.push('/salary/manage')
        }).catch(() => {});
      } else {
            this.$router.push('/salary/manage')
      }
    },
    
    hasUnsavedChanges() {
      return Object.values(this.filters).some(value => value && value.length > 0);
    }
  }
};
</script>

<style lang="scss" scoped>
.salary-payment-total {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
  
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
    
    .loading-content {
      text-align: center;
      
      i {
        font-size: 40px;
        color: #409EFF;
        margin-bottom: 20px;
      }
      
      p {
        font-size: 16px;
        color: #606266;
      }
    }
  }
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 20px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    h2 {
      margin: 0;
      font-size: 22px;
      font-weight: 600;
      color: #303133;
    }
    
    .page-subtitle {
      color: #909399;
      font-size: 14px;
      margin-top: 8px;
      margin-bottom: 0;
    }
    
    .header-actions {
      display: flex;
      gap: 12px;
    }
  }
  
  .stats-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
    margin-bottom: 24px;
    
    .stat-card {
      border-radius: 8px;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
      }
      
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 10px;
        
        .stat-icon {
          width: 48px;
          height: 48px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          
          &.blue {
            background: #e6f4ff;
            color: #1890ff;
          }
          
          &.green {
            background: #f6ffed;
            color: #52c41a;
          }
          
          &.orange {
            background: #fff7e6;
            color: #fa8c16;
          }
          
          &.purple {
            background: #f9f0ff;
            color: #722ed1;
          }
        }
        
        .stat-info {
          h3 {
            font-size: 28px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 4px;
          }
          
          p {
            font-size: 14px;
            color: #909399;
            margin: 0;
          }
        }
      }
    }
  }
  
  .filter-section {
    margin-bottom: 24px;
    border-radius: 8px;
    
    .filter-row {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;
      align-items: flex-end;
      
      .filter-item {
        display: flex;
        flex-direction: column;
        gap: 8px;
        flex: 1;
        min-width: 150px;
        
        label {
          font-size: 14px;
          color: #606266;
          font-weight: 500;
        }
      }
      
      .filter-actions {
        display: flex;
        gap: 12px;
        margin-left: auto;
      }
    }
  }
  
  .table-container {
    border-radius: 8px;
    
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 20px;
      border-bottom: 1px solid #EBEEF5;
      
      .table-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }
    
    .dept-info {
      .dept-name {
        font-weight: 600;
        color: #303133;
        margin-bottom: 4px;
      }
      
      .dept-path {
        font-size: 12px;
        color: #909399;
        line-height: 1.4;
      }
    }
    
    .amount {
      font-weight: 600;
      color: #303133;
      
      &.total {
        color: #409EFF;
        font-size: 16px;
      }
    }
    
    .action-buttons {
      display: flex;
      gap: 8px;
      justify-content: center;
    }
  }
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    padding: 16px 20px;
    background: #fff;
    border-top: 1px solid #EBEEF5;
  }
}

// 表格样式深度覆盖
::v-deep .el-table {
  .table-header-cell {
    background-color: #fafafa !important;
    color: #606266;
    font-weight: 600;
    font-size: 14px;
    padding: 12px 0;
  }
  
  .table-cell {
    padding: 16px 0;
  }
  
  .el-tag {
    font-weight: 500;
  }
}

// 响应式设计
@media screen and (max-width: 768px) {
  ::v-deep .salary-payment-total {
    padding: 10px;
    
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }
    
    .filter-row {
      flex-direction: column;
      
      .filter-actions {
        margin-left: 0;
        width: 100%;
        
        .el-button {
          flex: 1;
        }
      }
    }
    
    .stats-cards {
      grid-template-columns: 1fr;
    }
  }
}
</style>