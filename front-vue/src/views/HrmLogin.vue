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
    </el-card>
  </div>
</template>

<script>
import { login } from '@/api/auth'

export default {
  data () {
    return {
      form: { username: '', password: '' },
      rules: {
        username: [{ required: true, message: '请输入工号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      loading: false
    }
  },
  methods: {
    handleLogin () {
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
          .catch(() => {})   // 异常已在 auth.js 拦截器里统一提示
          .finally(() => { this.loading = false })
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