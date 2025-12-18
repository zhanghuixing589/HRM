<template>
  <div>
    <el-card shadow="never">
      <div slot="header">
        <span>{{ pageTitle }}</span>
        <div style="float: right;">
          <el-button v-if="isResubmitMode" type="text" icon="el-icon-close" @click="cancelResubmit"
            style="margin-left: 10px;">
            取消重新提交
          </el-button>
          <el-button type="text" icon="el-icon-document-checked" @click="$router.push('/hr/archive/my-archives')">
            查看我的提交
          </el-button>
        </div>
      </div>

      <!-- 如果是重新提交模式，显示原档案信息 -->
      <div v-if="isResubmitMode && originalArchive" style="margin-bottom: 20px;">
        <el-alert type="info" :title="`重新提交档案：${originalArchive.arcCode} - ${originalArchive.name}`"
          :description="originalArchive.reason || '请根据要求修改后重新提交'" show-icon :closable="false" />
      </div>

      <el-form ref="form" :model="dto" :rules="rules" label-width="110px" v-loading="loading">
        <!-- 如果是重新提交模式，显示档案编号 -->
        <el-form-item v-if="isResubmitMode" label="档案编号">
          <el-input v-model="originalArchive.arcCode" readonly disabled />
        </el-form-item>

        <!-- 机构三级联动 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="一级机构" prop="firstOrgId">
              <el-select v-model="dto.firstOrgId" placeholder="请选择一级机构" clearable @change="handleFirstOrgChange">
                <el-option v-for="org in firstList" :key="org.orgId" :label="org.orgName" :value="org.orgId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="二级机构" prop="secondOrgId">
              <el-select v-model="dto.secondOrgId" placeholder="请选择二级机构" clearable @change="handleSecondOrgChange"
                :disabled="!dto.firstOrgId">
                <el-option v-for="org in secondList" :key="org.orgId" :label="org.orgName" :value="org.orgId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="三级机构" prop="thirdOrgId">
              <el-select v-model="dto.thirdOrgId" placeholder="请选择三级机构" clearable @change="handleThirdOrgChange"
                :disabled="!dto.secondOrgId">
                <el-option v-for="org in thirdList" :key="org.orgId" :label="org.orgName" :value="org.orgId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 职位和职称 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="职位名称" prop="positionId">
              <el-select v-model="dto.positionId" placeholder="请选择职位" clearable @change="handlePositionChange"
                :disabled="!dto.thirdOrgId">
                <el-option v-for="pos in positionList" :key="pos.posId" :label="pos.posName" :value="pos.posId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="职称" prop="title">
              <el-select v-model="dto.title" placeholder="请选择职称" clearable @change="handleTitleChange">
                <el-option label="初级" value="初级" />
                <el-option label="中级" value="中级" />
                <el-option label="高级" value="高级" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="薪酬标准" prop="salaryStandard">
              <el-select 
                v-model="dto.salaryStandard" 
                placeholder="请选择薪酬标准" 
                clearable
                :disabled="!showSalaryStandards || salaryStandardList.length === 0"
                filterable
                style="width: 100%;"
              >
                <el-option 
                  v-for="standard in salaryStandardList" 
                  :key="standard.standardId" 
                  :label="`${standard.standardName}`" 
                  :value="standard.standardId" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          
        </el-row>

        <!-- 基本信息 -->
        <el-row :gutter="20">
          <el-col :span="5">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="dto.name" maxlength="20" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="性别" prop="sex">
              <el-select v-model="dto.sex" style="width: 100%">
                <el-option label="男" :value="1" />
                <el-option label="女" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="dto.idCard" maxlength="18" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="出生日期" prop="birDate">
              <el-date-picker v-model="dto.birDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd"
                style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 其他字段 -->
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="民族" prop="nationality">
              <el-input v-model="dto.nationality" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="学历" prop="qualification">
              <el-select v-model="dto.qualification" style="width: 100%">
                <el-option label="小学" value="小学" />
                <el-option label="初中" value="初中" />
                <el-option label="高中" value="高中" />
                <el-option label="专科" value="专科" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="政治面貌" prop="identity">
              <el-input v-model="dto.identity" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="宗教信仰" prop="belief">
              <el-input v-model="dto.belief" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="Email" prop="email">
              <el-input v-model="dto.email" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="手机" prop="phone">
              <el-input v-model="dto.phone" maxlength="11" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="电话" prop="telephone">
              <el-input v-model="dto.telephone" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="国籍" prop="country">
              <el-input v-model="dto.country" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="出生地" prop="birAddress">
              <el-input v-model="dto.birAddress" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="邮编" prop="zipCode">
              <el-input v-model="dto.zipCode" clearable />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="住址" prop="address">
          <el-input v-model="dto.address" clearable />
        </el-form-item>

        <el-form-item label="专业" prop="major">
          <el-input v-model="dto.major" clearable />
        </el-form-item>

        <!-- 照片上传 -->
        <el-form-item label="照片">
          <el-upload class="avatar-uploader" :action="uploadUrl" :show-file-list="false"
            :on-success="handleAvatarSuccess" :before-upload="beforeAvatarUpload" :headers="uploadHeaders">
            <img v-if="dto.photoPath" :src="dto.photoPath" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>

        <!-- 如果是重新提交，显示修改说明 -->
        <el-form-item v-if="isResubmitMode" label="修改说明" prop="changeReason">
          <el-input v-model="dto.changeReason" type="textarea" :rows="3" placeholder="请说明修改的原因和内容" maxlength="200"
            show-word-limit />
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isResubmitMode ? '重新提交' : '提交登记' }}
          </el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button v-if="isResubmitMode" @click="cancelResubmit">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { getOrgTree } from '@/api/org'
