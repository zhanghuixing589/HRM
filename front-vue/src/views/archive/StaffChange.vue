<template>
  <div>
    <el-card shadow="never">
      <div slot="header">
        <span>档案变更申请</span>
        <el-button type="text" icon="el-icon-back" @click="$router.back()" style="float:right;">返回</el-button>
      </div>

      <div v-if="loading">
        <el-skeleton :rows="10" animated />
      </div>

      <div v-else-if="!archiveData">
        <el-empty description="未找到档案数据">
          <el-button type="primary" @click="$router.push('/hr/archive/my-archives')">返回我的提交记录</el-button>
        </el-empty>
      </div>

      <el-form v-else ref="form" :model="dto" :rules="rules" label-width="110px" v-loading="submitting">
        <!-- 不可修改字段（只读） -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="档案编号">
              <el-input v-model="archiveData.arcCode" readonly disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="一级机构">
              <el-input v-model="archiveData.firstOrgName" readonly disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="二级机构">
              <el-input v-model="archiveData.secondOrgName" readonly disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="三级机构">
              <el-input v-model="archiveData.thirdOrgName" readonly disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="职位">
              <el-input v-model="archiveData.positionName" readonly disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 可修改字段 -->
        <h3 style="margin: 20px 0 15px 0; color: #409EFF; border-left: 4px solid #409EFF; padding-left: 10px;">
          可修改信息
        </h3>

        <el-row :gutter="20">
          <el-col :span="8">
            <!-- 姓名 -->
            <el-form-item label="姓名" prop="name">
              <el-input v-model="dto.name" maxlength="100" clearable placeholder="请输入姓名" />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <!-- 性别 -->
            <el-form-item label="性别" prop="sex">
              <el-select v-model="dto.sex" style="width: 100%" placeholder="请选择性别">
                <el-option label="男" :value="1" />
                <el-option label="女" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <!-- 身份证号 -->
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="dto.idCard" maxlength="18" clearable placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="职称" prop="title">
              <el-select v-model="dto.title"
                        placeholder="请选择职称"
                        @change="handleTitleChange">   <!-- 关键事件 -->
                <el-option label="初级" value="初级" />
                <el-option label="中级" value="中级" />
                <el-option label="高级" value="高级" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <el-form-item label="薪酬标准" prop="salaryStandard">
              <el-select v-model="dto.salaryStandard"
                        placeholder="请选择薪酬标准"
                        clearable
                        filterable
                        style="width: 100%;">
                <el-option v-for="item in salaryStandardList"
                          :key="item.standardId"
                          :label="item.standardName" 
                          :value="item.standardId" />   <!-- 提交 ID -->
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <!-- 出生日期 -->
            <el-form-item label="出生日期" prop="birDate">
              <el-date-picker v-model="dto.birDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd"
                style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 学历 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="民族" prop="nationality">
              <el-input v-model="dto.nationality" clearable placeholder="请输入民族" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="学历" prop="qualification">
              <el-select v-model="dto.qualification" style="width: 100%" placeholder="请选择学历">
                <el-option label="小学" value="小学" />
                <el-option label="初中" value="初中" />
                <el-option label="高中" value="高中" />
                <el-option label="专科" value="专科" />
                <el-option label="本科" value="本科" />
                <el-option label="研究生" value="" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="政治面貌" prop="identity">
              <el-input v-model="dto.identity" clearable placeholder="请输入政治面貌" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="宗教信仰" prop="belief">
              <el-input v-model="dto.belief" clearable placeholder="请输入宗教信仰" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="Email" prop="email">
              <el-input v-model="dto.email" clearable placeholder="请输入Email" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="手机" prop="phone">
              <el-input v-model="dto.phone" maxlength="11" clearable placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="电话" prop="telephone">
              <el-input v-model="dto.telephone" clearable placeholder="请输入电话" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="国籍" prop="country">
              <el-input v-model="dto.country" clearable placeholder="请输入国籍" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="出生地" prop="birAddress">
              <el-input v-model="dto.birAddress" clearable placeholder="请输入出生地" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="住址" prop="address">
          <el-input v-model="dto.address" clearable placeholder="请输入住址" />
        </el-form-item>

        <el-form-item label="邮编" prop="zipCode">
          <el-input v-model="dto.zipCode" clearable placeholder="请输入邮编" />
        </el-form-item>

        <el-form-item label="专业" prop="major">
          <el-input v-model="dto.major" clearable placeholder="请输入专业" />
        </el-form-item>

        <!-- 照片上传 -->
        <el-form-item label="照片">
          <el-upload class="avatar-uploader" :action="uploadUrl" :show-file-list="false"
            :on-success="handleAvatarSuccess" :before-upload="beforeAvatarUpload" :headers="uploadHeaders">
            <img v-if="dto.photoPath" :src="dto.photoPath" class="avatar">
            <img v-else-if="archiveData.photoPath" :src="archiveData.photoPath" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>

        <!-- 修改原因 -->
        <el-form-item label="修改说明" prop="changeReason">
          <el-input v-model="dto.changeReason" type="textarea" :rows="3" placeholder="请说明修改的原因和内容" maxlength="200"
            show-word-limit />
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">提交变更申请</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 查看驳回原因 -->
    <el-dialog title="驳回原因" :visible.sync="reasonDialogVisible" width="500px" :close-on-click-modal="false">
      <div v-if="rejectReason">
        <el-alert type="error" :title="`档案 ${archiveData.arcCode} 被驳回`" :description="rejectReason" show-icon
          :closable="false" />
        <div style="margin-top: 15px; color: #999; font-size: 12px;">
          提示：请根据上述驳回原因修改档案信息后重新提交
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="reasonDialogVisible = false" size="medium">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getArchiveDetail } from '@/api/archive'
import { applyArchiveChange, resubmitChange, getChangeDetail } from '@/api/change'
import { getSalaryStandardsByPosition, getAllActiveSalaryStandards } from '@/api/archiveSalary'

