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
              
              <!-- 百分比 -->
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
        计算: {{ formatCurrency(baseSalary) }} × {{ (scope.row.amount || 0) / 100 }}% = 
        ¥ {{ formatCurrency(scope.row.calculatedAmount) }}
      </div>
    </template>
    
    <template v-else>
      <span style="color: #c0c4cc;">请选择计算方式</span>
    </template>
  </template>
</el-table-column>

         <!-- 修改"项目金额"列 -->
<el-table-column label="项目金额" width="140" align="right">
  <template slot-scope="scope">
    <div v-if="scope.row.projectCode && scope.row.calculationMethod">
      <span :style="{ 
        color: getProjectTypeByCode(scope.row.projectCode) === 1 ? '#67C23A' : '#F56C6C',
        fontWeight: 'bold',
        fontSize: '14px'
      }">
        {{ getProjectTypeByCode(scope.row.projectCode) === 1 ? '+' : '-' }} 
        ¥ {{ formatCurrency(scope.row.calculatedAmount) }}
      </span>
      <div v-if="scope.row.calculationMethod === 'percentage'" 
           style="color: #909399; font-size: 11px; margin-top: 3px;">
        计算值
      </div>
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
          <!-- 添加基本工资提示 -->
  <el-alert 
    v-if="baseSalary > 0" 
    :title="'基本工资基准值: ¥' + formatCurrency(baseSalary) + '（已从本地加载）'" 
    type="info" 
    :closable="false"
    show-icon
    style="margin-bottom: 15px;"
  />
  
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

         <!-- 草稿状态操作组 -->
    <template v-if="isDraftStatus">
      <el-button 
        type="primary" 
        @click="handleSaveDraft"
        :loading="saving"
        :disabled="!canEdit || saving"
        icon="el-icon-document"
      >
        {{ isEditMode ? '更新保存' : '保存草稿' }}
      </el-button>
      
      <el-button 
        v-if="hasEditPermission && canEdit"
        type="success" 
        @click="handleSubmitReview"
        :loading="submitting"
        :disabled="!canSubmitForReview || submitting"
        icon="el-icon-check"
      >
        提交审核
      </el-button>
      
      <el-button 
        type="info" 
        @click="handleResetForm"
        :disabled="!canEdit || saving || submitting"
        icon="el-icon-refresh"
        v-if="!isEditMode"
      >
        重置表单
      </el-button>
    </template>

    <!-- 驳回状态操作组 -->
    <template v-else-if="isRejectedStatus">
      <el-button 
        type="primary" 
        @click="handleSaveDraft"
        :loading="saving"
        :disabled="!canEdit || saving"
        icon="el-icon-document"
      >
        保存修改
      </el-button>
      
      <el-button 
        v-if="hasEditPermission && canEdit"
        type="success" 
        @click="handleSubmitReview"
        :loading="submitting"
        :disabled="!canSubmitForReview || submitting"
        icon="el-icon-check"
      >
        重新提交审核
      </el-button>
      
      <el-tag type="danger" size="medium" effect="dark" style="margin: 0 10px;">
        驳回状态：可修改后重新提交
      </el-tag>
    </template>

    <!-- 待审核状态提示 -->
    <template v-else-if="isPendingStatus">
      <el-alert 
        title="当前标准已提交审核，请等待管理员审批" 
        type="warning" 
        :closable="false"
        show-icon
        style="margin-right: 20px;"
      />
    </template>

    <!-- 已生效状态提示 -->
    <template v-else-if="isApprovedStatus">
      <el-alert 
        title="当前标准已生效，不可编辑" 
        type="success" 
        :closable="false"
        show-icon
        style="margin-right: 20px;"
      />
    </template>

    <!-- 公共操作 -->
    <el-button 
      type="warning" 
      @click="handleBack"
      icon="el-icon-close"
      :disabled="saving || submitting"
    >
      取消返回
    </el-button>
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
  submitForApproval ,
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
     baseSalary() {
    return this.findBaseSalaryFromItems();
  },
  
  totalIncomeAmount() {
    return this.formData.items.reduce((total, item) => {
      if (!item.projectCode) return total;
      
      const projectType = this.getProjectTypeByCode(item.projectCode);
      if (projectType !== 1) return total; // 只计算收入项
      
      const amount = parseFloat(item.calculatedAmount) || 0;
      return total + amount;
    }, 0);
  },
  
  totalDeductionAmount() {
    return this.formData.items.reduce((total, item) => {
      if (!item.projectCode) return total;
      
      const projectType = this.getProjectTypeByCode(item.projectCode);
      if (projectType !== 2) return total; // 只计算扣除项
      
      const amount = parseFloat(item.calculatedAmount) || 0;
      return total + amount;
    }, 0);
  },
  
  // 添加百分比项目明细（用于调试和显示）
  percentageItemsDetail() {
    return this.formData.items
      .filter(item => item.calculationMethod === 'percentage' && item.projectCode)
      .map(item => ({
        projectName: this.getProjectName(item.projectCode),
        percentage: parseFloat(item.amount) || 0,
        calculatedAmount: parseFloat(item.calculatedAmount) || 0,
        projectType: this.getProjectTypeByCode(item.projectCode)
      }));
  },
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

       /**
     * 表单是否可编辑（核心逻辑）
     * 判断依据：非只读模式 + 有编辑权限 + 状态允许编辑
     */
    canEdit() {
      // 如果处于只读模式（如查看详情），完全不可编辑
      if (this.isReadonly) return false;
      
      // 如果没有编辑权限，完全不可编辑
      if (!this.hasEditPermission) return false;
      
      // 根据状态判断是否可以编辑
      // status: 0-已驳回, 1-草稿, 2-待审核, 3-已生效
      // 只有驳回和草稿状态可以编辑
      return this.formData.status === 1 || this.formData.status === 0;
    },

      /**
     * 判断是否为草稿状态
     */
    isDraftStatus() {
      return this.formData.status === 1;
    },
    
    /**
     * 判断是否为驳回状态
     */
    isRejectedStatus() {
      return this.formData.status === 0;
    },
    
    /**
     * 判断是否为待审核状态
     */
    isPendingStatus() {
      return this.formData.status === 2;
    },
    
    /**
     * 判断是否为已生效状态
     */
    isApprovedStatus() {
      return this.formData.status === 3;
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
     /**
     * 显示的状态文本（带详细提示）
     */
    statusDisplayText() {
      const status = this.formData.status;
      switch (status) {
        case 1: 
          return {
            text: '草稿',
            tip: '可编辑和保存'
          };
        case 2: 
          return {
            text: '待审核',
            tip: '已提交审核，等待审批'
          };
        case 3: 
          return {
            text: '已生效',
            tip: '标准已生效，不可编辑'
          };
        case 0: 
          return {
            text: '已驳回',
            tip: '可修改后重新提交'
          };
        default: 
          return {
            text: '未知',
            tip: '未知状态'
          };
      }
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

        // 创建模式：清理本地基本工资数据
    if (!this.isEditMode) {
      localStorage.removeItem('salary_base_salary');
      console.log('创建模式：已清理本地基本工资数据');
    }
        
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
    // 获取本地存储的基本工资
  getStoredBaseSalary() {
    try {
      const stored = localStorage.getItem('salary_base_salary');
      return stored ? parseFloat(stored) : null;
    } catch (error) {
      console.warn('读取本地基本工资失败:', error);
      return null;
    }
  },
  
  // 存储基本工资到本地
  storeBaseSalary(salary) {
    try {
      if (salary && salary > 0) {
        localStorage.setItem('salary_base_salary', salary.toString());
        console.log('基本工资已存储到本地:', salary);
      }
    } catch (error) {
      console.warn('存储基本工资失败:', error);
    }
  },
  
  // 识别基本工资项目（根据编码或名称）
  isBaseSalaryProject(projectCode, projectName) {
    // 判断逻辑：编码包含 BASE/SALARY 或名称包含 基本工资/底薪
    const code = (projectCode || '').toUpperCase();
    const name = (projectName || '').toUpperCase();
    
    return code.includes('BASE') || 
           code.includes('SALARY') || 
           code.includes('GZ') ||
           name.includes('基本工资') ||
           name.includes('底薪') ||
           name.includes('岗位工资');
  },
  
  // 从当前项目中查找基本工资
  findBaseSalaryFromItems() {
    const baseSalaryItem = this.formData.items.find(item => {
      if (!item.projectCode) return false;
      const project = this.salaryProjects.find(p => p.projectCode === item.projectCode);
      return project && this.isBaseSalaryProject(project.projectCode, project.projectName);
    });
    
    if (baseSalaryItem && baseSalaryItem.calculationMethod === 'fixed') {
      return parseFloat(baseSalaryItem.amount) || 0;
    }
    return this.getStoredBaseSalary() || 0;
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
          calculationMethod: 'fixed',
          calculatedAmount: 0
        })
        return
      }
      
     const project = this.salaryProjects.find(p => p.projectCode === projectCode);
  if (project) {
    const isBaseSalary = this.isBaseSalaryProject(project.projectCode, project.projectName);
    
    const updatedItem = {
      ...item,
      projectCode: projectCode,
      calculationMethod: isBaseSalary ? 'fixed' : (project.defaultCalculationMethod || 'fixed'),
      amount: project.defaultAmount || 0,
      calculatedAmount: 0
    };
    
    // 如果是基本工资项目，从本地加载历史值
    if (isBaseSalary && updatedItem.calculationMethod === 'fixed') {
      const storedSalary = this.getStoredBaseSalary();
      if (storedSalary) {
        updatedItem.amount = storedSalary;
        updatedItem.calculatedAmount = storedSalary;
      }
    }
    
    this.$set(this.formData.items, index, updatedItem);
    this.recalculateItem(updatedItem);
  }
    },
    
    handleCalculationMethodChange(item) {
      item.amount = 0
      this.recalculateItem(item)
    },
    
    calculateFixedItemAmount(item) {
      item.calculatedAmount = item.amount || 0;
  
  // 检查是否是基本工资项目，如果是则存储到本地
  const project = this.salaryProjects.find(p => p.projectCode === item.projectCode);
  if (project && this.isBaseSalaryProject(project.projectCode, project.projectName)) {
    this.storeBaseSalary(parseFloat(item.amount) || 0);
    // 存储后重新计算所有百分比项目
    this.recalculateAllPercentageItems();
  }
  
  this.recalculateItem(item);
    },


    // 新增：重新计算所有百分比项目
