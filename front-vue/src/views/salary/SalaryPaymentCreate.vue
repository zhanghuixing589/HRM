<template>
  <div class="salary-payment-form">
    <!-- 加载遮罩 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-content">
        <i class="el-icon-loading"></i>
        <p>正在加载薪酬详情...</p>
      </div>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2>薪酬发放登记</h2>
        <p class="page-subtitle">薪酬单号：{{ paymentCode }} | 部门：{{ departmentName }}</p>
      </div>
      <div class="header-actions">
        <el-button icon="el-icon-back" @click="handleBack">返回列表</el-button>
        <el-button type="success" icon="el-icon-check" @click="handleSubmit">提交薪酬发放登记</el-button>
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
            <h3>¥{{ totalPayable | currency }}</h3>
            <p>应发总额</p>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon green">
            <i class="el-icon-user"></i>
          </div>
          <div class="stat-info">
            <h3>{{ employeeCount }}</h3>
            <p>发放人数</p>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon orange">
            <i class="el-icon-tickets"></i>
          </div>
          <div class="stat-info">
            <h3>¥{{ totalBonus | currency }}</h3>
            <p>绩效奖金总额</p>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card" shadow="hover">
        <div class="stat-content">
          <div class="stat-icon purple">
            <i class="el-icon-bank-card"></i>
          </div>
          <div class="stat-info">
            <h3>¥{{ totalActual | currency }}</h3>
            <p>实发总额</p>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 薪酬明细表格 -->
    <el-card class="table-container" shadow="never">
      <div slot="header" class="table-header">
        <span class="table-title">薪酬发放明细</span>
        <div class="table-actions">
          <el-button type="primary" icon="el-icon-download" size="small" @click="handleExport">导出Excel</el-button>
        </div>
      </div>
      
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        header-cell-class-name="table-header-cell"
        cell-class-name="table-cell"
        show-summary
        :summary-method="getSummaries"
      >
        <el-table-column prop="employeeCode" label="员工编号" width="100" fixed align="center"></el-table-column>
        
        <el-table-column prop="employeeName" label="姓名" width="100" fixed align="center"></el-table-column>
        
        <el-table-column prop="position" label="职位" width="120" align="center"></el-table-column>
        
        <el-table-column label="应发项目" align="center">
          <el-table-column prop="baseSalary" label="基本工资" width="110" align="right">
            <template slot-scope="{ row }">
              ¥{{ row.baseSalary | currency }}
            </template>
          </el-table-column>
          
          <el-table-column prop="performanceBonus" label="绩效奖金" width="110" align="right">
            <template slot-scope="{ row }">
              ¥{{ row.performanceBonus | currency }}
            </template>
          </el-table-column>
          
          <el-table-column prop="transportAllowance" label="交通补贴" width="100" align="right">
            <template slot-scope="{ row }">
              ¥{{ row.transportAllowance | currency }}
            </template>
          </el-table-column>
          
          <el-table-column prop="mealAllowance" label="餐费补贴" width="100" align="right">
            <template slot-scope="{ row }">
              ¥{{ row.mealAllowance | currency }}
            </template>
          </el-table-column>
          
          <el-table-column prop="rewardAmount" label="奖励金额" width="100" align="right">
            <template slot-scope="{ row }">
              ¥{{ row.rewardAmount | currency }}
            </template>
          </el-table-column>
        </el-table-column>
        
        <el-table-column label="代扣项目" align="center">
          <el-table-column prop="pensionInsurance" label="养老保险" width="100" align="right">
            <template slot-scope="{ row }">
              ¥{{ row.pensionInsurance | currency }}
            </template>
          </el-table-column>
          
          <el-table-column prop="medicalInsurance" label="医疗保险" width="100" align="right">
            <template slot-scope="{ row }">
              ¥{{ row.medicalInsurance | currency }}
            </template>
          </el-table-column>
          
          <el-table-column prop="unemploymentInsurance" label="失业保险" width="100" align="right">
            <template slot-scope="{ row }">
              ¥{{ row.unemploymentInsurance | currency }}
            </template>
          </el-table-column>
          
          <el-table-column prop="housingFund" label="住房公积金" width="110" align="right">
            <template slot-scope="{ row }">
              ¥{{ row.housingFund | currency }}
            </template>
          </el-table-column>
          
          <el-table-column prop="deductionAmount" label="应扣金额" width="100" align="right">
            <template slot-scope="{ row }">
              ¥{{ row.deductionAmount | currency }}
            </template>
          </el-table-column>
        </el-table-column>
        
        <el-table-column label="实发金额" width="120" fixed="right" align="center">
          <template slot-scope="{ row }">
            <span class="actual-amount">¥{{ row.actualAmount | currency }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" size="mini" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'SalaryPaymentForm',
  data() {
    return {
      loading: false,
      paymentCode: 'PA202307001', // 薪酬单号
      departmentName: '技术中心/产品研发部/前端组', // 部门名称
      tableData: [],
    };
  },
  
  computed: {
    employeeCount() {
      return this.tableData.length;
    },
    totalPayable() {
      return this.tableData.reduce((sum, item) => {
        return sum + item.baseSalary + item.performanceBonus + item.transportAllowance + 
                   item.mealAllowance + item.rewardAmount;
      }, 0);
    },
    totalBonus() {
      return this.tableData.reduce((sum, item) => sum + item.performanceBonus, 0);
    },
    totalActual() {
      return this.tableData.reduce((sum, item) => sum + item.actualAmount, 0);
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
  },
  
  methods: {
    async loadData() {
      this.loading = true;
      try {
        // 模拟API调用，实际项目中从后端获取
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // 模拟数据
        this.tableData = [
          {
            employeeCode: 'EMP001',
            employeeName: '张明',
            position: '前端工程师',
            baseSalary: 12000,
            performanceBonus: 2000,
            transportAllowance: 0,
            mealAllowance: 300,
            rewardAmount: 0,
            pensionInsurance: 960,
            medicalInsurance: 243,
            unemploymentInsurance: 60,
            housingFund: 960,
            deductionAmount: 0,
            actualAmount: 12077
          },
          {
            employeeCode: 'EMP002',
            employeeName: '李华',
            position: '前端工程师',
            baseSalary: 10000,
            performanceBonus: 1500,
            transportAllowance: 500,
            mealAllowance: 800,
            rewardAmount: 0,
            pensionInsurance: 800,
            medicalInsurance: 203,
            unemploymentInsurance: 50,
            housingFund: 800,
            deductionAmount: 0,
            actualAmount: 10147
          },
          {
            employeeCode: 'EMP003',
            employeeName: '王芳',
            position: '前端工程师',
            baseSalary: 11000,
            performanceBonus: 1800,
            transportAllowance: 300,
            mealAllowance: 880,
            rewardAmount: 0,
            pensionInsurance: 880,
            medicalInsurance: 223,
            unemploymentInsurance: 55,
            housingFund: 880,
            deductionAmount: 0,
            actualAmount: 11062
          },
          {
            employeeCode: 'EMP004',
            employeeName: '刘伟',
            position: '前端工程师',
            baseSalary: 13000,
            performanceBonus: 2500,
            transportAllowance: 500,
            mealAllowance: 300,
            rewardAmount: 0,
            pensionInsurance: 1040,
            medicalInsurance: 263,
            unemploymentInsurance: 65,
            housingFund: 1040,
            deductionAmount: 0,
            actualAmount: 13892
          },
          {
            employeeCode: 'EMP005',
            employeeName: '陈静',
            position: '前端工程师',
            baseSalary: 12000,
            performanceBonus: 2200,
            transportAllowance: 500,
            mealAllowance: 300,
            rewardAmount: 0,
            pensionInsurance: 960,
            medicalInsurance: 243,
            unemploymentInsurance: 60,
            housingFund: 960,
            deductionAmount: 0,
            actualAmount: 12777
          }
        ];
      } catch (error) {
        this.$message.error('数据加载失败，请稍后重试');
        console.error('加载数据失败:', error);
      } finally {
        this.loading = false;
      }
    },
    
    // 自定义合计行
    getSummaries(param) {
      const { columns, data } = param;
      const sums = [];
      
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '总计';
          return;
        }
        
        const values = data.map(item => Number(item[column.property]));
        
        if (!values.every(value => isNaN(value))) {
          sums[index] = values.reduce((prev, curr) => {
            const value = Number(curr);
            if (!isNaN(value)) {
              return prev + curr;
            } else {
              return prev;
            }
          }, 0);
          
          // 格式化货币
          if (column.property.includes('Salary') || 
              column.property.includes('Bonus') || 
              column.property.includes('Allowance') ||
              column.property.includes('Insurance') ||
              column.property.includes('Fund') ||
              column.property.includes('Amount')) {
            sums[index] = `¥${sums[index].toFixed(2)}`;
          }
        } else {
          sums[index] = '';
        }
      });
      
      return sums;
    },
    
    handleEdit(row) {
      this.$message.info(`编辑员工: ${row.employeeName}`);
      // 实际项目中跳转到编辑页面
    },
    
    handleSubmit() {
      this.$confirm('确认提交薪酬发放登记？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$message.success('薪酬发放登记提交成功！');
        // 实际项目中调用API提交
      }).catch(() => {});
    },
    
    handleBack() {
      this.$router.push('/salary/payment');
    },
    
    handleExport() {
      this.$message.info('正在导出Excel...');
      // 实际项目中调用导出API
    }
  }
};
</script>

<style lang="scss" scoped>
.salary-payment-form {
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
      
      .table-actions {
        display: flex;
        gap: 12px;
      }
    }
    
    .actual-amount {
      font-weight: 600;
      color: #409EFF;
      font-size: 14px;
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
  
  // 合计行样式
  .el-table__footer-wrapper {
    .el-table__footer {
      .cell {
        font-weight: 600;
        color: #303133;
      }
    }
  }
}

// 响应式设计
@media screen and (max-width: 768px) {
  ::v-deep .salary-payment-form {
    padding: 10px;
    
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }
    
    .stats-cards {
      grid-template-columns: 1fr;
    }
  }
}
</style>