export default {
  name: 'StaffChange',
  data() {
    return {
      loading: true,
      submitting: false,
      archiveData: null,
      rejectReason: '',
      reasonDialogVisible: false,
      salaryStandardName: '',
      changeMode: false,  // true表示变更模式，false表示重新提交模式
      changeId: null,

      dto: {
        // 可修改字段
        idCard: '',
        birDate: '',
        nationality: '',
        qualification: '',
        identity: '',
        belief: '',
        email: '',
        phone: '',
        telephone: '',
        address: '',
        zipCode: '',
        country: '',
        birAddress: '',
        major: '',
        photoPath: '',
        changeReason: ''
      },

      rules: {
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
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
        ]
      },

      // 薪酬标准相关
      showSalaryStandards: false,
      salaryStandardList: [],
      salaryStandardMap: {},
      salaryStandardLoading: false,

      uploadUrl: process.env.VUE_APP_BASE_API + '/common/upload',
      uploadHeaders: {
        Authorization: localStorage.getItem('token') || ''
      }
    }
  },
  created() {
    this.loadArchiveData(),
      this.initPageMode()
  },
  methods: {
    // 加载档案数据
    async loadArchiveData() {
      try {
        this.loading = true
        const arcId = this.$route.query.id

        if (!arcId) {
          this.$message.warning('请先选择要变更的档案')
          this.loading = false
          return
        }

        // 先检查是否有待审核的变更申请
        try {
          const checkResponse = await this.$http.get(`/hr/change/check-pending/${arcId}`)
          if (checkResponse.data && checkResponse.data.code === 200) {
            if (checkResponse.data.data && checkResponse.data.data.hasPending === true) {
              this.$message.error(checkResponse.data.data.message || '该档案有待审核的变更申请，不能重复提交')
              if (this.$route.path !== '/hr/archive/my-archives') {
                this.$router.push('/hr/archive/my-archives')
              }
              this.loading = false
              return
            }
          }
        } catch (checkError) {
          console.warn('检查待审核变更申请失败:', checkError)
          // 检查失败不影响继续操作
        }

        const response = await getArchiveDetail(arcId)
        if (response && (response.code === 200 || response.success)) {
          this.archiveData = response.data || response
          console.log(this.archiveData)

          // 检查档案状态是否为已通过（只有已通过的档案才能变更）
          if (this.archiveData.status !== 2) {
            this.$message.warning('只有已通过的档案才能变更')
            if (this.$route.path !== '/hr/archive/my-archives') {
              this.$router.push('/hr/archive/my-archives')
            }
            this.loading = false
            return
          }

          // 初始化表单数据
          this.initFormData()
        } else {
          this.$message.error(response ? response.message : '加载档案失败')
        }
      } catch (error) {
        console.error('加载档案失败:', error)
        this.$message.error('加载档案失败')
      } finally {
        this.loading = false
      }
    },

    // 初始化页面
    initPageMode() {
      const arcId = this.$route.query.id
      const changeId = this.$route.query.changeId

      if (changeId) {
        // 重新提交变更申请
        this.changeMode = false
        this.changeId = changeId
        this.pageTitle = '变更重新提交'
        this.loadChangeData()
      } else if (arcId) {
        // 新变更申请
        this.changeMode = true
        this.pageTitle = '档案变更申请'
        this.loadArchiveData()
      } else {
        this.$message.error('请先选择要变更的档案')
        this.loading = false
      }
    },

    // 初始化表单数据
    async initFormData(changeData) {
      if (!this.archiveData) return

      // 1) 基本字段拷贝（你原来已有）
      this.dto = {
        name: this.archiveData.name || '',
        sex: this.archiveData.sex || 1,
        idCard: this.archiveData.idCard || '',
        title: this.archiveData.title || '',
        salaryStandard: this.archiveData.salaryStandardName || '', // 此时还是 ID
        birDate: this.archiveData.birDate || '',
        nationality: this.archiveData.nationality || '',
        qualification: this.archiveData.qualification || '',
        identity: this.archiveData.identity || '',
        belief: this.archiveData.belief || '',
        email: this.archiveData.email || '',
        phone: this.archiveData.phone || '',
        telephone: this.archiveData.telephone || '',
        address: this.archiveData.address || '',
        zipCode: this.archiveData.zipCode || '',
        country: this.archiveData.country || '',
        birAddress: this.archiveData.birAddress || '',
        major: this.archiveData.major || '',
        photoPath: this.archiveData.photoPath || '',
        changeReason: ''
      }
      this.archiveData.positionId = this.archiveData.positionId || this.archiveData.posId || null

      // 2) 第一次就把下拉框数据源拉回来（关键！）
      await this.loadSalaryStandards()

      // 3) 如果有“变更后数据”(重新提交场景)，再用变更后数据覆盖一次
      if (changeData && changeData.afterDataParsed) {
        const after = changeData.afterDataParsed
        Object.keys(this.dto).forEach(key => {
          if (after[key] !== undefined && after[key] !== null) {
            this.dto[key] = after[key]
          }
        })
        if (after.salaryStandard) {
          // 变更后可能换了标准，再补一次映射
          await this.loadSalaryStandards()
        }
      }
    },

    // 职称变化
    handleTitleChange() {
      this.dto.salaryStandard = ''   // 清空旧选中
      this.loadSalaryStandards()
    },

    async loadSalaryStandards() {
      if (!this.dto.title) {          // 没选职称就清空
        this.salaryStandardList = []
        return
      }

      console.log('【请求参数】positionId:', this.archiveData.positionId,
              'title:', this.dto.title,
        'archiveData:', this.archiveData)
              
      const res = await getSalaryStandardsByPosition(
        this.archiveData.positionId,
        this.dto.title
      )

      console.log('【后端返回】', res)
       
      if (res && res.code === 200) {
        this.salaryStandardList = res.data || []
        // 同步建 id→name 映射，方便下拉框显示
        this.salaryStandardMap = {}
        this.salaryStandardList.forEach(item => {
          this.salaryStandardMap[item.standardId] = item.standardName
        })
      } else {
        this.salaryStandardList = []
      }
    },

    async getPositionIdByName(name) {
      const map = { '总经理': 1, '副总经理': 2, '部门经理': 3, '主管': 4, '专员': 5, '助理': 6 };
      return map[name] || null;
    },
    // 4. 初始化时把“初始薪酬标准 ID → 名称”反解出来
    async initSalaryStandardName() {
      if (!this.dto.salaryStandard) return;          // 没有值直接返回
      // 如果档案里已经带了名称就直接用
      if (this.archiveData.salaryStandardName) {
        this.salaryStandardMap[this.dto.salaryStandard] = this.archiveData.salaryStandardName;
        return;
      }
      // 否则拉一次全量映射
      const res = await getAllActiveSalaryStandards();
      (res?.data || []).forEach(s => {
        this.salaryStandardMap[s.standardId] = s.standardName;
      });
    },

    // 加载变更页面的薪酬标准
    async loadSalaryStandardsForChange() {
      try {
        if (!this.archiveData) return

        const response = await getSalaryStandardsByPosition(
          this.getPositionIdFromName(this.archiveData.positionName),
          this.dto.title
        )

        if (response && response.code === 200) {
          this.salaryStandardList = response.data || []
        } else {
          this.salaryStandardList = []
        }
      } catch (error) {
        console.error('加载薪酬标准失败:', error)
        this.salaryStandardList = []
      }
    },

    // 头像上传成功
    handleAvatarSuccess(res) {
      if (res && (res.code === 200 || res.success)) {
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

    // 提交变更申请
    async handleSubmit() {
      try {
        await this.$refs.form.validate()

        // 验证身份证号格式
        if (this.dto.idCard && !/^\d{17}[\dXx]$/.test(this.dto.idCard)) {
          this.$message.error('请输入正确的身份证号码')
          return
        }

        // 验证手机号格式
        if (this.dto.phone && !/^1[3-9]\d{9}$/.test(this.dto.phone)) {
          this.$message.error('手机号格式不正确')
          return
        }

        this.submitting = true

        // 构建提交数据
        const submitData = {
          ...this.dto,
          arcId: this.archiveData.arcId
        }

        console.log('提交变更申请数据:', submitData)

        let response

        if (this.changeMode) {
          // 新变更申请
          response = await applyArchiveChange(submitData)
        } else {
          // 重新提交变更申请
          response = await resubmitChange(this.changeId, submitData)
        }

        console.log('变更申请响应:', response)

        if (response && response.code === 200) {
          const message = this.changeMode ? '变更申请提交成功，等待复核' : '变更申请重新提交成功，等待复核'
          this.$message.success(response.message || message)

          // 根据模式跳转到不同页面
          if (this.changeMode) {
            this.$router.push('/hr/archive/my-change-records')
          } else {
            this.$router.push('/hr/archive/my-change-records')
          }
        } else {
          this.$message.error(response ? response.message : '提交失败')
        }
      } catch (error) {
        console.error('提交失败:', error)
        console.error('错误详情:', error.response || error.message)
        this.$message.error('提交失败: ' + (error.message || '未知错误'))
      } finally {
        this.submitting = false
      }
    },

    // 重置表单
    handleReset() {
      this.initFormData()
    },

    // 加载变更数据（用于重新提交）
    async loadChangeData() {
      try {
        const response = await getChangeDetail(this.changeId)
        if (response && response.code === 200) {
          const changeData = response.data
          console.log('重新提交-变更详情:', changeData)
          console.log('重新提交-变更前数据:', changeData.beforeDataParsed)
          console.log('重新提交-变更后数据:', changeData.afterDataParsed)
          console.log('重新提交-变更字段:', changeData.changedFieldsParsed)

          // 初始化表单数据
          this.initFormData(changeData)
        }
      } catch (error) {
        console.error('加载变更数据失败:', error)
      }
    },
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

.el-form-item {
  margin-bottom: 18px;
}

.el-row {
  margin-bottom: 0;
}
</style>