<template>
  <div class="salary-payment-form">
    <!-- 添加薪酬单按钮 -->
    <div class="page-header">
      <h2>薪酬发放管理</h2>
      <el-button type="primary" icon="el-icon-plus" @click="showOrgDialog">添加薪酬单</el-button>
    </div>

    <!-- 机构选择弹窗 -->
    <el-dialog
      title="选择机构"
      :visible.sync="orgDialogVisible"
      width="800px"
      :close-on-click-modal="false"
      @closed="handleDialogClosed"
    >
      <div class="org-selector">
        <!-- 机构树 -->
        <div class="org-tree-section">
          <h3>选择机构</h3>
          <el-input
            v-model="orgFilterText"
            placeholder="输入机构名称搜索"
            prefix-icon="el-icon-search"
            clearable
            class="org-search"
          />
          <div class="tree-container">
            <el-tree
              ref="orgTree"
              :data="orgTreeData"
              :props="orgTreeProps"
              :filter-node-method="filterOrgNode"
              node-key="orgId"
              :expand-on-click-node="false"
              highlight-current
              :default-expanded-keys="expandedOrgKeys"
              @node-click="handleOrgClick"
            >
              <span class="custom-tree-node" slot-scope="{ node, data }">
                <span class="org-node">
                  <i :class="getOrgIcon(data.orgType)" class="org-icon"></i>
                  {{ node.label }}
                  <span class="org-type-tag" v-if="data.orgType === 3">部门</span>
                </span>
              </span>
            </el-tree>
          </div>
        </div>

        <!-- 已选机构信息 -->
        <div class="selected-org-section" v-if="selectedOrg">
          <h3>已选机构信息</h3>
          <div class="selected-org-info">
            <div class="org-detail">
              <div class="detail-item">
                <span class="detail-label">机构编码：</span>
                <span class="detail-value">{{ selectedOrg.orgCode || '--' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">机构类型：</span>
                <span class="detail-value">{{ getOrgTypeText(selectedOrg.orgType) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">机构层级：</span>
                <span class="detail-value">{{ selectedOrg.orgLevel }}级</span>
              </div>
              <div class="detail-item" v-if="selectedOrg.contactPerson">
                <span class="detail-label">联系人：</span>
                <span class="detail-value">{{ selectedOrg.contactPerson }}</span>
              </div>
              <div class="detail-item" v-if="selectedOrg.contactPhone">
                <span class="detail-label">联系电话：</span>
                <span class="detail-value">{{ selectedOrg.contactPhone }}</span>
              </div>
              <div class="detail-item" v-if="selectedOrg.orgStatus !== undefined">
                <span class="detail-label">状态：</span>
                <el-tag :type="selectedOrg.orgStatus === 1 ? 'success' : 'danger'" size="small">
                  {{ selectedOrg.orgStatus === 1 ? '启用' : '禁用' }}
                </el-tag>
              </div>
            </div>

            <!-- 机构路径 -->
            <div class="org-path">
              <div class="path-label">机构路径：</div>
              <div class="path-value">
                <el-tag v-if="selectedOrg.firstOrgName" type="info" size="small">
                  {{ selectedOrg.firstOrgName }}
                </el-tag>
                <i class="el-icon-right path-arrow" v-if="selectedOrg.firstOrgName && (selectedOrg.secondOrgName || selectedOrg.thirdOrgName)"></i>
                <el-tag v-if="selectedOrg.secondOrgName" type="info" size="small">
                  {{ selectedOrg.secondOrgName }}
                </el-tag>
                <i class="el-icon-right path-arrow" v-if="selectedOrg.secondOrgName && selectedOrg.thirdOrgName"></i>
                <el-tag v-if="selectedOrg.thirdOrgName" type="primary" size="small">
                  {{ selectedOrg.thirdOrgName }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="orgDialogVisible = false">取 消</el-button>
        <el-button 
          type="primary" 
          :disabled="!selectedOrg || !selectedOrg.orgId" 
          @click="handleConfirm"
          :loading="confirmLoading"
        >
          确 定
        </el-button>
      </span>
    </el-dialog>

    <!-- 薪酬单详情页面 -->
    <div v-if="salaryDetail">
      <!-- 页面头部 -->
      <div class="page-header">
        <div>
          <h2>薪酬发放登记</h2>
          <div class="header-info">
            <el-tag type="primary">新建</el-tag>
            <span class="salary-code">薪酬单号：{{ salaryDetail.salaryCode }}</span>
            <span class="org-info">机构：{{ getFullOrgPath(salaryDetail) }}</span>
            <span class="create-time">创建时间：{{ formatTime(salaryDetail.createTime) }}</span>
          </div>
        </div>
        <div class="header-actions">
          <el-button icon="el-icon-back" @click="handleCancel">返回列表</el-button>
          <el-button type="success" icon="el-icon-check" @click="handleSaveDraft">保存草稿</el-button>
          <el-button type="primary" icon="el-icon-document-checked" @click="handleSubmitForm">提交发放</el-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon blue">
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
            <div class="stat-icon green">
              <i class="el-icon-office-building"></i>
            </div>
            <div class="stat-info">
              <h3>{{ getOrgLevel(salaryDetail) }}</h3>
              <p>机构层级</p>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon orange">
              <i class="el-icon-data-analysis"></i>
            </div>
            <div class="stat-info">
              <h3>{{ getPositionTypes() }}</h3>
              <p>职位种类</p>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 薪酬明细表格 -->
      <el-card class="table-container" shadow="never">
        <div slot="header" class="table-header">
          <span class="table-title">薪酬发放明细</span>
          <span class="table-subtitle">机构：{{ getFullOrgPath(salaryDetail) }} | 共 {{ employeeCount }} 人</span>
          <div class="table-actions">
            <el-button type="primary" icon="el-icon-download" size="small" @click="handleExportTemplate">导出模板</el-button>
            <el-button type="success" icon="el-icon-upload2" size="small" @click="handleImport">导入数据</el-button>
          </div>
        </div>
        
        <el-table
          :data="salaryItems"
          v-loading="loading"
          border
          stripe
          style="width: 100%"
          header-cell-class-name="table-header-cell"
          cell-class-name="table-cell"
          :default-sort="{ prop: 'employeeCode', order: 'ascending' }"
        >
          <el-table-column 
            type="index" 
            label="序号" 
            width="60" 
            align="center"
          ></el-table-column>
          
          <el-table-column 
            prop="employeeCode" 
            label="员工编号" 
            width="120" 
            fixed 
            align="center"
            sortable
          ></el-table-column>
          
          <el-table-column 
            prop="employeeName" 
            label="姓名" 
            width="100" 
            fixed 
            align="center"
          ></el-table-column>
          
          <el-table-column 
            prop="positionName" 
            label="职位" 
            width="120" 
            align="center"
          >
            <template slot-scope="{ row }">
              <el-tag v-if="row.positionName" size="small">
                {{ row.positionName }}
              </el-tag>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
          
          <el-table-column 
            prop="deptName" 
            label="所属部门" 
            width="150" 
            align="center"
          >
            <template slot-scope="{ row }">
              <span v-if="row.deptName">{{ row.deptName }}</span>
              <span v-else class="no-data">--</span>
            </template>
          </el-table-column>
          
          <!-- 应发项目 -->
          <el-table-column label="应发项目" align="center" class-name="income-header">
            <el-table-column 
              prop="baseSalary" 
              label="基本工资" 
              width="120" 
              align="right"
            >
              <template slot-scope="{ row }">
                <el-input-number
                  v-model="row.baseSalary"
                  :controls="false"
                  :precision="2"
                  :min="0"
                  :max="999999"
                  size="small"
                  class="salary-input"
                />
              </template>
            </el-table-column>
            
            <el-table-column 
              prop="performanceBonus" 
              label="绩效奖金" 
              width="120" 
              align="right"
            >
              <template slot-scope="{ row }">
                <el-input-number
                  v-model="row.performanceBonus"
                  :controls="false"
                  :precision="2"
                  :min="0"
                  :max="999999"
                  size="small"
                  class="salary-input"
                />
              </template>
            </el-table-column>
            
            <el-table-column 
              prop="transportAllowance" 
              label="交通补贴" 
              width="120" 
              align="right"
            >
              <template slot-scope="{ row }">
                <el-input-number
                  v-model="row.transportAllowance"
                  :controls="false"
                  :precision="2"
                  :min="0"
                  :max="999999"
                  size="small"
                  class="salary-input"
                />
              </template>
            </el-table-column>
            
            <el-table-column 
              prop="mealAllowance" 
              label="餐费补贴" 
              width="120" 
              align="right"
            >
              <template slot-scope="{ row }">
                <el-input-number
                  v-model="row.mealAllowance"
                  :controls="false"
                  :precision="2"
                  :min="0"
                  :max="999999"
                  size="small"
                  class="salary-input"
                />
              </template>
            </el-table-column>
            
            <el-table-column 
              prop="rewardAmount" 
              label="奖励金额" 
              width="120" 
              align="right"
            >
              <template slot-scope="{ row }">
                <el-input-number
                  v-model="row.rewardAmount"
                  :controls="false"
                  :precision="2"
                  :min="0"
                  :max="999999"
                  size="small"
                  class="salary-input"
                />
              </template>
            </el-table-column>
          </el-table-column>
          
          <!-- 代扣项目 -->
          <el-table-column label="代扣项目" align="center" class-name="deduction-header">
            <el-table-column 
              prop="pensionInsurance" 
              label="养老保险" 
              width="120" 
              align="right"
            >
              <template slot-scope="{ row }">
                <el-input-number
                  v-model="row.pensionInsurance"
                  :controls="false"
                  :precision="2"
                  :min="0"
                  :max="999999"
                  size="small"
                  class="salary-input"
                />
              </template>
            </el-table-column>
            
            <el-table-column 
              prop="medicalInsurance" 
              label="医疗保险" 
              width="120" 
              align="right"
            >
              <template slot-scope="{ row }">
                <el-input-number
                  v-model="row.medicalInsurance"
                  :controls="false"
                  :precision="2"
                  :min="0"
                  :max="999999"
                  size="small"
                  class="salary-input"
                />
              </template>
            </el-table-column>
            
            <el-table-column 
              prop="unemploymentInsurance" 
              label="失业保险" 
              width="120" 
              align="right"
            >
              <template slot-scope="{ row }">
                <el-input-number
                  v-model="row.unemploymentInsurance"
                  :controls="false"
                  :precision="2"
                  :min="0"
                  :max="999999"
                  size="small"
                  class="salary-input"
                />
              </template>
            </el-table-column>
            
            <el-table-column 
              prop="housingFund" 
              label="住房公积金" 
              width="120" 
              align="right"
            >
              <template slot-scope="{ row }">
                <el-input-number
                  v-model="row.housingFund"
                  :controls="false"
                  :precision="2"
                  :min="0"
                  :max="999999"
                  size="small"
                  class="salary-input"
                />
              </template>
            </el-table-column>
            
            <el-table-column 
              prop="deductionAmount" 
              label="应扣金额" 
              width="120" 
              align="right"
            >
              <template slot-scope="{ row }">
                <el-input-number
                  v-model="row.deductionAmount"
                  :controls="false"
                  :precision="2"
                  :min="0"
                  :max="999999"
                  size="small"
                  class="salary-input"
                />
              </template>
            </el-table-column>
          </el-table-column>
          
          <el-table-column 
            prop="actualAmount" 
            label="实发金额" 
            width="120" 
            fixed="right" 
            align="right"
            class-name="actual-header"
          >
            <template slot-scope="{ row }">
              <span class="actual-amount">¥{{ calculateActualAmount(row) | currency }}</span>
            </template>
          </el-table-column>
          
          <el-table-column 
            label="操作" 
            width="80" 
            fixed="right" 
            align="center"
          >
            <template slot-scope="{ row }">
              <el-button 
                type="text" 
                size="mini" 
                icon="el-icon-delete" 
                @click="handleRemoveEmployee(row)"
              ></el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script>
import { getOrgTree, getOrgById } from '@/api/org'
import { getPositionNameById ,getEmployeesByOrgId} from '@/api/user'
import{saveSalaryDraft,submitSalary} from '@/api/salary/standards'

export default {
  name: 'SalaryPaymentForm',
  data() {
    return {
      // 机构选择相关
      orgDialogVisible: false,
      orgFilterText: '',
      orgTreeData: [],
      orgTreeProps: {
        children: 'children',
        label: 'orgName'
      },
      expandedOrgKeys: [],
      selectedOrg: null,
      confirmLoading: false,
      
      // 薪酬单相关
      salaryDetail: null,
      salaryItems: [],
      loading: false,
      
      // 缓存数据
      allEmployees: [], // 当前机构下所有员工
      positionMap: {} // 职位ID到名称的映射
    }
  },
  
  computed: {
    employeeCount() {
      return this.salaryItems.length
    }
  },
  
  watch: {
    orgFilterText(val) {
      this.$refs.orgTree.filter(val)
    }
  },
  
  created() {
    this.loadOrgTree()
  },
  
  methods: {
    // 显示机构选择弹窗
    showOrgDialog() {
      this.orgDialogVisible = true
      this.selectedOrg = null
    },
    
    // 加载机构树
    async loadOrgTree() {
      try {
        this.loading = true
        const response = await getOrgTree()
        
        if (response.code === 200 && response.success) {
          this.orgTreeData = response.data
          // 展开一级机构
          if (this.orgTreeData.length > 0) {
            this.expandedOrgKeys = this.orgTreeData.map(org => org.orgId)
          }
        } else {
          throw new Error(response.message || '加载机构树失败')
        }
      } catch (error) {
        console.error('加载机构树失败:', error)
        this.$message.error('加载机构树失败')
      } finally {
        this.loading = false
      }
    },
    
    // 过滤机构节点
    filterOrgNode(value, data) {
      if (!value) return true
      return data.orgName.toLowerCase().indexOf(value.toLowerCase()) !== -1
    },
    
    // 获取机构图标
    getOrgIcon(orgType) {
      switch (orgType) {
        case 1: return 'el-icon-office-building' // 一级机构
        case 2: return 'el-icon-s-office' // 二级机构
        case 3: return 'el-icon-house' // 部门
        default: return 'el-icon-folder'
      }
    },
    
    // 获取机构类型文本
    getOrgTypeText(orgType) {
      switch (orgType) {
        case 1: return '一级机构'
        case 2: return '二级机构'
        case 3: return '部门'
        default: return '未知'
      }
    },
    
    // 处理机构点击
    async handleOrgClick(data) {
      try {
        // 获取完整的机构信息（包括一二三级）
        const response = await getOrgById (data.orgId)
        
        if (response.code === 200 && response.success) {
          this.selectedOrg = response.data
          console.log('选择的机构信息:', this.selectedOrg)
        } else {
          throw new Error(response.message || '获取机构信息失败')
        }
      } catch (error) {
        console.error('获取机构信息失败:', error)
        this.$message.error('获取机构信息失败')
      }
    },
    
    // 处理弹窗关闭
    handleDialogClosed() {
      this.orgFilterText = ''
      this.selectedOrg = null
    },
    
    // 确认选择机构
    async handleConfirm() {
      if (!this.selectedOrg || !this.selectedOrg.orgId) {
        this.$message.warning('请选择机构')
        return
      }
      
      try {
        this.confirmLoading = true
        
        // 1. 生成薪酬单号
        const salaryCode = this.generateSalaryCode()
        
        // 2. 获取该机构下的所有员工
        const employees = await this.getOrgEmployees(this.selectedOrg.orgId)
        
        // 3. 获取职位信息
        await this.loadPositionNames(employees)
        
        // 4. 创建薪酬单详情
        this.salaryDetail = {
          salaryCode,
          orgId: this.selectedOrg.orgId,
          orgCode: this.selectedOrg.orgCode,
          orgName: this.selectedOrg.orgName,
          firstOrgId: this.selectedOrg.firstOrgId,
          firstOrgName: this.selectedOrg.firstOrgName,
          secondOrgId: this.selectedOrg.secondOrgId,
          secondOrgName: this.selectedOrg.secondOrgName,
          thirdOrgId: this.selectedOrg.thirdOrgId,
          thirdOrgName: this.selectedOrg.thirdOrgName,
          createTime: new Date(),
          status: 0, // 0: 草稿, 1: 已提交
          employeeCount: employees.length
        }
        
        // 5. 创建薪酬项列表
        this.salaryItems = employees.map(employee => ({
          id: `temp_${Date.now()}_${employee.employeeId}`,
          employeeId: employee.employeeId,
          employeeCode: employee.employeeCode,
          employeeName: employee.employeeName,
          positionId: employee.positionId,
          positionName: this.positionMap[employee.positionId] || employee.positionName,
          deptName: employee.deptName || this.selectedOrg.orgName,
          // 应发项目
          baseSalary: 0,
          performanceBonus: 0,
          transportAllowance: 0,
          mealAllowance: 0,
          rewardAmount: 0,
          // 代扣项目
          pensionInsurance: 0,
          medicalInsurance: 0,
          unemploymentInsurance: 0,
          housingFund: 0,
          deductionAmount: 0,
          // 其他
          actualAmount: 0,
          remarks: ''
        }))
        
        console.log('创建的薪酬单:', this.salaryDetail)
        console.log('薪酬项列表:', this.salaryItems)
        
        // 6. 关闭弹窗
        this.orgDialogVisible = false
        this.$message.success('薪酬单创建成功')
        
      } catch (error) {
        console.error('创建薪酬单失败:', error)
        this.$message.error('创建薪酬单失败: ' + error.message)
      } finally {
        this.confirmLoading = false
      }
    },
    
    // 生成薪酬单号
    generateSalaryCode() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0')
      return `SALARY_${year}${month}${day}_${random}`
    },
    
    // 获取机构下的所有员工
    async getOrgEmployees(orgId) {
      try {
        const response = await getEmployeesByOrgId(orgId)
        
        if (response.code === 200 && response.success) {
          this.allEmployees = response.data
          console.log('机构员工列表:', this.allEmployees)
          return this.allEmployees
        } else {
          throw new Error(response.message || '获取员工列表失败')
        }
      } catch (error) {
        console.error('获取员工列表失败:', error)
        this.$message.error('获取员工列表失败')
        return []
      }
    },
    
    // 加载职位名称
    async loadPositionNames(employees) {
      const positionIds = [...new Set(employees.map(e => e.positionId).filter(Boolean))]
      
      for (const positionId of positionIds) {
        if (!this.positionMap[positionId]) {
          try {
            const response = await getPositionNameById(positionId)
            if (response.code === 200 && response.success) {
              this.positionMap[positionId] = response.data?.posName || '未知职位'
            }
          } catch (error) {
            console.warn(`获取职位 ${positionId} 失败:`, error)
            this.positionMap[positionId] = '未知职位'
          }
        }
      }
    },
    
    // 获取完整的机构路径
    getFullOrgPath(salaryDetail) {
      if (!salaryDetail) return ''
      const orgs = [
        salaryDetail.firstOrgName,
        salaryDetail.secondOrgName,
        salaryDetail.thirdOrgName
      ].filter(Boolean)
      return orgs.join('/') || salaryDetail.orgName
    },
    
    // 格式化时间
    formatTime(time) {
      if (!time) return '--'
      const date = new Date(time)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
    },
    
    // 获取机构层级
    getOrgLevel(salaryDetail) {
      if (!salaryDetail) return 0
      let level = 1
      if (salaryDetail.secondOrgName) level = 2
      if (salaryDetail.thirdOrgName) level = 3
      return level
    },
    
    // 获取职位种类数量
    getPositionTypes() {
      const positions = new Set(this.salaryItems.map(item => item.positionName).filter(Boolean))
      return positions.size
    },
    
    // 计算实发金额
    calculateActualAmount(row) {
      const income = (row.baseSalary || 0) + 
                     (row.performanceBonus || 0) + 
                     (row.transportAllowance || 0) + 
                     (row.mealAllowance || 0) + 
                     (row.rewardAmount || 0)
      
      const deduction = (row.pensionInsurance || 0) + 
                        (row.medicalInsurance || 0) + 
                        (row.unemploymentInsurance || 0) + 
                        (row.housingFund || 0) + 
                        (row.deductionAmount || 0)
      
      const actual = income - deduction
      row.actualAmount = actual > 0 ? actual : 0
      return row.actualAmount
    },
    
    // 删除员工
    handleRemoveEmployee(row) {
      this.$confirm(`确定要移除员工 ${row.employeeName} 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const index = this.salaryItems.findIndex(item => item.id === row.id)
        if (index !== -1) {
          this.salaryItems.splice(index, 1)
          this.$message.success('员工移除成功')
        }
      }).catch(() => {})
    },
    
    // 保存草稿
    async handleSaveDraft() {
      try {
        this.loading = true
        
        // 更新实发金额
        this.salaryItems.forEach(row => this.calculateActualAmount(row))
        
        const salaryData = {
          ...this.salaryDetail,
          status: 0, // 草稿状态
          salaryItems: this.salaryItems,
          updateTime: new Date(),
          totalAmount: this.salaryItems.reduce((sum, item) => sum + item.actualAmount, 0),
          totalEmployees: this.salaryItems.length
        }
        
        // 调用API保存
        const response = await saveSalaryDraft(salaryData)
        
        if (response.code === 200 && response.success) {
          this.$message.success('薪酬单已保存为草稿')
          // 更新薪酬单ID
          if (response.data && response.data.salaryId) {
            this.salaryDetail.salaryId = response.data.salaryId
          }
        } else {
          throw new Error(response.message || '保存失败')
        }
      } catch (error) {
        console.error('保存草稿失败:', error)
        this.$message.error('保存失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    // 提交表单
    async handleSubmitForm() {
      try {
        // 验证数据
        const hasZeroSalary = this.salaryItems.some(item => 
          item.baseSalary === 0 && 
          item.performanceBonus === 0 && 
          item.transportAllowance === 0 && 
          item.mealAllowance === 0 && 
          item.rewardAmount === 0
        )
        
        if (hasZeroSalary) {
          this.$confirm('存在薪酬为0的员工，是否继续提交？', '提示', {
            confirmButtonText: '继续提交',
            cancelButtonText: '返回修改',
            type: 'warning'
          }).then(() => {
            this.doSubmit()
          }).catch(() => {})
        } else {
          this.doSubmit()
        }
      } catch (error) {
        console.error('提交前验证失败:', error)
      }
    },
    
    // 执行提交
    async doSubmit() {
      try {
        this.loading = true
        
        // 更新实发金额
        this.salaryItems.forEach(row => this.calculateActualAmount(row))
        
        const salaryData = {
          ...this.salaryDetail,
          status: 1, // 已提交状态
          salaryItems: this.salaryItems,
          submitTime: new Date(),
          submitterId: this.getCurrentUserId(),
          submitterName: this.getCurrentUserName(),
          totalAmount: this.salaryItems.reduce((sum, item) => sum + item.actualAmount, 0),
          totalEmployees: this.salaryItems.length
        }
        
        const response = await submitSalary(salaryData)
        
        if (response.code === 200 && response.success) {
          this.$message.success('薪酬单提交成功！')
          // 跳转到薪酬单列表
          this.$router.push('/salary/payment')
        } else {
          throw new Error(response.message || '提交失败')
        }
      } catch (error) {
        console.error('提交失败:', error)
        this.$message.error('提交失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    // 取消返回
    handleCancel() {
      this.$confirm('确定要取消创建吗？未保存的数据将会丢失。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.salaryDetail = null
        this.salaryItems = []
        this.$message.info('已取消创建')
      }).catch(() => {})
    },
    
    // 导出模板
    handleExportTemplate() {
      this.$message.info('导出模板功能开发中...')
    },
    
    // 导入数据
    handleImport() {
      this.$message.info('导入数据功能开发中...')
    },
    
    // 获取当前用户ID
    getCurrentUserId() {
      const user = localStorage.getItem('user')
      return user ? JSON.parse(user).userId : null
    },
    
    // 获取当前用户名
    getCurrentUserName() {
      const user = localStorage.getItem('user')
      return user ? JSON.parse(user).userName : '未知用户'
    }
  },
  
  filters: {
    currency(value) {
      if (!value && value !== 0) return '0.00'
      return Number(value).toFixed(2)
    }
  }
}
</script>

<style lang="scss" scoped>
.salary-payment-form {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
  
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
    
    .header-info {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-top: 8px;
      font-size: 14px;
      color: #606266;
      
      .salary-code {
        color: #409EFF;
        font-weight: 500;
      }
      
      .org-info {
        color: #67C23A;
      }
      
      .create-time {
        color: #909399;
      }
    }
    
    .header-actions {
      display: flex;
      gap: 12px;
    }
  }
  
  // 机构选择弹窗样式
  .org-selector {
    display: flex;
    gap: 24px;
    height: 400px;
    
    .org-tree-section {
      flex: 1;
      border-right: 1px solid #ebeef5;
      padding-right: 24px;
      
      h3 {
        margin: 0 0 16px 0;
        color: #303133;
        font-size: 16px;
      }
      
      .org-search {
        margin-bottom: 16px;
      }
      
      .tree-container {
        height: calc(100% - 100px);
        overflow-y: auto;
        
        ::v-deep .el-tree {
          .custom-tree-node {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 14px;
            
            .org-node {
              display: flex;
              align-items: center;
              gap: 6px;
              
              .org-icon {
                color: #409EFF;
                font-size: 16px;
              }
              
              .org-type-tag {
                font-size: 12px;
                color: #E6A23C;
                background: #fdf6ec;
                padding: 1px 6px;
                border-radius: 3px;
                margin-left: 8px;
              }
            }
          }
          
          .el-tree-node.is-current > .el-tree-node__content {
            background-color: #f0f9ff;
            
            .custom-tree-node {
              color: #409EFF;
            }
          }
        }
      }
    }
    
    .selected-org-section {
      flex: 1;
      
      h3 {
        margin: 0 0 16px 0;
        color: #303133;
        font-size: 16px;
      }
      
      .selected-org-info {
        padding: 16px;
        background: #fafafa;
        border-radius: 6px;
        
        .org-detail {
          margin-bottom: 16px;
          
          .detail-item {
            display: flex;
            margin-bottom: 8px;
            font-size: 14px;
            
            .detail-label {
              width: 80px;
              color: #909399;
              flex-shrink: 0;
            }
            
            .detail-value {
              color: #303133;
              font-weight: 500;
            }
          }
        }
        
        .org-path {
          .path-label {
            color: #909399;
            font-size: 14px;
            margin-bottom: 8px;
          }
          
          .path-value {
            display: flex;
            align-items: center;
            gap: 8px;
            flex-wrap: wrap;
            
            .path-arrow {
              color: #c0c4cc;
              font-size: 12px;
            }
          }
        }
      }
    }
  }
  
  // 统计卡片
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
  
  // 表格容器
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
      
      .table-subtitle {
        flex: 1;
        margin-left: 20px;
        color: #606266;
        font-size: 14px;
      }
      
      .table-actions {
        display: flex;
        gap: 12px;
      }
    }
    
    .salary-input {
      width: 100%;
      text-align: right;
      
      ::v-deep .el-input__inner {
        text-align: right;
        padding-right: 5px;
      }
    }
    
    .actual-amount {
      font-weight: 600;
      color: #409EFF;
      font-size: 14px;
    }
    
    .no-data {
      color: #909399;
      font-style: italic;
    }
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
    padding: 8px 0;
  }
  
  // 应发项目标题样式
  .income-header {
    background-color: #f0f9eb !important;
  }
  
  // 代扣项目标题样式
  .deduction-header {
    background-color: #fef0f0 !important;
  }
  
  // 实发金额标题样式
  .actual-header {
    background-color: #f0f9ff !important;
  }
}

// 响应式设计
@media screen and (max-width: 768px) {
  .salary-payment-form {
    padding: 10px;
    
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }
    
    .stats-cards {
      grid-template-columns: 1fr;
    }
    
    .org-selector {
      flex-direction: column;
      height: auto;
      
      .org-tree-section {
        border-right: none;
        border-bottom: 1px solid #ebeef5;
        padding-right: 0;
        padding-bottom: 24px;
        margin-bottom: 24px;
      }
    }
  }
}
</style>