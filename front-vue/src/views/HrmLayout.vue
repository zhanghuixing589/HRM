<template>
  <div>
    <el-button type="text" @click="logout">退出登录</el-button>
    <h1>欢迎 {{ user.userName }}，角色类型：{{ user.roleType }}</h1>
  </div>
</template>

<script>
export default {
  computed: {
    user () {
      return JSON.parse(localStorage.getItem('user') || '{}')
    }
  },
  methods: {
    logout () {
      localStorage.clear()
      this.$router.replace('/login')
    }
  }
}
</script>