import { listByOrg } from '@/api/pos'
import { registerArchive, resubmitArchive, getArchiveDetail } from '@/api/archive'
import { getSalaryStandardsByPosition } from '@/api/archiveSalary'

export default {
  name: 'StaffRegister',
  data() {
    return {
      isResubmitMode: false,
      originalArchive: null,
      pageTitle: '档案登记',
      arcId: null,

      dto: {
        name: '',
        sex: 1,
        idCard: '',
        firstOrgId: '',
        secondOrgId: '',
        thirdOrgId: '',
        positionId: '',
        positionName: '',
        title: '',
        salaryStandard: '',
        birDate: '',
        nationality: '汉族',
        qualification: '本科',
        email: '',
        telephone: '',
        phone: '',
        address: '',
        zipCode: '',
        country: '中国',
        birAddress: '',
        belief: '',
        identity: '',
        major: '',
        photoPath: '',
        changeReason: ''
      },
      rules: {
        firstOrgId: [
          { required: true, message: '请选择一级机构', trigger: 'change' }
        ],
        secondOrgId: [
          { required: true, message: '请选择二级机构', trigger: 'change' }
        ],
        thirdOrgId: [
          { required: true, message: '请选择三级机构', trigger: 'change' }
        ],
        positionId: [
          { required: true, message: '请选择职位', trigger: 'change' }
        ],
        title: [
          { required: true, message: '请选择职称', trigger: 'change' }
        ],
        salaryStandard: [
          { required: true, message: '请选择薪酬标准', trigger: 'change' }
        ],
        name: [
          { required: true, message: '请输入姓名', trigger: 'blur' }
        ],
        sex: [
          { required: true, message: '请选择性别', trigger: 'change' }
        ],
        idCard: [
          { required: true, message: '请输入身份证号', trigger: 'blur' },
          { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
        ],
        birDate: [
          { required: true, message: '请选择出生日期', trigger: 'change' }
        ],
        nationality: [
          { required: true, message: '请输入民族', trigger: 'blur' }
        ],
        qualification: [
          { required: true, message: '请选择学历', trigger: 'change' }
        ]
      },
      loading: false,
      submitting: false,
      showSalaryStandards: false,
      firstList: [],
      secondList: [],
      thirdList: [],
      positionList: [],
      salaryStandardList: [],
      uploadUrl: 'http://localhost:8080/common/upload',
      uploadHeaders: {
        Authorization: localStorage.getItem('token') || ''
      }
    }
  },
  created() {
    this.initPageMode()
  },
  methods: {
    // 初始化页面模式（新登记还是重新提交）
    initPageMode() {
      const mode = this.$route.query.mode
      const arcId = this.$route.query.arcId || this.$route.params.id

      if (mode === 'resubmit' && arcId) {
        this.isResubmitMode = true
        this.pageTitle = '档案重新提交'
        this.arcId = arcId
        this.loadOriginalArchive()
      } else {
        this.isResubmitMode = false
        this.pageTitle = '档案登记'
        this.loadFirstLevelOrgs()
      }
    },

    // 加载原档案数据
    async loadOriginalArchive() {
      try {
        this.loading = true

        // 获取档案详情
        const response = await getArchiveDetail(this.arcId)

        if (response && response.code === 200) {
          this.originalArchive = response.data

          // 检查档案状态是否为已驳回
          if (this.originalArchive.status !== 3) {
            this.$message.warning('该档案不是已驳回状态，无法重新提交')
            this.$router.push('/hr/archive/my-archives')
            return
          }

          // 初始化表单数据
          this.initFormData()

          // 加载机构信息
          await this.loadOrgInfo()
        } else {
          this.$message.error(response ? response.message : '加载档案失败')
          this.$router.push('/hr/archive/my-archives')
        }
      } catch (error) {
        console.error('加载档案失败:', error)
        this.$message.error('加载档案失败')
        this.$router.push('/hr/archive/my-archives')
      } finally {
        this.loading = false
      }
    },

    // 初始化表单数据
    initFormData() {
      if (!this.originalArchive) return

      const archive = this.originalArchive

      this.dto = {
        name: archive.name || '',
        sex: archive.sex || 1,
        idCard: archive.idCard || '',
        firstOrgId: archive.firstOrgId || '',
        secondOrgId: archive.secondOrgId || '',
        thirdOrgId: archive.thirdOrgId || '',
        positionId: '', 
        positionName: archive.positionName || '',
        title: archive.title || '',
        salaryStandard: archive.salaryStandard || '',
        birDate: archive.birDate || '',
        nationality: archive.nationality || '汉族',
        qualification: archive.qualification || '本科',
        email: archive.email || '',
        telephone: archive.telephone || '',
        phone: archive.phone || '',
        address: archive.address || '',
        zipCode: archive.zipCode || '',
        country: archive.country || '中国',
        birAddress: archive.birAddress || '',
        belief: archive.belief || '',
        identity: archive.identity || '',
        major: archive.major || '',
        photoPath: archive.photoPath || '',
        changeReason: ''
      }

      // 如果是重新提交模式，并且有职称，加载薪酬标准
      if (this.isResubmitMode && this.dto.positionId && this.dto.title) {
        this.loadSalaryStandards(this.dto.positionId, this.dto.title)
      }
    },

    // 加载机构信息
    async loadOrgInfo() {
      try {
        // 加载一级机构
        const firstResponse = await getOrgTree()
        if (firstResponse && firstResponse.code === 200) {
          this.firstList = Array.isArray(firstResponse.data) ? firstResponse.data : []

          // 找到对应的一级机构
          if (this.dto.firstOrgId) {
            const firstOrg = this.firstList.find(org => org.orgId === this.dto.firstOrgId)
            if (firstOrg && firstOrg.children) {
              this.secondList = firstOrg.children
            }
          }

          // 找到对应的二级机构
          if (this.dto.secondOrgId) {
            const secondOrg = this.secondList.find(org => org.orgId === this.dto.secondOrgId)
            if (secondOrg && secondOrg.children) {
              this.thirdList = secondOrg.children
            }
          }

          // 加载三级机构的职位
          if (this.dto.thirdOrgId) {
            await this.loadPositions()
          }
        }
      } catch (error) {
        console.error('加载机构信息失败:', error)
      }
    },

    // 加载一级机构
    async loadFirstLevelOrgs() {
      try {
        this.loading = true
        const response = await getOrgTree()

        // 检查响应是否成功
        if (response && response.code === 200) {
          const data = response.data
          console.log('机构树数据:', data)
          this.firstList = Array.isArray(data) ? data : []
        } else {
          this.firstList = []
          const errorMessage = response ? response.message : '获取机构数据失败'
          this.$message.error(errorMessage)
          console.error('机构接口返回错误:', response)
        }
      } catch (error) {
        console.error('加载一级机构失败:', error)
        this.firstList = []
        this.$message.error('加载机构数据失败，请检查网络连接')
      } finally {
        this.loading = false
      }
    },

    // 一级机构变化
    handleFirstOrgChange(firstOrgId) {
      this.dto.secondOrgId = ''
      this.dto.thirdOrgId = ''
      this.dto.positionId = ''
      this.dto.positionName = ''
      this.dto.title = ''
      this.dto.salaryStandard = ''
      this.secondList = []
      this.thirdList = []
      this.positionList = []
      this.showSalaryStandards = false
      this.salaryStandardList = []

      if (!firstOrgId) return

      const firstOrg = this.firstList.find(org => org && org.orgId === firstOrgId)
      if (firstOrg && firstOrg.children) {
        this.secondList = firstOrg.children
      }
    },

    // 二级机构变化
    handleSecondOrgChange(secondOrgId) {
      this.dto.thirdOrgId = ''
      this.dto.positionId = ''
      this.dto.positionName = ''
      this.dto.title = ''
      this.dto.salaryStandard = ''
      this.thirdList = []
      this.positionList = []
      this.showSalaryStandards = false
      this.salaryStandardList = []

      if (!secondOrgId) return

      const secondOrg = this.secondList.find(org => org && org.orgId === secondOrgId)
      if (secondOrg && secondOrg.children) {
        this.thirdList = secondOrg.children
      }
    },

    // 三级机构变化
    async handleThirdOrgChange(thirdOrgId) {
      this.dto.positionId = ''
      this.dto.positionName = ''
      this.dto.title = ''
      this.dto.salaryStandard = ''
      this.positionList = []
      this.showSalaryStandards = false
      this.salaryStandardList = []

      if (!thirdOrgId) return

      await this.loadPositions()
    },

    async loadPositions() {
      try {
        const response = await listByOrg(this.dto.thirdOrgId)
        if (response && response.code === 200) {
          this.positionList = response.data
          console.log('职位列表:', this.positionList)
          
          // 如果是重新提交模式，找到对应的职位ID
          if (this.isResubmitMode && this.dto.positionName) {
            const position = this.positionList.find(p => p && p.posName === this.dto.positionName)
            if (position) {
              this.dto.positionId = position.posId
              this.showSalaryStandards = true
              await this.loadSalaryStandards(this.dto.positionId, '')
              
              // 如果已经有职称，加载薪酬标准
              if (this.dto.title) {
                await this.loadSalaryStandards(this.dto.positionId, this.dto.title)
              }
            }
          }
        } else {
          this.positionList = []
          const errorMessage = response ? response.message : '获取职位数据失败'
          this.$message.error(errorMessage)
        }
      } catch (error) {
        console.error('加载职位失败:', error)
        this.positionList = []
        this.$message.error('加载职位失败')
      }
    },

    // 职位变化
    async handlePositionChange(positionId) {
      this.dto.positionName = ''
      this.dto.title = ''
      this.dto.salaryStandard = ''
      this.showSalaryStandards = false
      this.salaryStandardList = []

      if (!positionId) return

      const selectedPosition = this.positionList.find(p => p && p.posId === positionId)
      if (selectedPosition) {
        this.dto.positionName = selectedPosition.posName

        // 职位选择后，立即加载与该职位关联的所有薪酬标准（即使职称为空）
        this.showSalaryStandards = true
        await this.loadSalaryStandards(positionId, '') // 传递空职称
      }
      this.loadSalaryStandards(positionId, null)

      // 如果已经有职称，立即加载薪酬标准
      if (this.dto.title) {
        this.loadSalaryStandards(positionId, this.dto.title)
      }
    },

    // 职称变化
    async handleTitleChange() {
      this.dto.salaryStandard = ''

      // 通过职位和职称显示薪酬标准
      if (this.dto.positionId && this.dto.title) {
        this.showSalaryStandards = true
        await this.loadSalaryStandards(this.dto.positionId, this.dto.title)
      } else {
        this.showSalaryStandards = false
        this.salaryStandardList = []
      }
    },

    // 加载薪酬标准（从后端API获取）
    async loadSalaryStandards(positionId, title) {
      try {
        // 显示加载状态
        this.$set(this, 'salaryStandardLoading', true)
        
        const response = await getSalaryStandardsByPosition(positionId, title)
        
        if (response && response.code === 200) {
          this.salaryStandardList = response.data || []
          
          if (this.salaryStandardList.length === 0) {
            this.$message.warning('未找到符合条件的薪酬标准')
          }
        } else {
          this.salaryStandardList = []
          const errorMsg = response ? response.message : '获取薪酬标准失败'
          this.$message.error(errorMsg)
        }
      } catch (error) {
        console.error('加载薪酬标准失败:', error)
        this.salaryStandardList = []
        this.$message.error('加载薪酬标准失败')
      } finally {
        this.$set(this, 'salaryStandardLoading', false)
      }
    },

    // 头像上传成功
    handleAvatarSuccess(res) {
      if (res && res.code === 200) {
        this.dto.photoPath = res.data || res.url
        this.$message.success('头像上传成功')
      } else {
        this.$message.error(res ? res.message : '头像上传失败')
      }
    },

    // 上传前检查
    beforeAvatarUpload(file) {
      const isImage = file.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        this.$message.error('只能上传图片文件!')
        return false
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过 2MB!')
        return false
      }

      return isImage && isLt2M
    },

    // 提交表单
    async handleSubmit() {
      try {
        await this.$refs.form.validate()

        // 检查是否选择了薪酬标准
        if (!this.dto.salaryStandard) {
          this.$message.error('请选择薪酬标准')
          return
        }

        // 检查身份证号格式
        const idCardRegex = /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/
        if (!idCardRegex.test(this.dto.idCard)) {
          this.$message.error('请输入正确的身份证号码')
          return
        }

        this.submitting = true

        let response

        if (this.isResubmitMode && this.arcId) {
          // 重新提交
          response = await resubmitArchive(this.arcId, this.dto)
        } else {
          // 新登记
          response = await registerArchive(this.dto)
        }

        if (response && response.code === 200) {
          const message = this.isResubmitMode ? '档案重新提交成功，等待复核' : '档案登记成功，等待复核'
          this.$message.success(response.message || message)
          this.handleReset()
          this.$router.push('/hr/archive/my-archives')
        } else {
          this.$message.error(response ? response.message : '提交失败')
        }
      } catch (error) {
        console.error('提交失败:', error)
        if (error.response) {
          this.$message.error(error.response.data?.message || '提交失败')
        } else if (error.message) {
          this.$message.error(error.message)
        } else {
          this.$message.error('提交失败，请稍后重试')
        }
      } finally {
        this.submitting = false
      }
    },

    // 重置表单
    handleReset() {
      if (this.isResubmitMode) {
        // 如果是重新提交模式，重置为原档案数据
        this.initFormData()
      } else {
        // 如果是新登记模式，清空表单
        this.$refs.form.resetFields()
        this.secondList = []
        this.thirdList = []
        this.positionList = []
        this.showSalaryStandards = false
        this.salaryStandardList = []
        this.dto.photoPath = ''
        this.dto.changeReason = ''

        // 重新加载一级机构
        this.loadFirstLevelOrgs()
      }
    },

    // 取消重新提交
    cancelResubmit() {
      this.$confirm('确定取消重新提交吗？', '提示', {
        type: 'warning'
      }).then(() => {
        this.$router.push('/hr/archive/my-archives')
      })
    }
  }
}
</script>

<style scoped lang="scss">
.avatar-uploader ::v-deep .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 150px;
  line-height: 150px;
  text-align: center;
}

.avatar-uploader ::v-deep .el-upload:hover {
  border-color: #409EFF;
}

.avatar {
  width: 120px;
  height: 150px;
  display: block;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

/* 调整表单布局 */
.el-form-item {
  margin-bottom: 18px;
}

.el-row {
  margin-bottom: 0;
}
</style>