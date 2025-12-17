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
    const token = localStorage.getItem('token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
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
export const createProject = (data) => api.post('/api/salary/projects', data)
export const updateProject = (id, data) => api.put(`/api/salary/projects/${id}`, data)
export const deleteProject = (id) => api.delete(`/api/salary/projects/${id}`)
export const batchDeleteProjects = (ids) => api.delete('/api/salary/projects/batch', { data: ids })
export const getProjectEnums = () => api.get('/api/salary/projects/enums')
export const checkProjectCode = (projectCode) => api.get(`/api/salary/projects/check-code/${projectCode}`)
export const getProjectById = (id) => api.get(`/api/salary/projects/${id}`)