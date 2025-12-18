
console.log('userApi对象:', userApi)  // 添加这行查看userApi的内容
// src/api/index.js - 统一导出
import userApi from './user'
import authApi from './auth'  // 假设你已有auth.js



const api = {
  user: userApi,
  auth: authApi,
  // salary: salaryApi,
  // archive: archiveApi
}

// 导出所有API
export default api

// 按需导出
export { default as userApi } from './user'
export { default as authApi } from './auth'