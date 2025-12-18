<template>
  <div class="standard-form">
    <!-- 加载遮罩 -->
    <div v-if="loading && !formInitiated" class="loading-overlay">
      <el-card shadow="never" style="padding: 30px; text-align: center;">
        <div style="margin-bottom: 20px;">
          <i class="el-icon-loading" style="font-size: 40px; color: #409EFF;"></i>
        </div>
        <h3>{{ isEditMode ? '正在加载标准数据...' : '正在初始化表单...' }}</h3>
        <p>{{ isEditMode ? '请稍候，正在获取标准详细信息' : '请稍候，正在获取职位和项目数据' }}</p>
      </el-card>
    </div>

    <!-- 表单主体 -->
    <el-form ref="form" :model="formData" :rules="formRules" v-if="formInitiated">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>{{ isEditMode ? '编辑薪酬标准' : '新建薪酬标准' }}</h2>
        <div class="header-actions">
          <el-button icon="el-icon-back" @click="handleBack">
            返回列表
          </el-button>
        </div>
      </div>

      <!-- 主表区域 -->
      <el-card class="main-card">
        <div slot="header" class="card-header">
          <span>标准主表信息</span>
          <el-tag v-if="formData.status" :type="getStatusTagType(formData.status)">
            {{ getStatusText(formData.status) }}
          </el-tag>
        </div>
        
        <!-- 标准ID（编辑模式显示） -->
        <el-form-item v-if="isEditMode" label="标准ID" prop="standardId">
          <el-input v-model="formData.standardId" disabled />
          <div class="form-tips">系统内部唯一标识</div>
        </el-form-item>

        <!-- 标准编码 -->
        <el-form-item label="标准编码" prop="standardCode">
          <el-input v-model="formData.standardCode" :disabled="isEditMode">
            <el-button
              slot="append"
              icon="el-icon-refresh"
              @click="handleGenerateCode"
              :disabled="isEditMode || !canRefreshCode"
              v-if="!isEditMode"
            >
              重新生成
            </el-button>
          </el-input>
          <div class="form-tips">{{ isEditMode ? '标准编码不可修改' : '编码由系统自动生成，确保唯一性' }}</div>
        </el-form-item>

        <!-- 标准名称 -->
        <el-form-item label="标准名称" prop="standardName">
          <el-input 
            v-model="formData.standardName" 
            placeholder="请输入标准名称"
            maxlength="200"
            show-word-limit
            :disabled="!isEditable"
          />
          <div class="form-tips">{{ isEditable ? '请填写标准名称，长度2-200字符' : '已提交或已生效的标准名称不可修改' }}</div>
        </el-form-item>
        
        <!-- 适用职位 -->
        <el-form-item label="适用职位" prop="positionIds">
          <el-select
            v-model="formData.positionIds"
            multiple
            filterable
            placeholder="请选择职位"
            style="width: 100%"
            @change="handlePositionSelect"
            :disabled="!isEditable"
            :loading="loadingPositions"
            clearable
          >
            <!-- 添加搜索和筛选功能 -->
            <div slot="prefix" style="display: flex; align-items: center; padding: 0 10px;">
              <i class="el-icon-search"></i>
            </div>
            
            <el-option-group
              v-for="(group, groupIndex) in groupedPositions"
              :key="groupIndex"
              :label="group.label"
            >
              <el-option
                v-for="position in group.positions"
                :key="position.posId"
                :label="getPositionDisplayName(position)"
                :value="position.posId"
                :disabled="getPositionDisabled(position.posId)"
              >
                <div style="display: flex; flex-direction: column; padding: 5px 0;">
                  <!-- 第一行：职位基本信息 -->
                  <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 5px;">
                    <div>
                      <span style="font-weight: bold; color: #303133;">{{ position.posCode }} - {{ position.posName }}</span>
                      <el-tag 
                        v-if="position.status === 1" 
                        size="mini" 
                        type="success"
                        style="margin-left: 8px;"
                      >
                        启用
                      </el-tag>
                      <el-tag 
                        v-else 
                        size="mini" 
                        type="danger"
                        style="margin-left: 8px;"
                      >
                        禁用
                      </el-tag>
                    </div>
                    <div>
                      <el-tag 
                        v-if="getPositionDisabled(position.posId)" 
                        size="mini" 
                        type="danger"
                        style="margin-left: 8px;"
                      >
                        已占用
                      </el-tag>
                      <el-tag 
                        v-else-if="formData.positionIds.includes(position.posId)"
                        size="mini" 
                        type="success"
                        style="margin-left: 8px;"
                      >
                        已选择
                      </el-tag>
                    </div>
                  </div>
                  
                  <!-- 第二行：机构完整路径 -->
                  <div style="font-size: 12px; color: #909399; line-height: 1.4;">
                    <i class="el-icon-office-building" style="margin-right: 5px;"></i>
                    <span>{{ position.orgFullPath || position.orgName || '无机构信息' }}</span>
                  </div>
                  
                  <!-- 第三行：机构类型和ID -->
                  <div style="font-size: 11px; color: #c0c4cc; display: flex; justify-content: space-between; margin-top: 3px;">
                    <span>机构ID: {{ position.orgId }}</span>
                    <span v-if="position.orgType">
                      类型: {{ getOrgTypeText(position.orgType) }}
                    </span>
                  </div>
                </div>
              </el-option>
            </el-option-group>
          </el-select>
          <div class="form-tips">
            <div>已选择 {{ formData.positionIds.length }} 个职位</div>
            <div style="color: #F56C6C; font-size: 11px; margin-top: 5px;">
              ※ 红色"已占用"标签表示该职位已被其他标准占用，不可选择
            </div>
            <div style="color: #67C23A; font-size: 11px; margin-top: 2px;">
              ※ 绿色"已选择"标签表示当前已选择的职位
            </div>
          </div>
        </el-form-item>

        <!-- 制定人信息 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="制定人" prop="creatorName">
              <el-input v-model="formData.creatorName" disabled>
                <i slot="prefix" class="el-icon-user"></i>
              </el-input>
              <div class="form-tips">{{ isEditMode ? '标准创建人' : '当前登录用户' }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="制定时间" prop="createdAt">
              <el-date-picker
                v-model="formData.createdAt"
                type="datetime"
                placeholder="创建时间"
                style="width: 100%"
                disabled
              />
              <div class="form-tips">{{ isEditMode ? '标准创建时间' : '当前时间' }}</div>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 登记人信息（仅在提交审核后显示） -->
        <template v-if="formData.status >= 2">
          <el-divider content-position="left">登记信息</el-divider>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="登记人" prop="registrarName">
                <el-input v-model="formData.registrarName" disabled>
                  <i slot="prefix" class="el-icon-user-solid"></i>
                </el-input>
                <div class="form-tips">审核通过时记录</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="登记时间" prop="registrationTime">
                <el-date-picker
                  v-model="formData.registrationTime"
                  type="datetime"
                  placeholder="登记时间"
                  style="width: 100%"
                  disabled
                />
                <div class="form-tips">审核通过时间</div>
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- 状态信息 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="当前状态" prop="status">
              <el-tag 
                :type="getStatusTagType(formData.status)" 
                size="medium" 
                effect="dark"
                style="width: 100%; text-align: center;"
              >
                {{ getStatusText(formData.status) || '草稿' }}
              </el-tag>
              <div class="form-tips">{{ getStatusDescription(formData.status) }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最后更新" prop="updatedAt">
              <el-date-picker
                v-model="formData.updatedAt"
                type="datetime"
                placeholder="更新时间"
                style="width: 100%"
                disabled
              />
              <div class="form-tips">最后操作时间</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>
      
      <!-- 薪酬项目明细表区域 -->
      <el-card class="detail-card">
        <div slot="header" class="card-header">
          <span>薪酬明细项目</span>
          <div>
            <span style="margin-right: 10px; color: #909399; font-size: 12px;">
              共 {{ salaryProjects.length }} 个项目可用
            </span>
            <el-button 
              type="primary" 
              size="small" 
              icon="el-icon-plus"
              @click="addSalaryItem"
              :disabled="!isEditable"
              v-if="isEditable"
            >
              添加项目
            </el-button>
            <el-tag v-else type="info" size="small">不可编辑</el-tag>
          </div>
        </div>
        
        <!-- 项目明细表格 -->
        <el-table
          :data="formData.items"
          border
          stripe
          size="small"
          style="width: 100%"
          :row-class-name="tableRowClassName"
          empty-text="暂无项目数据，请点击上方添加按钮"
        >
          <el-table-column label="序号" width="80" align="center">
            <template slot-scope="scope">
              <el-input-number
                v-model="scope.row.sortOrder"
                :min="1"
                :max="999"
                size="small"
                controls-position="right"
                style="width: 70px"
                :disabled="!isEditable"
              />
            </template>
          </el-table-column>
          
          <el-table-column label="项目编码" prop="projectCode" width="200">
            <template slot-scope="scope">
              <el-select
                v-model="scope.row.projectCode"
                placeholder="选择项目"
                style="width: 100%"
                :disabled="!isEditable"
                @change="(value) => handleProjectSelect(value, scope.row, scope.$index)"
                :loading="loadingProjects"
                clearable
                filterable
              >
                <el-option-group 
                  v-for="group in groupedProjects" 
                  :key="group.label" 
                  :label="group.label"
                >
                  <el-option
                    v-for="project in group.options"
                    :key="project.projectCode"
                    :label="`${project.projectCode} - ${project.projectName}`"
                    :value="project.projectCode"
                    :disabled="isProjectSelected(project.projectCode, scope.$index)"
                  >
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                      <div>
                        <span style="font-weight: bold;">{{ project.projectCode }}</span>
                        <span style="margin-left: 10px; color: #606266;">{{ project.projectName }}</span>
                      </div>
                      <div>
                        <el-tag 
                          size="mini" 
                          :type="project.projectType === 1 ? 'success' : project.projectType === 2 ? 'danger' : 'info'"
                        >
                          {{ getProjectTypeLabel(project.projectType) }}
                        </el-tag>
                      </div>
                    </div>
                  </el-option>
                </el-option-group>
              </el-select>
            </template>
          </el-table-column>
          
          <el-table-column label="项目名称" prop="projectName" width="180">
            <template slot-scope="scope">
              <span :class="{ 'text-muted': !scope.row.projectCode }">
                {{ getProjectName(scope.row.projectCode) || '未选择项目' }}
              </span>
            </template>
          </el-table-column>
          
          <el-table-column label="项目类型" width="120">
            <template slot-scope="scope">
              <el-tag 
                size="small"
                :type="getProjectTypeByCode(scope.row.projectCode) === 1 ? 'success' : 
                       getProjectTypeByCode(scope.row.projectCode) === 2 ? 'danger' : 'info'"
              >
                {{ getProjectTypeLabel(getProjectTypeByCode(scope.row.projectCode)) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="计算方式" width="150">
            <template slot-scope="scope">
              <el-select
                v-model="scope.row.calculationMethod"
                placeholder="选择方式"
                size="small"
                style="width: 100%"
                :disabled="!isEditable"
                @change="handleCalculationMethodChange(scope.row)"
              >
                <el-option label="固定金额" value="fixed" />
                <el-option label="基本工资百分比" value="percentage" />
              </el-select>
            </template>
          </el-table-column>
          
          <el-table-column label="金额/百分比" width="200">
            <template slot-scope="scope">
              <template v-if="scope.row.calculationMethod === 'fixed'">
                <el-input-number
                  v-model="scope.row.amount"
                  :precision="2"
                  :step="100"
                  :min="0"
                  :max="9999999"
                  style="width: 100%"
                  placeholder="请输入金额"
                  :disabled="!isEditable"
                  controls-position="right"
                  @change="calculateFixedItemAmount(scope.row)"
                >
                  <template slot="append">元</template>
                </el-input-number>
                <div style="margin-top: 5px; font-size: 11px; color: #67C23A;">
                  固定值: ¥ {{ (scope.row.amount || 0).toFixed(2) }}
                </div>
              </template>
              
              <template v-else-if="scope.row.calculationMethod === 'percentage'">
                <el-input-number
                  v-model="scope.row.amount"
                  :precision="2"
                  :step="5"
                  :min="0"
                  :max="1000"
                  style="width: 100%"
                  placeholder="请输入百分比"
                  :disabled="!isEditable"
                  controls-position="right"
                  @change="calculatePercentageItemAmount(scope.row)"
                >
                  <template slot="append">%</template>
                </el-input-number>
                <div style="margin-top: 5px; font-size: 11px; color: #409EFF;">
                  计算: BASE_SALARY × {{ (scope.row.amount || 0) / 100 }}
                </div>
              </template>
              
              <template v-else>
                <span style="color: #c0c4cc;">请选择计算方式</span>
              </template>
            </template>
          </el-table-column>

          <!-- 新增：项目金额计算列 -->
          <el-table-column label="项目金额" width="150" align="right">
            <template slot-scope="scope">
              <div v-if="scope.row.calculationMethod === 'fixed' && scope.row.amount">
                <span :style="{ 
                  color: getProjectTypeByCode(scope.row.projectCode) === 1 ? '#67C23A' : '#F56C6C',
                  fontWeight: 'bold'
                }">
                  {{ getProjectTypeByCode(scope.row.projectCode) === 1 ? '+' : '-' }} ¥ {{ formatCurrency(scope.row.amount) }}
                </span>
              </div>
              <div v-else-if="scope.row.calculationMethod === 'percentage'">
                <span style="color: #909399; font-size: 12px;">
                  {{ scope.row.amount }}% × 基本工资
                </span>
              </div>
              <div v-else>
                <span style="color: #c0c4cc; font-size: 12px;">--</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="100" align="center" v-if="isEditable">
            <template slot-scope="scope">
              <el-button
                type="danger"
                icon="el-icon-delete"
                size="mini"
                circle
                @click="removeSalaryItem(scope.$index)"
                title="删除项目"
                :disabled="!scope.row.projectCode"
              />
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 总计信息 -->
        <div v-if="formData.items.length > 0" class="summary-section">
          <el-divider content-position="left">薪酬合计</el-divider>
          <div class="summary-content">
            <div class="summary-item">
              <span class="summary-label">总收入项：</span>
              <span class="summary-value income">{{ totalIncomeItems }} 项</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">总扣除项：</span>
              <span class="summary-value deduction">{{ totalDeductionItems }} 项</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">收入金额合计：</span>
              <span class="summary-value income" style="font-weight: bold;">+ ¥ {{ formatCurrency(totalIncomeAmount) }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">扣除金额合计：</span>
              <span class="summary-value deduction" style="font-weight: bold;">- ¥ {{ formatCurrency(totalDeductionAmount) }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">百分比项总计：</span>
              <span class="summary-value percentage">{{ totalPercentageItems }} 项 <i class="el-icon-warning" title="基于基本工资计算，具体金额因人而异"></i></span>
            </div>
            <div class="summary-item" style="grid-column: span 2; background-color: #f0f9ff; border-left-color: #409EFF;">
              <span class="summary-label" style="font-size: 16px; color: #303133;">固定金额合计：</span>
              <span class="summary-value amount" style="font-size: 18px; color: #409EFF; font-weight: bold;">
                {{ formatCurrency(totalNetAmount) }}
              </span>
            </div>
            <div class="summary-item" style="grid-column: span 2; background-color: #f8f9fa; margin-top: 10px;">
              <span class="summary-label" style="color: #909399;">说明：</span>
              <span class="summary-value" style="color: #606266; font-size: 12px;">
                固定金额合计 = 收入金额合计 - 扣除金额合计
              </span>
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 备注信息 -->
      <el-card class="remark-card">
        <div slot="header">
          <span>备注信息</span>
        </div>
        <el-form-item label="备注" prop="remark">
          <el-input
            type="textarea"
            v-model="formData.remark"
            :placeholder="isEditable ? '请输入备注信息（可选）' : '无备注信息'"
            :rows="3"
            :maxlength="500"
            show-word-limit
            :disabled="!isEditable"
          />
        </el-form-item>
      </el-card>
      
      <!-- 操作按钮区域 -->
      <div class="action-buttons">
        <!-- 权限提示信息 -->
        <div v-if="!hasEditPermission" class="permission-denied-tip">
          <el-alert 
            title="您没有编辑权限，请联系管理员" 
            type="warning" 
            :closable="false"
            show-icon
            style="margin-bottom: 20px;"
          />
        </div>

        <el-button 
          type="primary" 
          @click="handleSaveDraft"
          :loading="saving"
          :disabled="!isEditable"
          icon="el-icon-document"
        >
          {{ isEditMode ? '更新保存' : '保存草稿' }}
        </el-button>
        
        <el-button 
          v-if="hasEditPermission && isEditable"
          type="success" 
          @click="handleSubmitReview"
          :loading="submitting"
          :disabled="!canSubmitForReview"
          icon="el-icon-check"
        >
          提交审核
        </el-button>
        
        <el-button 
          type="info" 
          @click="handleResetForm"
          :disabled="!isEditable"
          icon="el-icon-refresh"
          v-if="!isEditMode"
        >
          重置表单
        </el-button>
        
        <el-button 
          type="warning" 
          @click="handleBack"
          icon="el-icon-close"
        >
          取消返回
        </el-button>
      </div>
      <!-- 添加保存提示 -->
      <div v-if="saving" class="saving-tip">
        <i class="el-icon-loading"></i> 正在保存数据，请稍候...
      </div>
    </el-form>
    
    <!-- 错误提示弹窗 -->
    <el-dialog
      :title="errorDialog.title"
      :visible.sync="errorDialog.visible"
      width="400px"
    >
      <div style="padding: 20px; text-align: center;">
        <i class="el-icon-warning" style="font-size: 50px; color: #E6A23C; margin-bottom: 20px;"></i>
        <p style="font-size: 16px; margin-bottom: 20px;">{{ errorDialog.message }}</p>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="errorDialog.visible = false">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
  generateStandardCode, 
  createStandard, 
  updateStandard, 

  getAllPositions,
  getAllProjects,
  checkPositionHasStandard,
  getStandardDetail
} from '@/api/salary/standards'

export default {
  name: 'StandardForm',
  data() {
    return {
      // 页面模式
      isEditMode: false,
      standardId: null,
      isReadonly: false,
      mode: 'create',
      
      // 加载状态
      loading: false,
      saving: false,
      submitting: false,
      loadingPositions: false,
      loadingProjects: false,
      formInitiated: false,
      
      // 数据
      positionList: [],
      salaryProjects: [],

      
      
      // 表单数据
      formData: {
        standardId: null,
        standardCode: '',
        standardName: '',
        positionIds: [],
        items: [],
        remark: '',
        status: 1,
        creatorId: null,
        creatorName: '',
        createdAt: null,
        registrarId: null,
        registrarName: '',
        registrationTime: null,
        updatedAt: new Date()
      },

      userRoles: [],
      
      // 表单验证规则
      formRules: {
        standardName: [
          { required: true, message: '请输入标准名称', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
        ],
        positionIds: [
          { required: true, message: '请至少选择一个适用职位', trigger: 'change' }
        ],
        items: [
          { 
            validator: (rule, value, callback) => {
              if (!value || value.length === 0) {
                callback(new Error('请至少添加一个薪酬项目'))
                return
              }
              
              // 检查是否有空项目
              const emptyProject = value.find(item => !item.projectCode)
              if (emptyProject) {
                callback(new Error('请为所有项目选择薪酬项目'))
                return
              }
              
              // 检查重复项目
              const projectCodes = value.map(item => item.projectCode)
              const uniqueCodes = [...new Set(projectCodes)]
              if (projectCodes.length !== uniqueCodes.length) {
                callback(new Error('不能选择重复的薪酬项目'))
                return
              }
              
              callback()
            },
            trigger: 'change'
          },
        ]
      },

      // 缓存对象
      positionDisabledCache: {}, // 存储职位禁用状态 {positionId: boolean}
      
      // 错误对话框
      errorDialog: {
        visible: false,
        title: '操作提示',
        message: ''
      },
      
      // 原始数据（用于编辑时的重置）
      originalFormData: null
    }
  },

  computed: {
    hasEditPermission() {
      const hasPermission = this.userRoles.includes(1) || 
                           this.userRoles.includes(5) || 
                           this.userRoles.includes(3);
      return hasPermission;
    },

    isEditable() {
      if (this.isReadonly) return false;
      if (!this.hasEditPermission) return false;
      if (!this.isEditMode) return true;
      return this.formData.status === 1 || this.formData.status === 0;
    },
    
    canRefreshCode() {
      return !this.isEditMode && this.formInitiated
    },
    
    // 修改：增加无效职位检查
    canSubmitForReview() {
      return this.isEditable && 
             this.formData.standardName.trim() && 
             this.formData.positionIds.length > 0 && 
             this.formData.items.length > 0 &&
             this.formData.items.every(item => item.projectCode) &&
             !this.hasInvalidPositions()
    },
    
    groupedPositions() {
      const groups = {}
      
      this.positionList.forEach(position => {
        const orgPath = position.orgFullPath || position.orgName || '未分组'
        
        if (!groups[orgPath]) {
          groups[orgPath] = {
            label: orgPath,
            positions: []
          }
        }
        
        groups[orgPath].positions.push(position)
      })
      
      return Object.values(groups)
        .map(group => ({
          ...group,
          positions: group.positions.sort((a, b) => 
            (a.posCode || '').localeCompare(b.posCode || '')
          )
        }))
        .sort((a, b) => a.label.localeCompare(b.label))
    },
    
    groupedProjects() {
      const incomeProjects = this.salaryProjects.filter(p => p.projectType === 1)
      const deductionProjects = this.salaryProjects.filter(p => p.projectType === 2)
      const otherProjects = this.salaryProjects.filter(p => !p.projectType || (p.projectType !== 1 && p.projectType !== 2))
      
      const groups = []
      
      if (incomeProjects.length > 0) {
        groups.push({
          label: `收入项目（${incomeProjects.length}）`,
          options: incomeProjects
        })
      }
      
      if (deductionProjects.length > 0) {
        groups.push({
          label: `扣除项目（${deductionProjects.length}）`,
          options: deductionProjects
        })
      }
      
      if (otherProjects.length > 0) {
        groups.push({
          label: `其他项目（${otherProjects.length}）`,
          options: otherProjects
        })
      }
      
      return groups
    },
    
    totalIncomeItems() {
      return this.formData.items.filter(item => {
        return this.getProjectTypeByCode(item.projectCode) === 1
      }).length
    },
    
    totalDeductionItems() {
      return this.formData.items.filter(item => {
        return this.getProjectTypeByCode(item.projectCode) === 2
      }).length
    },
    
    totalPercentageItems() {
      return this.formData.items.filter(item => item.calculationMethod === 'percentage').length
    },
    
    totalIncomeAmount() {
      return this.formData.items.reduce((total, item) => {
        if (item.calculationMethod === 'fixed' && 
            this.getProjectTypeByCode(item.projectCode) === 1 && 
            item.amount) {
          return total + Number(item.amount)
        }
        return total
      }, 0)
    },
    
    totalDeductionAmount() {
      return this.formData.items.reduce((total, item) => {
        if (item.calculationMethod === 'fixed' && 
            this.getProjectTypeByCode(item.projectCode) === 2 && 
            item.amount) {
          return total + Number(item.amount)
        }
        return total
      }, 0)
    },
    
    totalNetAmount() {
      return this.totalIncomeAmount - this.totalDeductionAmount
    }
  },
  
  watch: {
    '$route.query.id': {
      immediate: true,
      handler(newId) {
        if (newId) {
          this.isEditMode = true
          this.standardId = newId
        } else {
          this.isEditMode = false
          this.standardId = null
        }
      }
    }
  },
  
  created() {
    this.loadUserRoles();
    this.mode = this.$route.query.mode || 'create'
    this.isReadonly = this.$route.query.readonly === 'true' || this.mode === 'review'
    this.initForm();
  },
  
  methods: {
    // 修改：在初始化时加载职位禁用状态（创建模式也需要）
    async initForm() {
      try {
        this.loading = true
        console.log('初始化表单，模式:', this.isEditMode ? '编辑' : '创建')
        
        // 设置当前用户信息
        await this.setCurrentUserInfo()
        
        // 加载职位和项目数据
        await this.loadFormData()
        
        // 关键：加载职位禁用状态（无论创建还是编辑模式）
        await this.loadPositionDisabledStates()
        
        // 如果是编辑模式，加载已有数据
        if (this.isEditMode && this.standardId) {
          await this.loadStandardDetail()
        } else {
          // 创建模式：添加默认空项目
          await this.generateLocalCode()
          this.addDefaultSalaryItem()
        }
        
        // 保存原始数据
        this.originalFormData = JSON.parse(JSON.stringify(this.formData))
        
        this.formInitiated = true
        console.log('表单初始化完成')
        
      } catch (error) {
        console.error('初始化表单失败:', error)
        this.showError('表单初始化失败', error.message)
        this.formInitiated = true
      } finally {
        this.loading = false
      }
    },

    loadUserRoles() {
      try {
        const userStr = localStorage.getItem('user') || sessionStorage.getItem('user');
        if (userStr) {
          const user = JSON.parse(userStr);
          if(user.roleType){
            this.userRoles = [user.roleType];
          } else if (user.role && user.role.roleId) {
            this.userRoles = [user.role.roleId];
          } else if (user.roles) {
            this.userRoles = user.roles;
          } else {
            this.userRoles = [];
          }
        } else {
          this.userRoles = [];
        }
      } catch (error) {
        console.error('解析用户角色失败:', error);
        this.userRoles = [];
      }
    },

    // 修改：优化职位禁用判断逻辑
    getPositionDisabled(positionId) {
      // 创建模式：只要被任何标准占用就禁用
      if (!this.isEditMode) {
        return this.positionDisabledCache[positionId] === true
      }
      
      // 编辑模式：被其他标准占用且不在当前选择中才禁用
      const isInCurrentStandard = this.formData.positionIds.includes(positionId)
      if (isInCurrentStandard) {
        return false
      }
      
      return this.positionDisabledCache[positionId] === true
    },

    // 新增：检查是否有被占用的职位
    hasInvalidPositions() {
      return this.formData.positionIds.some(positionId => {
        return this.getPositionDisabled(positionId)
      })
    },

    // 新增：判断职位是否被占用（用于卡片显示）
    isPositionOccupied(positionId) {
      if (!this.positionDisabledCache) return false
      return this.positionDisabledCache[positionId] === true
    },
    
    // 修改：增强职位禁用状态加载（兼容创建模式）
    async loadPositionDisabledStates() {
      try {
        this.loadingPositions = true
        console.log('开始加载职位禁用状态...')
        
        // 确保职位数据已加载
        if (!this.positionList || this.positionList.length === 0) {
          console.warn('职位数据为空，跳过禁用状态检查')
          return
        }
        
        const currentStandardId = this.isEditMode ? this.standardId : null
        const checkPromises = this.positionList.map(async (position) => {
          const positionId = position.posId
          try {
            // 创建模式传null，编辑模式传当前标准ID
            const params = currentStandardId ? { currentStandardId } : {}
            const response = await checkPositionHasStandard(positionId, params)
            
            if (response && response.success) {
              const hasStandard = response.data.hasStandard
              this.$set(this.positionDisabledCache, positionId, hasStandard)
            }
          } catch (error) {
            console.error(`检查职位 ${positionId} 状态失败:`, error)
            this.$set(this.positionDisabledCache, positionId, false)
          }
        })
        
        await Promise.all(checkPromises)
        console.log('职位禁用状态加载完成')
        
      } catch (error) {
        console.error('加载职位禁用状态失败:', error)
        this.$message.error('加载职位状态失败')
      } finally {
        this.loadingPositions = false
      }
    },
    
    async loadFormData() {
      try {
        await Promise.all([
          this.loadPositionsData(),
          this.loadProjectsData()
        ])
      } catch (error) {
        console.error('加载表单数据失败:', error)
        throw error
      }
    },
    
    async setCurrentUserInfo() {
      try {
        const userStr = localStorage.getItem('user') || sessionStorage.getItem('user')
        if (userStr) {
          const user = JSON.parse(userStr)
          this.formData.creatorId = user.userId || user.id
          this.formData.creatorName = user.userName || user.realName || user.nickname || '未知用户'
        } else {
          this.formData.creatorName = '当前用户'
        }
      } catch (error) {
        console.error('解析用户信息失败:', error)
        this.formData.creatorName = '系统用户'
      }
      
      if (!this.formData.createdAt) {
        this.formData.createdAt = new Date()
      }
      this.formData.updatedAt = new Date()
    },
    
    async generateStandardCode() {
      try {
        const response = await generateStandardCode()
        if (response && response.success) {
          this.formData.standardCode = response.data.standardCode
        } else {
          this.generateLocalCode()
        }
      } catch (error) {
        console.error('API生成编码失败，使用本地生成:', error)
        this.generateLocalCode()
      }
    },
    
    generateLocalCode() {
      const timestamp = Date.now()
      const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0')
      this.formData.standardCode = `STD${timestamp}${random}`
    },
    
    handleGenerateCode() {
      if (!this.canRefreshCode) return
      this.generateLocalCode()
      this.$message.success('标准编码已重新生成')
    },
    
    async loadPositionsData() {
      try {
        this.loadingPositions = true
        const response = await getAllPositions()
        
        if (response && response.success) {
          this.positionList = Array.isArray(response.data) ? response.data : []
          if (this.positionList.length === 0) {
            this.$message.warning('未获取到职位数据')
          }
        } else {
          throw new Error(response?.message || '获取职位数据失败')
        }
      } catch (error) {
        console.error('加载职位数据失败:', error)
        this.$message.error('加载职位数据失败')
        this.positionList = []
      } finally {
        this.loadingPositions = false
      }
    },
    
    async loadProjectsData() {
      try {
        this.loadingProjects = true
        const response = await getAllProjects()
        
        if (response && response.success) {
          this.salaryProjects = Array.isArray(response.data) ? response.data : []
        } else {
          throw new Error(response?.message || '获取项目数据失败')
        }
      } catch (error) {
        console.error('加载项目数据失败:', error)
        this.$message.error('加载项目数据失败')
        this.salaryProjects = []
      } finally {
        this.loadingProjects = false
      }
    },
    
    async loadStandardDetail() {
      try {
        this.loading = true
        const response = await getStandardDetail(this.standardId)
        
        if (response && response.success) {
          const data = response.data
          Object.assign(this.formData, {
            standardId: data.standardId,
            standardCode: data.standardCode || '',
            standardName: data.standardName || '',
            positionIds: data.positions ? data.positions.map(p => p.posId) : [],
            items: data.items ? data.items.map(item => ({
              id: item.id,
              projectCode: item.projectCode || '',
              projectName: item.projectName || '',
              amount: Number(item.amount || 0),
              calculationMethod: item.calculationMethod || 'fixed',
              sortOrder: Number(item.sortOrder || 0)
            })) : [],
            remark: data.remark || '',
            status: data.status || 1,
            creatorId: data.creatorId,
            creatorName: data.creatorName,
            createdAt: data.createdAt ? new Date(data.createdAt) : new Date(),
            registrarId: data.registrarId,
            registrarName: data.registrarName,
            registrationTime: data.registrationTime ? new Date(data.registrationTime) : null,
            updatedAt: data.updatedAt ? new Date(data.updatedAt) : new Date()
          })
          
          this.originalFormData = JSON.parse(JSON.stringify(this.formData))
          this.$message.success('标准详情加载成功')
        } else {
          throw new Error(response?.message || '加载数据失败')
        }
      } catch (error) {
        console.error('加载标准详情失败:', error)
        this.showError('加载数据失败', error.message)
      } finally {
        this.loading = false
      }
    },
    
    addDefaultSalaryItem() {
      if (this.formData.items.length === 0) {
        this.formData.items.push({
          projectCode: '',
          amount: 0,
          calculationMethod: 'fixed',
          sortOrder: 1
        })
      }
    },
    
    addSalaryItem() {
      const newSortOrder = this.formData.items.length > 0 
        ? Math.max(...this.formData.items.map(item => item.sortOrder)) + 1
        : 1
      
      this.formData.items.push({
        projectCode: '',
        amount: 0,
        calculationMethod: 'fixed',
        sortOrder: newSortOrder
      })
    },
    
    removeSalaryItem(index) {
      this.$confirm('确定要删除这个项目吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.formData.items.splice(index, 1)
        this.formData.items.forEach((item, idx) => {
          item.sortOrder = idx + 1
        })
        this.$message.success('项目已删除')
      }).catch(() => {})
    },
    
    // 修改：强化职位选择验证
    handlePositionSelect(selectedIds) {
      // 找出新添加的职位ID
      const currentSelectedIds = [...(this.formData.positionIds || [])]
      const newlyAddedIds = selectedIds.filter(id => !currentSelectedIds.includes(id))
      
      // 检查新增的职位是否被占用
      const invalidPositions = []
      
      newlyAddedIds.forEach(positionId => {
        if (this.getPositionDisabled(positionId)) {
          invalidPositions.push(positionId)
        }
      })
      
      if (invalidPositions.length > 0) {
        const positionNames = invalidPositions.map(id => {
          const position = this.positionList.find(p => p.posId === id)
          return position ? `${position.posCode}-${position.posName}` : `ID: ${id}`
        })
        
        this.$message.warning(`以下职位已被其他标准占用，无法选择：${positionNames.join('、')}`)
        
        // 从选择中移除无效的职位
        this.formData.positionIds = selectedIds.filter(id => !invalidPositions.includes(id))
        return
      }
      
      this.formData.positionIds = selectedIds
    },
    
    getPositionFullName(position) {
      if (!position) return ''
      const posCode = position.posCode || ''
      const posName = position.posName || ''
      const orgFullPath = position.orgFullPath || position.orgName || ''
      return `${posCode} - ${posName} (${orgFullPath})`
    },

    getPositionDisplayName(position) {
      if (!position) return ''
      const posName = position.posName || ''
      const orgName = position.orgName || ''
      return `${posName} - ${orgName}`
    },
    
    handleProjectSelect(projectCode, item, index) {
      if (!projectCode) {
        this.$set(this.formData.items, index, {
          ...item,
          projectCode: '',
          amount: 0,
          calculationMethod: 'fixed'
        })
        return
      }
      
      const project = this.salaryProjects.find(p => p.projectCode === projectCode)
      if (project) {
        const updatedItem = {
          ...item,
          projectCode: projectCode,
          calculationMethod: project.defaultCalculationMethod || 'fixed',
          amount: project.defaultAmount || 0
        }
        
        this.$set(this.formData.items, index, updatedItem)
        this.recalculateItem(updatedItem)
      }
    },
    
    handleCalculationMethodChange(item) {
      item.amount = 0
      this.recalculateItem(item)
    },
    
    calculateFixedItemAmount(item) {
      this.recalculateItem(item)
    },
    
    calculatePercentageItemAmount(item) {
      this.recalculateItem(item)
    },
    
    recalculateItem() {
      this.$forceUpdate()
    },
    
    recalculateAllItems() {
      this.$forceUpdate()
    },
    
    isProjectSelected(projectCode, currentIndex) {
      return this.formData.items.some((item, index) => 
        index !== currentIndex && item.projectCode === projectCode
      )
    },
    
    getProjectName(projectCode) {
      if (!projectCode) return ''
      const project = this.salaryProjects.find(p => p.projectCode === projectCode)
      return project ? project.projectName : ''
    },
    
    getProjectTypeByCode(projectCode) {
      if (!projectCode) return 0
      const project = this.salaryProjects.find(p => p.projectCode === projectCode)
      return project ? project.projectType : 0
    },
    
    getProjectTypeLabel(type) {
      switch (type) {
        case 1: return '收入项'
        case 2: return '扣除项'
        default: return '其他'
      }
    },
    
    getPositionById(positionId) {
      return this.positionList.find(p => p.posId === positionId) || {}
    },

    removeSelectedPosition(positionId) {
      const index = this.formData.positionIds.indexOf(positionId)
      if (index > -1) {
        this.formData.positionIds.splice(index, 1)
        this.$message.success('已移除选中的职位')
      }
    },
    
    getOrgTypeText(type) {
      switch(type) {
        case 1: return '一级机构'
        case 2: return '二级机构'
        case 3: return '三级机构'
        default: return '其他'
      }
    },
    
    formatCurrency(value) {
      if (value === null || value === undefined) return '0.00'
      const num = Number(value)
      if (isNaN(num)) return '0.00'
      return num.toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })
    },
    
    // 修改：保存前验证职位占用
    async handleSaveDraft() {
      if (this.saving) return;
      
      this.saving = true;
      try {
        await this.$refs.form.validate()
        
        // 检查是否有被占用的职位
        if (this.hasInvalidPositions()) {
          const invalidPositions = this.formData.positionIds.filter(id => this.getPositionDisabled(id))
          const positionNames = invalidPositions.map(id => {
            const position = this.positionList.find(p => p.posId === id)
            return position ? `${position.posCode}-${position.posName}` : `ID: ${id}`
          })
          this.$message.error(`保存失败：以下职位已被其他标准占用\n${positionNames.join('、')}`)
          return
        }
        
        // 原有保存逻辑保持不变
        const userStr = localStorage.getItem('user') || sessionStorage.getItem('user')
        let userId = null
        if (userStr) {
          try {
            const userInfo = JSON.parse(userStr)
            userId = userInfo.userId || userInfo.id
          } catch (e) {
            console.error('解析用户信息失败:', e)
          }
        }
        
        if (!userId) {
          this.$message.error('未获取到用户信息，请重新登录')
          this.$router.push('/login')
          return
        }
        
        const saveData = {
          standardCode: this.formData.standardCode,
          standardName: this.formData.standardName.trim(),
          positionIds: this.formData.positionIds.map(id => Number(id)),
          items: this.formData.items
            .filter(item => item.projectCode)
            .map(item => ({
              projectCode: item.projectCode,
              amount: Number(item.amount || 0),
              calculationMethod: item.calculationMethod || 'fixed',
              sortOrder: Number(item.sortOrder || 0)
            })),
          remark: (this.formData.remark || '').trim(),
          status: this.formData.status || 1
        }

        if (!saveData.standardName) {
          this.$message.error("标准名称不能为空！")
          return
        }
        
        let response
        if (!this.isEditMode) {
          saveData.creatorId = Number(userId)
          response = await createStandard(saveData)
        } else {
          saveData.updaterId = Number(userId)
          response = await updateStandard(this.standardId, saveData)
        }
        
        if (response && response.success) {
          const resultData = response.data
          if (resultData && resultData.standardId) {
            this.formData.standardId = resultData.standardId
            
            if (!this.isEditMode) {
              this.isEditMode = true
              this.standardId = resultData.standardId
              
              this.$router.replace({
                path: this.$route.path,
                query: { id: resultData.standardId }
              })
            }
          }
          
          this.originalFormData = JSON.parse(JSON.stringify(this.formData))
          
          this.$message.success({
            message: this.isEditMode ? '更新保存成功！' : '创建成功！',
            duration: 2000,
            showClose: true
          })
          
          setTimeout(() => {
            this.$router.push('/salary/standards')
          }, 2000)
          
        } else {
          throw new Error(response?.message || '保存失败')
        }
        
      } catch (error) {
        console.error('保存失败:', error)
        
        if (error.message && (
          error.message.includes('JWT') || 
          error.message.includes('Token') ||
          error.message.includes('401') ||
          error.message.includes('未登录')
        )) {
          this.showError('登录过期', '您的登录已过期，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          setTimeout(() => {
            this.$router.push('/login')
          }, 2000)
        } else {
          this.showError(this.isEditMode ? '更新失败' : '保存失败', error.message)
        }
      } finally {
        setTimeout(() => {
          this.saving = false;
        }, 2000);
      }
    },
    
    async handleSubmitReview() {
      try {
        await this.$refs.form.validate()
        
        this.$confirm('确定要提交审核吗？提交后不可编辑，等待管理员审核。', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(async () => {
          this.submitting = true
          
          this.formData.status = 2
          this.formData.registrationTime = new Date()
          
          await this.handleSaveDraft()
          
          this.$message.success('提交审核成功')
          setTimeout(() => {
            this.$router.push('/salary/standards')
          }, 1500)
          
        }).catch(() => {
          console.log('取消提交审核')
        })
      } catch (error) {
        console.error('提交审核失败:', error)
        this.showError('提交审核失败', error.message)
      } finally {
        this.submitting = false
      }
    },
    
    handleResetForm() {
      this.$confirm(this.isEditMode ? '确定要重置表单吗？所有未保存的更改将丢失。' : '确定要重置表单吗？所有输入将被清空。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        if (this.isEditMode && this.originalFormData) {
          Object.assign(this.formData, JSON.parse(JSON.stringify(this.originalFormData)))
        } else {
          const defaultFormData = {
            standardId: null,
            standardCode: this.formData.standardCode,
            standardName: '',
            positionIds: [],
            items: [],
            remark: '',
            status: 1,
            creatorId: this.formData.creatorId,
            creatorName: this.formData.creatorName,
            createdAt: this.formData.createdAt,
            registrarId: null,
            registrarName: '',
            registrationTime: null,
            updatedAt: new Date()
          }
          Object.assign(this.formData, defaultFormData)
          this.addDefaultSalaryItem()
        }
        
        this.$message.success('表单已重置')
      }).catch(() => {
        console.log('取消重置')
      })
    },
    
    handleBack() {
      if (this.hasUnsavedChanges()) {
        this.$confirm('您有未保存的更改，确定要离开吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$router.push('/salary/standards')
        }).catch(() => {
          console.log('取消离开')
        })
      } else {
        this.$router.push('/salary/standards')
      }
    },
    
    hasUnsavedChanges() {
      if (!this.originalFormData) return false
      return JSON.stringify(this.formData) !== JSON.stringify(this.originalFormData)
    },
    
    tableRowClassName({ rowIndex }) {
      return rowIndex % 2 === 0 ? 'even-row' : 'odd-row'
    },
    
    getStatusTagType(status) {
      switch (status) {
        case 1: return 'info'
        case 2: return 'warning'
        case 3: return 'success'
        case 0: return 'danger'
        default: return ''
      }
    },
    
    getStatusText(status) {
      switch (status) {
        case 1: return '草稿'
        case 2: return '待审核'
        case 3: return '已生效'
        case 0: return '已驳回'
        default: return '未知'
      }
    },
    
    getStatusDescription(status) {
      switch (status) {
        case 1: return '可编辑和保存'
        case 2: return '已提交审核，等待审批'
        case 3: return '已生效，不可编辑'
        case 0: return '审核驳回，可修改后重新提交'
        default: return ''
      }
    },
    
    showError(title, message) {
      this.errorDialog = {
        visible: true,
        title: title,
        message: message
      }
    }
  }
}
</script>