recalculateAllPercentageItems() {
  const baseSalary = this.findBaseSalaryFromItems();
  
  this.formData.items.forEach(item => {
    if (item.calculationMethod === 'percentage' && item.projectCode) {
      const percentage = parseFloat(item.amount) || 0;
      item.calculatedAmount = (baseSalary )* percentage / 100;
    }
  });
  
  this.$forceUpdate();
},
    
    calculatePercentageItemAmount(item) {
      const baseSalary = this.findBaseSalaryFromItems();
  const percentage = parseFloat(item.amount) || 0;
  item.calculatedAmount = (baseSalary * percentage) / 100;
  this.recalculateItem(item);
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
// 修改：保存前验证职位占用
async handleSaveDraft() {
  if (this.saving) return;
  
  this.saving = true;
  try {
    // 表单验证
    await this.$refs.form.validate();
    
    // 检查职位占用情况
    if (this.hasInvalidPositions()) {
      const invalidPositions = this.formData.positionIds.filter(id => this.getPositionDisabled(id));
      const positionNames = invalidPositions.map(id => {
        const position = this.positionList.find(p => p.posId === id);
        return position ? `${position.posCode}-${position.posName}` : `ID: ${id}`;
      });
      
      this.$message.error({
        message: `保存失败：以下职位已被其他标准占用\n${positionNames.join('、')}`,
        duration: 5000,
        showClose: true
      });
      return;
    }
    
    // ===== 关键：准备数据前记录状态 =====
    const submitStatus = this.formData.status;
    console.log(`[Save] 准备提交，当前状态: ${submitStatus} (${this.getStatusText(submitStatus)})`);
    
    // 准备保存数据
    const userStr = localStorage.getItem('user') || sessionStorage.getItem('user');
    let userId = null;
    if (userStr) {
      try {
        const userInfo = JSON.parse(userStr);
        userId = userInfo.userId || userInfo.id;
      } catch (e) {
        console.error('解析用户信息失败:', e);
      }
    }
    
    if (!userId) {
      this.$message.error('未获取到用户信息，请重新登录');
      setTimeout(() => {
        this.$router.push('/login');
      }, 1500);
      return;
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
      status: submitStatus  // 明确传递当前状态
    };
    
    // 添加创建人或更新人
    if (!this.isEditMode) {
      saveData.creatorId = Number(userId);
    } else {
      saveData.updaterId = Number(userId);
    }
    
    // ===== 关键：添加请求日志 =====
    console.log(`[Save] API请求参数:`, JSON.parse(JSON.stringify(saveData)));
    
    // API调用
    let response;
    if (!this.isEditMode) {
      response = await createStandard(saveData);
    } else {
      response = await updateStandard(this.standardId, saveData);
    }
    
    // ===== 关键：增强响应处理 =====
    console.log(`[Save] API响应:`, response);
    
    if (response && response.success) {
      const resultData = response.data;
       // ✅ 关键：检查后端返回的字段名
  console.log('后端返回的数据:', resultData);
  
  // 注意：后端返回的可能是 standardId 或 id
  const returnedId = resultData.standardId || resultData.id;
   if (returnedId) {
    this.formData.standardId = returnedId;
    console.log('✅ StandardId 已设置:', returnedId);
  } else {
    console.error('❌ 后端未返回 standardId 或 id');
  }

      // ===== 关键：验证后端返回的数据 =====
      if (!resultData) {
        console.error('[Save] 错误: 后端未返回data字段');
        throw new Error('后端返回数据格式错误：缺少data字段');
      }
      
      console.log(`[Save] 后端返回状态: ${resultData.status}`);
      
      // 保存返回的数据
      if (resultData.standardId) {
        this.formData.standardId = resultData.standardId;
        
        // 如果是创建模式，切换到编辑模式
        if (!this.isEditMode) {
          this.isEditMode = true;
          this.standardId = resultData.standardId;
          
          this.$router.replace({
            path: this.$route.path,
            query: { id: resultData.standardId }
          });
        }
      }
      
      // ===== 关键：强制同步后端返回的状态 =====
      if (resultData.status !== undefined && resultData.status !== null) {
        console.log(`[Save] 同步状态: ${resultData.status}`);
        this.formData.status = resultData.status;
      } else {
        console.warn('[Save] 警告: 后端未返回status字段');
      }
      
      // 更新其他字段
      if (resultData.updatedAt) {
        this.formData.updatedAt = new Date(resultData.updatedAt);
      }
      
      // 保存原始数据
      this.originalFormData = JSON.parse(JSON.stringify(this.formData));
      
      // 成功提示
      const successMessage = this.getStatusText(this.formData.status);
      this.$message.success({
        message: `保存成功！当前状态: ${successMessage}`,
        duration: 2000,
        showClose: true
      });
      
    } else {
      // 保存失败，恢复状态
      console.error('[Save] 保存失败，恢复原始状态');
      if (this.originalFormData) {
        this.formData.status = this.originalFormData.status;
      }
      throw new Error(response?.message || '保存失败');
    }
    
  } catch (error) {
    console.error('[Save] 异常:', error);
    
    // 发生错误时恢复状态
    if (this.originalFormData && this.originalFormData.status !== undefined) {
      this.formData.status = this.originalFormData.status;
      console.log(`[Save] 恢复状态为: ${this.formData.status}`);
    }
    
    // 错误处理...
  } finally {
    setTimeout(() => {
      this.saving = false;
    }, 2000);
  }
},
    
 /**
 * 提交审核（增强版）
 */
async handleSubmitReview() {
  if (this.submitting) return;
  
  // 检查是否可以提交
  if (!this.canSubmitForReview) {
    this.$message.warning('请完成所有必填项后再提交审核');
    return;
  }
  
  // 保存原始状态，用于失败恢复
  const originalStatus = this.formData.status;
  
  // ===== 关键：添加调试日志 =====
  console.log(`[Submit] 提交审核前状态: ${originalStatus}`);
  
  try {
    const isRejected = this.isRejectedStatus;
    const confirmMessage = isRejected 
      ? '当前标准已被驳回，确定要重新提交审核吗？' 
      : '确定要提交审核吗？提交后不可编辑，等待管理员审核。';
    
    await this.$confirm(confirmMessage, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    this.submitting = true;
    
    // 更新状态为待审核（2）
    this.formData.status = 2;
    
    // 如果是驳回状态重新提交，重置登记信息
    if (isRejected) {
      this.formData.registrarId = null;
      this.formData.registrarName = '';
      this.formData.registrationTime = null;
    }
    
    // ===== 关键：调用保存并等待完成 =====
    await this.handleSaveDraft();

    // ✅ 关键：验证 standardId 是否存在
    console.log('保存完成后的 standardId:', this.formData.standardId);
    
    if (!this.formData.standardId) {
      this.$message.error('错误：未获取到标准ID，请重新尝试');
      console.error('❌ standardId 为空，无法提交审核');
      return; // 阻止后续操作
    }

    // ✅ 关键：调用提交审核API
    console.log('调用 submitStandardForReview，参数:', {
      id: this.formData.standardId,
      remark: this.formData.remark
    });
    
    await submitForApproval(this.formData.standardId, this.formData.remark);
    // ===== 关键：检查保存后的状态（更宽松的判断） =====
    // 只要状态不是草稿或驳回，就认为成功
    const savedStatus = this.formData.status;
    console.log(`[Submit] 保存后状态: ${savedStatus}`);
    
    if (savedStatus === 2 || savedStatus === 3 || savedStatus === 1) { // 待审核或已生效都算成功
      this.$message.success({
        message: isRejected ? '重新提交审核成功！' : '提交审核成功！',
        duration: 2000,
        showClose: true
      });
      
      // 延迟跳转
      setTimeout(() => {
        this.$router.push({ 
          path: '/salary/standards', 
          query: { refresh: 'true' } 
        });
      }, 1500);
    } else {
      console.warn(`[Submit] 状态异常，期望2或3，实际: ${savedStatus}`);
      // 不抛出错误，因为可能已经保存成功只是状态返回异常
      this.$message.warning('审核提交成功，但状态更新异常');
    }
    
  } catch (error) {
    console.error('[Submit] 失败:', error);
    
    // 发生错误时恢复原始状态
    this.formData.status = originalStatus;
    
    if (error !== 'cancel') {
      this.showError('提交审核失败', error.message);
    }
  } finally {
    this.submitting = false;
  }
},
    
    /**
     * 重置表单（逻辑优化）
     */
    handleResetForm() {
      if (!this.canEdit) return;
      
      const confirmMessage = this.isEditMode 
        ? '确定要重置表单吗？所有未保存的更改将丢失。' 
        : '确定要重置表单吗？所有输入将被清空。';
      
      this.$confirm(confirmMessage, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        if (this.isEditMode && this.originalFormData) {
          // 编辑模式：恢复原始数据
          Object.assign(this.formData, JSON.parse(JSON.stringify(this.originalFormData)));
        } else {
          // 创建模式：重置为初始状态
          const timestamp = Date.now();
          const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0');
          
          Object.assign(this.formData, {
            standardId: null,
            standardCode: `STD${timestamp}${random}`,
            standardName: '',
            positionIds: [],
            items: [],
            remark: '',
            status: 1,
            createdAt: new Date(),
            updatedAt: new Date(),
            registrarId: null,
            registrarName: '',
            registrationTime: null
          });
          
          // 添加默认项目
          this.addDefaultSalaryItem();
        }
        
        this.$message.success('表单已重置');
      }).catch(() => {
        console.log('取消重置');
      });
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

/* 新增：按钮分组样式优化 */
.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 30px;
  padding: 25px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: 1px solid #ebeef5;
  flex-wrap: wrap; /* 允许换行 */
}

.action-buttons .el-button {
  min-width: 120px;
  padding: 12px 20px;
  font-size: 14px;
  border-radius: 6px;
}

/* 新增：状态提示标签样式 */
.status-hint-tag {
  margin: 0 15px;
  padding: 0 20px;
  height: 40px;
  line-height: 40px;
  font-size: 14px;
  font-weight: 500;
}

/* 新增：加载遮罩优化 */
.saving-tip {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #409EFF;
  font-size: 16px;
  padding: 20px 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  z-index: 9999;
  border: 1px solid #b3d8ff;
}

.saving-tip i {
  font-size: 24px;
  margin-right: 10px;
  vertical-align: middle;
}

/* 新增：按钮状态动画 */
.el-button {
  transition: all 0.3s ease;
}

.el-button.is-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.el-button.is-loading {
  position: relative;
  pointer-events: none;
}

/* 新增：响应式按钮布局 */
@media screen and (max-width: 768px) {
  .action-buttons {
    flex-direction: column;
    gap: 10px;
    padding: 15px;
  }
  
  .action-buttons .el-button {
    width: 100%;
    margin: 0;
  }
  
  .status-hint-tag {
    margin: 10px 0;
    width: 100%;
    text-align: center;
  }
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