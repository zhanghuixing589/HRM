/* eslint-disable */
import axios from 'axios'
import { Message } from 'element-ui'
import router from '@/router'

// 创建axios实例
const api = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 10000,
})

// 请求拦截器
api.interceptors.request.use(
  config => { 
    console.log('发送请求:', config.url, config.method)
    
    // 清除损坏的 token
    const token = localStorage.getItem('token')
    if (token) {
      // 检查 token 是否损坏
      try {
        // 简单的 token 验证
        if (token.includes('�') || token.length < 20) { // 损坏的 token 特征
          console.warn('检测到损坏的 token，正在清除...')
          localStorage.removeItem('token')
          sessionStorage.removeItem('token')
          // 不添加 Authorization 头
        } else {
          config.headers.Authorization = `Bearer ${token}`
        }
      } catch (error) {
        console.error('Token 验证失败:', error)
        localStorage.removeItem('token')
        sessionStorage.removeItem('token')
      }
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
api.interceptors.response.use(
  response => response.data,  // 直接返回data
  error => {
    Message.error(error.response?.data?.message || '请求出错')
    if (error.response?.status === 401) {
      localStorage.clear()
      router.replace('/')
      Message.error('登录已过期')
    }
    return Promise.reject(error)
  } 
)

// 薪酬项目管理API
export const getProjectList = (params) => {
  return api.get('/api/salary/projects/page', {
    params: params
  })
}

export const getAllProjects = () => api.get('/api/salary/projects/list')

// 创建项目 - 注意这里使用 /create 路径
export const createProject = (data) => api.post('/api/salary/projects/create', data)

export const updateProject = (id, data) => api.put(`/api/salary/projects/${id}`, data)

export const deleteProject = (id) => api.delete(`/api/salary/projects/${id}`)

export const batchDeleteProjects = (ids) => api.delete('/api/salary/projects/batch', { data: ids })

export const getProjectEnums = () => api.get('/api/salary/projects/enums')

// 检查项目编码 - 添加 excludeId 参数支持
export const checkProjectCode = (projectCode, excludeId = null) => {
  return api.get(`/api/salary/projects/check-code/${projectCode}`, {
    params: { excludeId }
  })
}

export const getProjectById = (id) => api.get(`/api/salary/projects/${id}`)

// 以下为新增的API接口，对应Controller中的其他方法
export const getProjectByCode = (projectCode) => api.get(`/api/salary/projects/code/${projectCode}`)

export const enableProject = (id) => api.put(`/api/salary/projects/${id}/enable`)

export const disableProject = (id) => api.put(`/api/salary/projects/${id}/disable`)

export const getEnabledProjects = () => api.get('/api/salary/projects/enabled')

export const getEnabledIncomeProjects = () => api.get('/api/salary/projects/enabled/income')

export const getEnabledDeductionProjects = () => api.get('/api/salary/projects/enabled/deduction')

export const getProjectsByCategory = (category) => api.get(`/api/salary/projects/by-category/${category}`)

export const getProjectsByType = (type) => api.get(`/api/salary/projects/by-type/${type}`)

// 枚举相关的API
export const getProjectTypesEnum = () => api.get('/api/salary/projects/enums/project-types')

export const getProjectCategoriesEnum = () => api.get('/api/salary/projects/enums/project-categories')

export const getCalculationMethodsEnum = () => api.get('/api/salary/projects/enums/calculation-methods')

export const getPredefinedCodesEnum = () => api.get('/api/salary/projects/enums/predefined-codes')


// 导入项目（可选功能）
export const importProjects = (data) => api.post('/api/salary/projects/import', data)

// 导出默认API实例（如果需要）
export default api