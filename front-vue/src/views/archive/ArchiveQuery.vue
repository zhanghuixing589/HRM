<template>
  <div>
    <el-card shadow="never">
      <div slot="header">
        <span>档案查询</span>
      </div>
      
      <!-- 查询条件 -->
      <el-form :model="queryForm" label-width="100px" size="small">
        <!-- 机构三级联动 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="一级机构">
              <el-select 
                v-model="queryForm.firstOrgId" 
                placeholder="请选择一级机构" 
                clearable 
                @change="handleFirstOrgChange"
              >
                <el-option 
                  v-for="org in firstList" 
                  :key="org.orgId" 
                  :label="org.orgName" 
                  :value="org.orgId" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="二级机构">
              <el-select 
                v-model="queryForm.secondOrgId" 
                placeholder="请选择二级机构" 
                clearable 
                @change="handleSecondOrgChange"
                :disabled="!queryForm.firstOrgId"
              >
                <el-option 
                  v-for="org in secondList" 
                  :key="org.orgId" 
                  :label="org.orgName" 
                  :value="org.orgId" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="三级机构">
              <el-select 
                v-model="queryForm.thirdOrgId" 
                placeholder="请选择三级机构" 
                clearable 
                :disabled="!queryForm.secondOrgId"
              >
                <el-option 
                  v-for="org in thirdList" 
                  :key="org.orgId" 
                  :label="org.orgName" 
                  :value="org.orgId" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="姓名">
              <el-input 
                v-model="queryForm.name" 
                placeholder="请输入姓名" 
                clearable 
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="档案编号">
              <el-input 
                v-model="queryForm.arcCode" 
                placeholder="请输入档案编号" 
                clearable 
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="身份证号">
              <el-input 
                v-model="queryForm.idCard" 
                placeholder="请输入身份证号" 
                clearable 
                maxlength="18"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="建档时间">
              <el-date-picker
                v-model="createTimeRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="yyyy-MM-dd"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="职位">
              <el-input 
                v-model="queryForm.positionName" 
                placeholder="请输入职位" 
                clearable 
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item>
              <el-button type="primary" @click="handleSearch" :loading="loading">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <!-- 查询结果 -->
      <div style="margin-top: 20px;">
        <el-table 
          :data="archiveList" 
          stripe 
          v-loading="loading"
          size="small"
          style="width: 100%;"
        >
          <el-table-column 
            label="档案编号" 
            width="150"
            align="center"
            header-align="center"
          >
            <template slot-scope="{ row }">
              {{ row.arcCode || '--' }}
            </template>
          </el-table-column>
          <el-table-column 
            label="姓名" 
            width="100"
            align="center"
            header-align="center"
          >
            <template slot-scope="{ row }">
              {{ row.name || '--' }}
            </template>
          </el-table-column>
          <el-table-column 
            label="所属机构" 
            min-width="200"
            align="center"
            header-align="center"
          >
            <template slot-scope="{ row }">
              {{ getOrgPath(row) }}
            </template>
          </el-table-column>
          <el-table-column 
            label="职位" 
            width="120"
            align="center"
            header-align="center"
          >
            <template slot-scope="{ row }">
              {{ row.positionName || '--' }}
            </template>
          </el-table-column>
          <el-table-column 
            label="职称" 
            width="100"
            align="center"
            header-align="center"
          >
            <template slot-scope="{ row }">
              {{ row.title || '--' }}
            </template>
          </el-table-column>
          <el-table-column 
            label="建档时间" 
            width="150"
            align="center"
            header-align="center"
          >
            <template slot-scope="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column 
            label="操作" 
            width="100"
            align="center"
            header-align="center"
          >
            <template slot-scope="{ row }">
              <el-button 
                type="text" 
                size="mini" 
                @click="showDetail(row)"
              >
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <el-pagination 
          v-if="total > 0" 
          :current-page="queryForm.page + 1" 
          :page-size="queryForm.size" 
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          style="margin-top: 20px; text-align: right;"
        />
        
        <!-- 当没有数据时显示提示 -->
        <div v-if="!loading && archiveList.length === 0 && searched" style="text-align: center; padding: 40px;">
          <el-empty description="暂无符合条件的档案" />
        </div>
      </div>
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog 
      title="档案详情" 
      :visible.sync="detailDialogVisible" 
      width="700px"
      top="5vh"
    >
      <div v-if="currentArchive">
        <el-descriptions 
          :column="2" 
          border
          size="small"
        >
          <el-descriptions-item label="档案编号">{{ currentArchive.arcCode }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentArchive.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ currentArchive.sex === 1 ? '男' : '女' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ currentArchive.idCard }}</el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ currentArchive.birDate }}</el-descriptions-item>
          <el-descriptions-item label="民族">{{ currentArchive.nationality }}</el-descriptions-item>
          <el-descriptions-item label="学历">{{ currentArchive.qualification }}</el-descriptions-item>
          <el-descriptions-item label="政治面貌">{{ currentArchive.identity }}</el-descriptions-item>
          <el-descriptions-item label="宗教信仰">{{ currentArchive.belief }}</el-descriptions-item>
          <el-descriptions-item label="一级机构">{{ currentArchive.firstOrgName }}</el-descriptions-item>
          <el-descriptions-item label="二级机构">{{ currentArchive.secondOrgName }}</el-descriptions-item>
          <el-descriptions-item label="三级机构">{{ currentArchive.thirdOrgName }}</el-descriptions-item>
          <el-descriptions-item label="职位">{{ currentArchive.positionName }}</el-descriptions-item>
          <el-descriptions-item label="职称">{{ currentArchive.title }}</el-descriptions-item>
          <el-descriptions-item label="手机">{{ currentArchive.phone }}</el-descriptions-item>
          <el-descriptions-item label="Email">{{ currentArchive.email }}</el-descriptions-item>
          <el-descriptions-item label="住址">{{ currentArchive.address }}</el-descriptions-item>
          <el-descriptions-item label="国籍">{{ currentArchive.country }}</el-descriptions-item>
          <el-descriptions-item label="出生地">{{ currentArchive.birAddress }}</el-descriptions-item>
          <el-descriptions-item label="邮编">{{ currentArchive.zipCode }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ currentArchive.major }}</el-descriptions-item>
          <el-descriptions-item label="登记人">{{ currentArchive.writerName }}</el-descriptions-item>
          <el-descriptions-item label="登记时间">{{ formatDate(currentArchive.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="复核人" v-if="currentArchive.reviewerName">{{ currentArchive.reviewerName }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getOrgTree } from '@/api/org'
import { queryArchives, getArchiveDetail } from '@/api/archive'

export default {
  name: 'ArchiveQuery',
  data() {
    return {
      queryForm: {
        firstOrgId: null,
        secondOrgId: null,
        thirdOrgId: null,
        name: '',
        arcCode: '',
        idCard: '',
        positionName: '',
        page: 0,
        size: 10,
        status: 2 // 只能查询已通过的档案
      },
      createTimeRange: [],
      firstList: [],
      secondList: [],
      thirdList: [],
      archiveList: [],
      currentArchive: null,
      loading: false,
      searched: false,
      total: 0,
      detailDialogVisible: false
    }
  },
  created() {
    this.loadFirstLevelOrgs()
  },
  methods: {
    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return '--'
      try {
        const date = new Date(dateStr)
        if (isNaN(date.getTime())) {
          return dateStr.replace('T', ' ')
        }
        return date.toLocaleDateString('zh-CN').replace(/\//g, '-')
      } catch (e) {
        return dateStr
      }
    },
    
    async loadFirstLevelOrgs() {
      try {
        const response = await getOrgTree()
        if (response && response.code === 200) {
          this.firstList = Array.isArray(response.data) ? response.data : []
        }
      } catch (error) {
        console.error('加载机构失败:', error)
        this.$message.error('加载机构失败')
      }
    },
    
    handleFirstOrgChange(firstOrgId) {
      this.queryForm.secondOrgId = null
      this.queryForm.thirdOrgId = null
      this.secondList = []
      this.thirdList = []
      
      if (!firstOrgId) return
      
      const firstOrg = this.firstList.find(org => org.orgId === firstOrgId)
      if (firstOrg && firstOrg.children) {
        this.secondList = firstOrg.children
      }
    },
    
    handleSecondOrgChange(secondOrgId) {
      this.queryForm.thirdOrgId = null
      this.thirdList = []
      
      if (!secondOrgId) return
      
      const secondOrg = this.secondList.find(org => org.orgId === secondOrgId)
      if (secondOrg && secondOrg.children) {
        this.thirdList = secondOrg.children
      }
    },
    
    async handleSearch() {
      try {
        this.loading = true
        this.searched = true
        
        // 构建查询参数
        const params = {
          page: this.queryForm.page,
          size: this.queryForm.size,
          status: 2 // 只能查询已通过的档案
        }
        
        // 添加筛选条件
        if (this.queryForm.firstOrgId) params.firstOrgId = this.queryForm.firstOrgId
        if (this.queryForm.secondOrgId) params.secondOrgId = this.queryForm.secondOrgId
        if (this.queryForm.thirdOrgId) params.thirdOrgId = this.queryForm.thirdOrgId
        if (this.queryForm.name) params.name = this.queryForm.name
        if (this.queryForm.arcCode) params.arcCode = this.queryForm.arcCode
        if (this.queryForm.idCard) params.idCard = this.queryForm.idCard
        if (this.queryForm.positionName) params.positionName = this.queryForm.positionName
        if (this.createTimeRange && this.createTimeRange.length === 2) {
          params.createTimeStart = this.createTimeRange[0]
          params.createTimeEnd = this.createTimeRange[1]
        }
        
        console.log('查询参数:', params)
        
        const response = await queryArchives(params)
        console.log('查询结果:', response)
        
        if (response && response.code === 200) {
          const data = response.data
          
          // 处理不同的数据结构
          if (data && data.list) {
            // 使用 list 字段
            this.archiveList = data.list
            this.total = data.total || 0
            console.log('使用 list 字段，获取数据:', this.archiveList.length, '条')
          } else if (data && data.content) {
            // 兼容 content 字段
            this.archiveList = data.content
            this.total = data.totalElements || 0
          } else if (Array.isArray(data)) {
            // 直接是数组
            this.archiveList = data
            this.total = data.length
          } else {
            console.warn('未知的数据结构:', data)
            this.archiveList = []
            this.total = 0
            this.$message.error('数据格式异常')
          }
          
          console.log('最终数据显示:', this.archiveList)
        } else {
          this.archiveList = []
          this.total = 0
          this.$message.error(response ? response.message : '查询失败')
        }
      } catch (error) {
        console.error('查询失败:', error)
        this.$message.error('查询失败')
      } finally {
        this.loading = false
      }
    },
    
    handleReset() {
      this.queryForm = {
        firstOrgId: null,
        secondOrgId: null,
        thirdOrgId: null,
        name: '',
        arcCode: '',
        idCard: '',
        positionName: '',
        page: 0,
        size: 10,
        status: 2
      }
      this.createTimeRange = []
      this.secondList = []
      this.thirdList = []
      this.archiveList = []
      this.total = 0
      this.searched = false
    },
    
    handleSizeChange(size) {
      this.queryForm.size = size
      this.queryForm.page = 0
      this.handleSearch()
    },
    
    handleCurrentChange(page) {
      this.queryForm.page = page - 1
      this.handleSearch()
    },
    
    getOrgPath(row) {
      if (!row) return '--'
      const first = row.firstOrgName || ''
      const second = row.secondOrgName || ''
      const third = row.thirdOrgName || ''
      return `${first} / ${second} / ${third}`
    },
    
    async showDetail(row) {
      try {
        const response = await getArchiveDetail(row.arcId)
        if (response && response.code === 200) {
          this.currentArchive = response.data
          this.detailDialogVisible = true
        }
      } catch (error) {
        console.error('获取详情失败:', error)
        this.$message.error('获取详情失败')
      }
    }
  }
}
</script>