<style scoped>
.standard-form {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

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
  border-radius: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.main-card,
.detail-card,
.remark-card {
  margin-bottom: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: 1px solid #ebeef5;
}

.main-card:hover,
.detail-card:hover,
.remark-card:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
  background: #f8f9fa;
  border-radius: 8px 8px 0 0;
}

.card-header span {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.form-tips {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  line-height: 1.4;
  padding: 5px 0;
}

.text-muted {
  color: #c0c4cc;
}



.position-card {
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
  height: 100%;
}

.position-card:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.1);
  transform: translateY(-2px);
}

.position-card-header {
  padding: 10px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e4e7ed;
}

.position-card-content {
  padding: 10px;
  font-size: 13px;
}

.summary-section {
  margin-top: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.summary-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
  padding: 10px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-radius: 6px;
  border-left: 4px solid #409EFF;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.summary-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.summary-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.summary-value {
  font-size: 16px;
  font-weight: 600;
}

.summary-value.income {
  color: #67C23A;
}

.summary-value.deduction {
  color: #F56C6C;
}

.summary-value.amount {
  color: #409EFF;
}

.summary-value.percentage {
  color: #E6A23C;
}

.summary-value.total {
  color: #909399;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 30px;
  padding: 25px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: 1px solid #ebeef5;
}

