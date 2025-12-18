<template>
  <div class="salary-project-create">
    <div class="page-header">
      <h2>{{ pageTitle }}</h2>
      <div class="header-actions">
        <el-button icon="el-icon-back" @click="handleBack">
          返回列表
        </el-button>
      </div>
    </div>
    
    <div class="create-form">
      <el-card>
        <div slot="header" class="card-header">
          <span>项目基本信息</span>
        </div>
        
        <el-form
          ref="projectForm"
          :model="projectForm"
          :rules="rules"
          label-width="120px"
          label-position="left"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="项目编码" prop="projectCode">
                <el-input
                  v-model="projectForm.projectCode"
                  placeholder="请输入项目编码"
                  @blur="handleCodeBlur"
                  :disabled="isEditMode"
                >
                  <template slot="prepend">
                    <el-select
                      v-model="predefinedCode"
                      placeholder="预定义"
                      style="width: 120px"
                      @change="handlePredefinedCodeChange"
                      :disabled="isEditMode"
                    >
                      <el-option
                        v-for="code in predefinedCodes"
                        :key="code.code"
                        :label="code.name"
                        :value="code.code"
                      />
                    </el-select>
                  </template>
                </el-input>
                <div class="form-tip">
                  编码必须唯一，建议使用大写字母和下划线组合，如：BASE_SALARY
                </div>
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="项目名称" prop="projectName">
                <el-input
                  v-model="projectForm.projectName"
                  placeholder="请输入项目名称"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="项目类型" prop="projectType">
                <el-select
                  v-model="projectForm.projectType"
                  placeholder="请选择项目类型"
                  style="width: 100%"
                >
                  <el-option
                    v-for="type in projectTypes"
                    :key="type.code"
                    :label="type.name"
                    :value="type.code"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            
            <el-col :span="8">
              <el-form-item label="项目类别" prop="category">
                <el-select
                  v-model="projectForm.category"
                  placeholder="请选择项目类别"
                  style="width: 100%"
                >
                  <el-option
                    v-for="category in projectCategories"
                    :key="category.name"
                    :label="category.name"
                    :value="category.name"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            
            <el-col :span="8">
              <el-form-item label="计算方法" prop="calculationMethod">
                <el-select
                  v-model="projectForm.calculationMethod"
                  placeholder="请选择计算方法"
                  style="width: 100%"
                >
                  <el-option
                    v-for="method in calculationMethods"
                    :key="method.name"
                    :label="method.name"
                    :value="method.name"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="排序顺序" prop="sortOrder">
            <el-input-number
              v-model="projectForm.sortOrder"
              :min="0"
              :max="999"
              controls-position="right"
              style="width: 200px"
            />
            <div class="form-tip">
              数字越小排序越靠前，默认为0
            </div>
          </el-form-item>
          
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="projectForm.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
            <div class="form-tip">
              禁用状态下项目将不会出现在可选项列表中
            </div>
          </el-form-item>
          
          <el-form-item label="项目描述" prop="description">
            <el-input
              v-model="projectForm.description"
              type="textarea"
              :rows="4"
              placeholder="请输入项目描述"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="计算参数配置">
            <div class="calculation-params">
              <!-- 根据计算方法显示不同的参数配置 -->
              <div v-if="projectForm.calculationMethod === '固定值'">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="固定金额" prop="fixedAmount">
                      <el-input
                        v-model="projectForm.params.fixedAmount"
                        placeholder="请输入固定金额"
                        type="number"
                        min="0"
                        step="0.01"
                      >
                        <template slot="append">元</template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
              
              <div v-if="projectForm.calculationMethod === '百分比'">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="百分比" prop="percentage">
                      <el-input
                        v-model="projectForm.params.percentage"
                        placeholder="请输入百分比"
                        type="number"
                        min="0"
                        max="100"
                        step="0.01"
                      >
                        <template slot="append">%</template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="计算基数" prop="baseType">
                      <el-select
                        v-model="projectForm.params.baseType"
                        placeholder="请选择计算基数"
                        style="width: 100%"
                      >
                        <el-option label="实际基本工资" value="actualBaseSalary" />
                        <el-option label="标准基本工资" value="standardBaseSalary" />
                        <el-option label="其他项目合计" value="otherTotal" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
              
              <div v-if="projectForm.calculationMethod === '公式计算'">
                <el-form-item label="计算公式" prop="formula">
                  <el-input
                    v-model="projectForm.params.formula"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入计算公式，如：${baseSalary} * 0.1"
                  />
                  <div class="form-tip">
                    可用变量：${baseSalary}（基本工资）、${workDays}（实际工作天数）、${totalDays}（总工作天数）、${attendanceDays}（出勤天数）
                  </div>
                </el-form-item>
              </div>
            </div>
          </el-form-item>
        </el-form>
        
        <div class="form-actions">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEditMode ? '更新' : '创建' }}
          </el-button>
        </div>

      </el-card>
    </div>
  </div>
