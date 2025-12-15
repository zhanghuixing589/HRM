<template>
  <div>
    <el-card shadow="never">
      <div slot="header">
        <span>机构 / 职位设置</span>
        <el-button type="text" icon="el-icon-refresh" @click="loadAll" style="float:right;">刷新</el-button>
      </div>

      <!-- 一级机构 -->
      <h4>一级机构</h4>
      <div class="level-box">
        <el-row :gutter="10">
          <el-col :span="8">
            <el-input v-model="firstOrgName" placeholder="输入一级机构名称" size="small" clearable/>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" icon="el-icon-plus" size="small"
                       :disabled="!firstOrgName" @click="addOrg(1)">添加</el-button>
          </el-col>
        </el-row>
        <el-table :data="firstList" size="mini" stripe style="margin-top:10px;" v-loading="loading">
          <el-table-column prop="orgCode" label="机构编码" width="120"/>
          <el-table-column prop="orgName" label="机构名称"/>
          <el-table-column label="状态" width="80">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                {{ scope.row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template slot-scope="scope">
              <el-button type="text" size="mini" @click="editOrg(scope.row)">编辑</el-button>
              <el-button type="text" size="mini" @click="toggleOrgStatus(scope.row)">
                {{ scope.row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button type="text" size="mini" @click="confirmDeleteOrg(scope.row)" style="color:#F56C6C;">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 二级机构 -->
      <h4>二级机构</h4>
      <div class="level-box">
        <el-row :gutter="10">
          <el-col :span="8">
            <el-select v-model="selFirst" placeholder="请选择一级机构" size="small" clearable @change="onFirstChange">
              <el-option v-for="f in firstList" :key="f.orgId" :label="f.orgName" :value="f.orgId"/>
            </el-select>
          </el-col>
          <el-col :span="8">
            <el-input v-model="secondName" placeholder="输入二级机构名称" size="small" clearable/>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" icon="el-icon-plus" size="small"
                       :disabled="!selFirst || !secondName" @click="addOrg(2)">添加</el-button>
          </el-col>
        </el-row>
        <el-table :data="secondList" size="mini" stripe style="margin-top:10px;" v-loading="loading">
          <el-table-column prop="orgCode" label="机构编码" width="120"/>
          <el-table-column prop="orgName" label="机构名称"/>
          <el-table-column label="隶属于" width="180">
            <template slot-scope="scope">{{ getParentName(scope.row.parId) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                {{ scope.row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template slot-scope="scope">
              <el-button type="text" size="mini" @click="editOrg(scope.row)">编辑</el-button>
              <el-button type="text" size="mini" @click="toggleOrgStatus(scope.row)">
                {{ scope.row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button type="text" size="mini" @click="confirmDeleteOrg(scope.row)" style="color:#F56C6C;">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 三级机构 -->
      <h4>三级机构</h4>
      <div class="level-box">
        <el-row :gutter="10">
          <el-col :span="8">
            <el-select v-model="selSecond" placeholder="请选择二级机构" size="small" clearable @change="onSecondChange">
              <el-option v-for="s in filteredSecondList" :key="s.orgId" :label="s.orgName" :value="s.orgId"/>
            </el-select>
          </el-col>
          <el-col :span="8">
            <el-input v-model="thirdName" placeholder="输入三级机构名称" size="small" clearable/>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" icon="el-icon-plus" size="small"
                       :disabled="!selSecond || !thirdName" @click="addOrg(3)">添加</el-button>
          </el-col>
        </el-row>
        <el-table :data="thirdList" size="mini" stripe style="margin-top:10px;" v-loading="loading">
          <el-table-column prop="orgCode" label="机构编码" width="120"/>
          <el-table-column prop="orgName" label="机构名称"/>
          <el-table-column label="隶属于" width="180">
            <template slot-scope="scope">{{ getParentName(scope.row.parId) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                {{ scope.row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template slot-scope="scope">
              <el-button type="text" size="mini" @click="editOrg(scope.row)">编辑</el-button>
              <el-button type="text" size="mini" @click="toggleOrgStatus(scope.row)">
                {{ scope.row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button type="text" size="mini" @click="confirmDeleteOrg(scope.row)" style="color:#F56C6C;">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 职位设置 -->
      <h4>职位设置</h4>
      <div class="level-box">
        <el-row :gutter="10">
          <el-col :span="8">
            <el-cascader v-model="selOrgPath"
                         :options="cascaderOptions"
                         :props="{emitPath:false, checkStrictly: true}"
                         placeholder="请选择三级机构"
                         size="small"
                         clearable
                         style="width:100%"/>
          </el-col>
          <el-col :span="6">
            <el-input v-model="posForm.posCode" placeholder="职位编码" size="small" clearable/>
          </el-col>
          <el-col :span="6">
            <el-input v-model="posForm.posName" placeholder="职位名称" size="small" clearable/>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" icon="el-icon-plus" size="small"
                       :disabled="!selOrgPath || !posForm.posCode || !posForm.posName"
                       @click="savePos">添加</el-button>
          </el-col>
        </el-row>

        <el-table :data="posList" size="mini" stripe style="margin-top:10px;" v-loading="loadingPos">
          <el-table-column prop="posCode" label="职位编码" width="120"/>
          <el-table-column prop="posName" label="职位名称"/>
          <el-table-column label="所属机构" width="260">
            <template slot-scope="scope">{{ getOrgPath(scope.row.orgId) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                {{ scope.row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template slot-scope="scope">
              <el-button type="text" size="mini" @click="editPos(scope.row)">编辑</el-button>
              <el-button type="text" size="mini" @click="togglePosStatus(scope.row)">
                {{ scope.row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button type="text" size="mini" @click="confirmDeletePos(scope.row)" style="color:#F56C6C;">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- 职位分页 -->
        <el-pagination
          @size-change="handlePosSizeChange"
          @current-change="handlePosCurrentChange"
          :current-page="posPagination.page"
          :page-sizes="[5, 10, 20, 50]"
          :page-size="posPagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="posPagination.total"
          style="margin-top: 15px; text-align: right;"
          small
          background>
        </el-pagination>
      </div>
    </el-card>

    <!-- 职位编辑弹窗 -->
    <el-dialog :title="'编辑职位'" :visible.sync="posShow" width="500px" @close="resetPosForm">
      <el-form :model="editingPos" label-width="100px">
        <el-form-item label="职位名称" required>
          <el-input v-model="editingPos.posName" placeholder="请输入职位名称" maxlength="100" clearable/>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editingPos.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="2">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="posShow = false">取消</el-button>
        <el-button type="primary" @click="confirmPos" :loading="savingPos">保存</el-button>
      </span>
    </el-dialog>

    <!-- 机构编辑弹窗 -->
    <el-dialog :title="orgForm.orgId ? '编辑机构' : '新增机构'" :visible.sync="orgShow" width="500px" @close="resetOrgForm">
      <el-form :model="orgForm" label-width="100px">
        <el-form-item label="机构名称" required>
          <el-input v-model="orgForm.orgName" placeholder="请输入机构名称" maxlength="100"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="orgForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="2">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="orgShow = false">取消</el-button>
        <el-button type="primary" @click="confirmOrg" :loading="saving">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getOrgTree, saveOrg, delOrg, toggleOrg } from '@/api/org'
import { savePos, delPos, togglePos, getPositionsPage } from '@/api/pos'

export default {
  data() {
    return {
      // 机构相关
      firstList: [],
      secondList: [],
      thirdList: [],
      firstOrgName: '',
      selFirst: null,
      secondName: '',
      selSecond: null,
      thirdName: '',
      loading: false,
      
      // 机构弹窗
      orgShow: false,
      saving: false,
      orgForm: {
        orgId: null,
        orgName: '',
        orgLevel: 1,
        parId: null,
        status: 1
      },
      parentOrgPath: null,
      
      // 职位相关
      selOrgPath: null,
      posForm: { posCode: '', posName: '' },
      posList: [],
      loadingPos: false,
      
      // 职位分页
      posPagination: {
        page: 0, // 注意：后端分页从0开始
        size: 10,
        total: 0
      },
      
      // 职位弹窗
      posShow: false,
      savingPos: false,
      editingPos: {
        posId: null,
        posCode: '',
        posName: '',
        orgId: null,
        orgName: '',
        status: 1
      }
    }
  },
  computed: {
    cascaderOptions() {
      return (this.firstList || []).map(f => ({
        value: f.orgId,
        label: f.orgName,
        children: (f.children || []).map(s => ({
          value: s.orgId,
          label: s.orgName,
          children: (s.children || []).map(t => ({ 
            value: t.orgId, 
            label: t.orgName 
          }))
        }))
      }))
    },
    filteredSecondList() {
      if (!this.selFirst) return this.secondList
      return this.secondList.filter(s => s.parId === this.selFirst)
    }
  },
  created() {
    this.loadAll()
  },
  methods: {
    // 加载数据
    loadAll() {
      this.loadOrganizations()
      this.loadPositions()
    },
    
    // 加载机构数据
    loadOrganizations() {
      this.loading = true
      getOrgTree().then(res => {
        const data = res.data || res
        const tree = Array.isArray(data) ? data : []
        this.firstList = tree
        this.secondList = tree.flatMap(f => f.children || [])
        this.thirdList = this.secondList.flatMap(s => s.children || [])
      }).catch(() => {
        this.$message.error('加载机构失败')
      }).finally(() => {
        this.loading = false
      })
    },
    
    // 加载职位数据（使用后端分页）
    loadPositions() {
      this.loadingPos = true
      getPositionsPage(this.posPagination.page, this.posPagination.size).then(res => {
        const data = res.data || res
        if (data && data.content) {
          this.posList = data.content
          this.posPagination.total = data.totalElements || 0
        } else {
          this.posList = Array.isArray(data) ? data : []
          this.posPagination.total = this.posList.length
        }
      }).catch(() => {
        this.$message.error('加载职位失败')
      }).finally(() => {
        this.loadingPos = false
      })
    },
    
    // 分页处理
    handlePosSizeChange(size) {
      this.posPagination.size = size
      this.posPagination.page = 0
      this.loadPositions()
    },
    
    handlePosCurrentChange(page) {
      // Element UI分页从1开始，后端从0开始，需要减1
      this.posPagination.page = page - 1
      this.loadPositions()
    },
    
    // 机构相关方法
    onFirstChange() {
      this.selSecond = null
      this.thirdName = ''
    },
    
    onSecondChange() {
      this.thirdName = ''
    },
    
    getParentName(pid) {
      const allOrgs = [...this.firstList, ...this.secondList]
      const parent = allOrgs.find(x => x.orgId === pid)
      return parent ? parent.orgName : '无上级机构'
    },
    
    getOrgPath(orgId) {
      const org = [...this.firstList, ...this.secondList, ...this.thirdList].find(x => x.orgId === orgId)
      if (!org) return ''
      const path = []
      let cur = org
      while (cur) {
        path.unshift(cur.orgName)
        cur = [...this.firstList, ...this.secondList].find(x => x.orgId === cur.parId)
      }
      return path.join(' / ')
    },
    
    addOrg(level) {
      let name = ''
      let parId = null
      
      if (level === 1) {
        name = this.firstOrgName.trim()
        if (!name) return this.$message.warning('请输入一级机构名称')
        parId = null
        this.firstOrgName = ''
      } else if (level === 2) {
        name = this.secondName.trim()
        if (!name) return this.$message.warning('请输入二级机构名称')
        if (!this.selFirst) return this.$message.warning('请选择一级机构')
        parId = this.selFirst
        this.secondName = ''
      } else {
        name = this.thirdName.trim()
        if (!name) return this.$message.warning('请输入三级机构名称')
        if (!this.selSecond) return this.$message.warning('请选择二级机构')
        parId = this.selSecond
        this.thirdName = ''
      }
      
      this.saveOrganization({
        orgId: null,
        orgName: name,
        orgLevel: level,
        parId: parId,
        status: 1
      })
    },
    
    editOrg(row) {
      this.orgForm = {
        orgId: row.orgId,
        orgName: row.orgName,
        orgLevel: row.orgLevel,
        parId: row.parId,
        status: row.status
      }
      
      if (row.parId) {
        this.parentOrgPath = row.parId
      }
      
      this.orgShow = true
    },
    
    confirmOrg() {
      if (!this.orgForm.orgName) {
        this.$message.warning('请输入机构名称')
        return
      }
      
      if (this.orgForm.orgLevel > 1) {
        this.orgForm.parId = this.parentOrgPath
        if (!this.orgForm.parId) {
          this.$message.warning('请选择上级机构')
          return
        }
      } else {
        this.orgForm.parId = null
      }
      
      this.saving = true
      this.saveOrganization(this.orgForm)
    },
    
    saveOrganization(payload) {
      console.log('保存机构:', payload)
      saveOrg(payload).then(res => {
        if (res.code === 200) {
          this.$message.success(res.message || '保存成功')
          this.orgShow = false
          this.loadOrganizations()
        } else {
          this.$message.error(res.message || '保存失败')
        }
      }).catch(e => {
        console.error('保存机构失败:', e)
        this.$message.error('保存失败: ' + (e.response?.data?.message || e.message))
      }).finally(() => {
        this.saving = false
      })
    },
    
    toggleOrgStatus(row) {
      const newStatus = row.status === 1 ? 2 : 1
      const action = newStatus === 1 ? '启用' : '禁用'
      this.$confirm(`确定${action}机构【${row.orgName}】吗？`, '提示', {
        type: 'warning'
      }).then(() => {
        toggleOrg(row.orgId, newStatus).then(res => {
          if (res.code === 200) {
            this.$message.success('操作成功')
            this.loadOrganizations()
          } else {
            this.$message.error(res.message || '操作失败')
          }
        }).catch(e => {
          this.$message.error('操作失败: ' + (e.response?.data?.message || e.message))
        })
      }).catch(() => {})
    },
    
    confirmDeleteOrg(row) {
      this.$confirm(`确定删除机构【${row.orgName}】吗？此操作不可恢复！`, '警告', {
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }).then(() => {
        delOrg(row.orgId).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadOrganizations()
          } else {
            this.$message.error(res.message || '删除失败')
          }
        }).catch(e => {
          this.$message.error('删除失败: ' + (e.response?.data?.message || e.message))
        })
      }).catch(() => {})
    },
    
    resetOrgForm() {
      this.orgForm = {
        orgId: null,
        orgName: '',
        orgLevel: 1,
        parId: null,
        status: 1
      }
      this.parentOrgPath = null
    },
    
    // 职位相关方法
    savePos() {
      if (!this.selOrgPath) {
        this.$message.warning('请选择所属机构')
        return
      }
      if (!this.posForm.posCode.trim()) {
        this.$message.warning('请输入职位编码')
        return
      }
      if (!this.posForm.posName.trim()) {
        this.$message.warning('请输入职位名称')
        return
      }
      
      const payload = {
        posId: null,
        posCode: this.posForm.posCode,
        posName: this.posForm.posName,
        orgId: this.selOrgPath,
        status: 1
      }
      
      this.savePosition(payload)
    },
    
    editPos(row) {
      this.editingPos = {
        posId: row.posId,
        posCode: row.posCode,
        posName: row.posName,
        orgId: row.orgId,
        orgName: this.getOrgPath(row.orgId),
        status: row.status
      }
      this.posShow = true
    },
    
    confirmPos() {
      if (!this.editingPos.posCode.trim()) {
        this.$message.warning('请输入职位编码')
        return
      }
      if (!this.editingPos.posName.trim()) {
        this.$message.warning('请输入职位名称')
        return
      }

      this.savingPos = true
      const payload = {
        posId: this.editingPos.posId,
        posCode: this.editingPos.posCode,
        posName: this.editingPos.posName,
        orgId: this.editingPos.orgId, // 保持原机构ID不变
        status: this.editingPos.status
      }
      this.savePosition('保存职位数据：', payload)
    },
    
    savePosition(payload) {
      console.log('保存职位:', payload)
      savePos(payload).then(res => {
        if (res.code === 200) {
          this.$message.success(res.message || '保存成功')
          this.posShow = false
          this.resetPosForm()
          this.posForm = { posCode: '', posName: '' }
          this.selOrgPath = null
          this.loadPositions()
        } else {
          this.$message.error(res.message || '保存失败')
        }
      }).catch(e => {
        console.error('保存职位失败:', e)
        this.$message.error('保存失败: ' + (e.response?.data?.message || e.message))
      }).finally(() => {
        this.savingPos = false
      })
    },
    
    // 职位启用/禁用
    togglePosStatus(row) {
      const newStatus = row.status === 1 ? 2 : 1
      const action = newStatus === 1 ? '启用' : '禁用'
      this.$confirm(`确定${action}职位【${row.posName}】吗？`, '提示', {
        type: 'warning'
      }).then(() => {
        togglePos(row.posId, newStatus).then(res => {
          if (res.code === 200) {
            this.$message.success('操作成功')
            this.loadPositions()
          } else {
            this.$message.error(res.message || '操作失败')
          }
        }).catch(e => {
          this.$message.error('操作失败: ' + (e.response?.data?.message || e.message))
        })
      }).catch(() => {})
    },
    
    confirmDeletePos(row) {
      this.$confirm(`确定删除职位【${row.posName}】吗？此操作不可恢复！`, '警告', {
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }).then(() => {
        delPos(row.posId).then(res => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadPositions()
          } else {
            this.$message.error(res.message || '删除失败')
          }
        }).catch(e => {
          this.$message.error('删除失败: ' + (e.response?.data?.message || e.message))
        })
      }).catch(() => {})
    },
    
    resetPosForm() {
      this.editingPos = {
        posId: null,
        posCode: '',
        posName: '',
        orgId: null,
        orgName: '',
        status: 1
      }
    }
  }
}
</script>

<style scoped lang="scss">
h4 {
  margin: 20px 0 10px 0;
  font-size: 14px;
  color: #666;
  font-weight: bold;
  border-left: 4px solid #409EFF;
  padding-left: 8px;
}

.level-box {
  background: #fff;
  padding: 15px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  margin-bottom: 20px;
  
  .el-row {
    margin-bottom: 15px;
  }
}

.el-table {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  
  ::v-deep .el-table__header-wrapper {
    background-color: #f5f7fa;
  }
}

.el-button--text {
  padding: 0;
  min-height: auto;
}
</style>