.action-buttons .el-button {
  min-width: 120px;
  padding: 12px 20px;
  font-size: 14px;
  border-radius: 6px;
}

/* 表格样式优化 */
::v-deep .even-row {
  background-color: #fafafa;
}

::v-deep .odd-row {
  background-color: white;
}

::v-deep .el-table .cell {
  padding: 10px 12px;
  line-height: 1.5;
}

::v-deep .el-table th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
  font-size: 14px;
}

::v-deep .el-table--border {
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

::v-deep .el-table--border td, 
::v-deep .el-table--border th {
  border-right: 1px solid #ebeef5;
}

::v-deep .el-table--border::before,
::v-deep .el-table--border::after {
  background-color: #ebeef5;
}

/* 职位选择下拉框样式优化 */
::v-deep .el-select-dropdown__item {
  height: auto !important;
  padding: 8px 20px !important;
  line-height: 1.5 !important;
}

::v-deep .el-select-dropdown__item.is-disabled {
  opacity: 0.6;
}

/* 选中状态的样式 */
::v-deep .el-select-dropdown__item.selected {
  background-color: #f0f9ff;
  color: #409EFF;
}

/* 分组标题样式 */
::v-deep .el-select-group__title {
  background-color: #f5f7fa !important;
  color: #909399 !important;
  font-weight: bold !important;
  padding: 10px 20px !important;
  border-bottom: 1px solid #ebeef5 !important;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .standard-form {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
    padding: 15px;
  }
  
  .position-card-header {
    padding: 8px;
  }
  
  .position-card-content {
    padding: 8px;
  }
  
  .summary-content {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 10px;
    padding: 15px;
  }
  
  .action-buttons .el-button {
    width: 100%;
    margin: 0;
  }
  
  ::v-deep .el-table {
    font-size: 12px;
  }
  
  ::v-deep .el-table .cell {
    padding: 6px 8px;
  }
  
  ::v-deep .el-select-dropdown__item {
    padding: 6px 15px !important;
  }
}

/* 滚动条样式 */
::v-deep .el-scrollbar__wrap {
  overflow-x: hidden;
}

/* 输入框和选择框样式优化 */
::v-deep .el-input-number .el-input__inner {
  text-align: left;
}

::v-deep .el-input-group__append {
  background-color: #f5f7fa;
  border-left: 1px solid #dcdfe6;
}

/* 保存提示 */
.saving-tip {
  text-align: center;
  color: #409EFF;
  font-size: 14px;
  margin-top: 10px;
  padding: 10px;
  background: #f0f9ff;
  border-radius: 4px;
  border: 1px solid #b3d8ff;
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter,
.fade-leave-to {
  opacity: 0;
}

/* 状态标签动画 */
::v-deep .el-tag {
  transition: all 0.3s ease;
}

::v-deep .el-tag:hover {
  transform: scale(1.05);
}
</style>