</template>

<script>
import { createProject, updateProject, getProjectEnums, checkProjectCode, getProjectById } from '@/api/salary/projects'

export default {
  name: 'SalaryProjectCreate',
  data() {
    return {
      // 是否为编辑模式
      isEditMode: false,
      currentProjectId: null,
      pageTitle: '创建新薪酬项目',
      
      // 表单数据
      projectForm: {
        projectCode: '',
        projectName: '',
        projectType: 1,
        category: '',
        calculationMethod: '',
        sortOrder: 0,
        status: 1,
        description: '',
        params: {
          fixedAmount: '',
          percentage: '',
          baseType: 'actualBaseSalary',
          formula: ''
        }
      },
      
      // 预定义编码
      predefinedCode: '',
      predefinedCodes: [],
      
      // 枚举数据
      projectTypes: [],
      projectCategories: [],
      calculationMethods: [],
      
      // 表单验证
      submitting: false,
      rules: {
        projectCode: [
          { required: true, message: '请输入项目编码', trigger: 'blur' },
          { pattern: /^[A-Z0-9_]+$/, message: '编码只能包含大写字母、数字和下划线', trigger: 'blur' },
          { validator: this.validateProjectCode, trigger: 'blur' }
        ],
        projectName: [
          { required: true, message: '请输入项目名称', trigger: 'blur' },
          { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
        ],
        projectType: [
          { required: true, message: '请选择项目类型', trigger: 'change' }
        ],
        category: [
          { required: true, message: '请选择项目类别', trigger: 'change' }
        ],
        calculationMethod: [
          { required: true, message: '请选择计算方法', trigger: 'change' }
        ],
        sortOrder: [
          { type: 'number', min: 0, max: 999, message: '排序顺序必须在0-999之间', trigger: 'blur' }
        ]
      }
    }
  },
  
  created() {
    this.fetchEnums()
    this.checkMode()
  },
  
  methods: {
    // 检查是创建模式还是编辑模式
    checkMode() {
      const projectId = this.$route.query.id
      if (projectId) {
        this.isEditMode = true
        this.currentProjectId = projectId
        this.pageTitle = '编辑薪酬项目'
        this.loadProjectData(projectId)
      }
    },
    
    // 加载项目数据（编辑模式）
    async loadProjectData(projectId) {
      try {
        const response = await getProjectById(projectId)
        if (response.success && response.data) {
          const project = response.data
          this.projectForm = {
            projectCode: project.projectCode,
            projectName: project.projectName,
            projectType: project.projectType,
            category: project.category,
            calculationMethod: project.calculationMethod,
            sortOrder: project.sortOrder || 0,
            status: project.status || 1,
            description: project.description || '',
            params: {}
          }
          
          // 解析params参数（JSON字符串转对象）
          if (project.params) {
            try {
              this.projectForm.params = JSON.parse(project.params)
            } catch (e) {
              console.warn('解析params参数失败:', e)
              this.projectForm.params = {
                fixedAmount: '',
                percentage: '',
                baseType: 'actualBaseSalary',
                formula: ''
              }
            }
          }
          
          // 设置预定义编码
          if (this.predefinedCodes.length > 0) {
            const predefinedCode = this.predefinedCodes.find(code => code.code === project.projectCode)
            if (predefinedCode) {
              this.predefinedCode = project.projectCode
            }
          }
        }
      } catch (error) {
        console.error('加载项目数据失败:', error)
        this.$message.error(error.response?.data?.message || '加载项目数据失败')
      }
    },
    
    // 获取枚举数据
    async fetchEnums() {
      try {
        const response = await getProjectEnums()
        if (response.success) {
          this.projectTypes = response.data.projectTypes || []
          this.projectCategories = response.data.projectCategories || []
          this.calculationMethods = response.data.calculationMethods || []
          this.predefinedCodes = response.data.predefinedProjectCodes || []
        }
      } catch (error) {
        console.error('获取枚举数据失败:', error)
        this.$message.error('获取枚举数据失败')
      }
    },
    
    // 处理预定义编码选择
    handlePredefinedCodeChange(code) {
      if (code && !this.isEditMode) {
        const selectedCode = this.predefinedCodes.find(item => item.code === code)
        if (selectedCode) {
          this.projectForm.projectCode = selectedCode.code
          this.projectForm.projectName = selectedCode.name
          this.projectForm.category = selectedCode.category
          this.projectForm.calculationMethod = selectedCode.calculationMethod
        }
      }
    },
    
    // 处理编码失去焦点
    async handleCodeBlur() {
      if (this.projectForm.projectCode && !this.isEditMode) {
        await this.validateCodeUnique()
      }
    },
    
    // 验证项目编码（表单验证器）
    async validateProjectCode(rule, value, callback) {
      if (!value) {
        callback(new Error('请输入项目编码'))
        return
      }
      
      if (!/^[A-Z_]+$/.test(value)) {
        callback(new Error('编码只能包含大写字母和下划线'))
        return
      }
      
      // 检查编码唯一性
      const isUnique = await this.validateCodeUnique()
      if (!isUnique) {
        callback(new Error('项目编码已存在'))
      } else {
        callback()
      }
    },
    
    // 验证编码唯一性
    async validateCodeUnique() {
      if (!this.projectForm.projectCode) return false
      
      try {
        const excludeId = this.isEditMode ? this.currentProjectId : null
        const response = await checkProjectCode(this.projectForm.projectCode, excludeId)
        return !response.data.exists
      } catch (error) {
        console.error('检查项目编码失败:', error)
        return false
      }
    },
    
    // 构建提交数据
    buildSubmitData() {
      const formData = { ...this.projectForm }
      
      // 处理params参数
      if (formData.params) {
        // 清理空值
        const cleanedParams = {}
        Object.keys(formData.params).forEach(key => {
          const value = formData.params[key]
          if (value !== '' && value !== null && value !== undefined) {
            // 如果是数字，确保是数值类型
            if (key === 'fixedAmount' || key === 'percentage') {
              cleanedParams[key] = Number(value)
            } else {
              cleanedParams[key] = value
            }
          }
        })
        
        // 如果有参数，转换为JSON字符串
        if (Object.keys(cleanedParams).length > 0) {
          formData.params = JSON.stringify(cleanedParams)
        } else {
          formData.params = null
        }
      }
      
      // 确保数字字段为数值类型
      formData.sortOrder = Number(formData.sortOrder) || 0
      formData.status = Number(formData.status) || 1
      formData.projectType = Number(formData.projectType) || 1
      
      return formData
    },

    // 处理提交
    async handleSubmit() {
      try {
        // 验证表单
        const valid = await this.$refs.projectForm.validate()
        if (!valid) return
        
        // 验证编码唯一性（编辑模式下也要验证）
        const isCodeValid = await this.validateCodeUnique()
        if (!isCodeValid) {
          this.$message.error('项目编码已存在，请修改编码')
          return
        }
        
        // 添加确认提示
        const action = this.isEditMode ? '更新' : '创建'
        await this.$confirm(`确定要${action}这个薪酬项目吗？`, `确认${action}`, {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        this.submitting = true
        
        // 构建提交数据
        const submitData = this.buildSubmitData()
        console.log('提交的数据:', submitData)
        
        let response
        if (this.isEditMode) {
          response = await updateProject(this.currentProjectId, submitData)
        } else {
          // 使用更新后的路径：/api/salary/projects/create
          response = await createProject(submitData)
        }
        
        if (response.success) {
          this.$message.success(`${action}成功`)
          this.$router.push('/salary/projects')
        } else {
          this.$message.error(response.message || `${action}失败`)
        }
      } catch (error) {
        console.error('操作失败:', error)
        if (error !== 'cancel') {
          const errorMsg = error.response?.data?.message || error.message || '操作失败，请检查网络连接'
          this.$message.error(errorMsg)
        }
      } finally {
        this.submitting = false
      }
    },

    // 处理重置
    handleReset() {
      this.$refs.projectForm.resetFields()
      if (this.isEditMode) {
        // 编辑模式：重置为当前项目的数据
        this.loadProjectData(this.currentProjectId)
      } else {
        // 创建模式：清空所有数据
        this.projectForm = {
          projectCode: '',
          projectName: '',
          projectType: 1,
          category: '',
          calculationMethod: '',
          sortOrder: 0,
          status: 1,
          description: '',
          params: {
            fixedAmount: '',
            percentage: '',
            baseType: 'actualBaseSalary',
            formula: ''
          }
        }
        this.predefinedCode = ''
      }
    },
    
    // 处理返回
    handleBack() {
      this.$router.push('/salary/projects')
    },

 
  }
}
</script>

<style scoped>
.salary-project-create {
  padding: 20px;
  background-color: #fff;
  min-height: 100vh;
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

.create-form {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.calculation-params {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}
</style>