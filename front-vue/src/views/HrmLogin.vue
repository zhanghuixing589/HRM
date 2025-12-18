<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <div slot="header">HRM 系统登录</div>
      <el-form :model="form" :rules="rules" ref="form" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="工号" prefix-icon="el-icon-user"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="el-icon-lock"
            @keyup.enter="handleLogin"></el-input>
        </el-form-item>
        <el-button type="primary" style="width:100%" :loading="loading" @click="handleLogin">登录</el-button>
      </el-form>

      <!-- 新增：修改密码入口 -->
      <div style="text-align: center; margin-top: 12px;">
        <el-link type="primary" :underline="false" @click="showPwdDialog = true">修改密码</el-link>
      </div>
    </el-card>

    <!-- 修改密码弹窗 -->
    <el-dialog title="修改密码" :visible.sync="showPwdDialog" width="400px" append-to-body :close-on-click-modal="false"
      @closed="$refs.pwdForm.resetFields()">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdForm" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="pwdForm.username" placeholder="工号" prefix-icon="el-icon-user"></el-input>
        </el-form-item>
        <el-form-item prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" placeholder="原始密码"
            prefix-icon="el-icon-lock"></el-input>
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="新密码"
            prefix-icon="el-icon-key"></el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="再次输入新密码"
            prefix-icon="el-icon-key"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showPwdDialog = false">取 消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="handleChangePwd">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { login } from '@/api/auth'
import { changePassword } from '@/api/auth'   // 新增 api

export default {
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.pwdForm.newPassword) {
        callback(new Error('两次输入的新密码不一致'))
      } else {
        callback()
      }
    }
    return {
      form: { username: '', password: '' },
      rules: {
        username: [{ required: true, message: '请输入工号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      loading: false,

      /* 修改密码相关 */
      showPwdDialog: false,
      pwdLoading: false,
      pwdForm: {
        username: '',
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      pwdRules: {
        username: [{ required: true, message: '请输入工号', trigger: 'blur' }],
        oldPassword: [{ required: true, message: '请输入原始密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '新密码长度至少6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleLogin() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.loading = true
        login(this.form)
          .then(res => {
            if (res.code === 200) {
              localStorage.setItem('token', res.data.token)
              localStorage.setItem('user', JSON.stringify(res.data.user))
              this.$router.push(res.data.redirectUrl || '/')
              this.$message.success('登录成功')
            } else {
              this.$message.error(res.message || '登录失败')
            }
          })
          .catch(() => { })
          .finally(() => { this.loading = false })
      })
    },

    /* 新增：修改密码提交 */
    handleChangePwd() {
      this.$refs.pwdForm.validate(valid => {
        if (!valid) return
        this.pwdLoading = true
        changePassword(this.pwdForm)
          .then(res => {
            if (res.code === 200) {
              this.$message.success('密码修改成功，请重新登录')
              this.showPwdDialog = false
              // 清空登录框，方便重新登录
              this.form.password = ''
            } else {
              this.$message.error(res.message || '密码修改失败')
            }
          })
          .finally(() => { this.pwdLoading = false })
      })
    }
  }
}
</script>

<style scoped>
.login-wrap {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f3f3;
}

.login-card {
  width: 400px;
}
</style>