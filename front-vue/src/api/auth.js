import axios from 'axios';
import { Message } from "element-ui";
import router from '@/router'

// 创建axios实例
const api = axios.create({
    baseURL: 'http://localhost:8080', // 后端API地址
    timeout: 10000, // 请求超时时间
})

// 请求拦截器
api.interceptors.request.use(
  config => { 
    // 从本地存储获取教师登陆令牌
    const token = localStorage.getItem('token');
    if (token) {  // 成功获取则将其添加到请求头
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    const res = response.data
    return res  // 直接返回响应数据
  },
  error => {
    Message.error(error.response?.data?.message || '请求出错，请稍后重试')  // 优先使用后端返回的错误消息
    if (error.response?.status === 401) {
      localStorage.clear()
      router.replace('/login')
      Message.error('登录已过期，请重新登录')
    }
    return Promise.reject(error)
  } 
)

export const login = (data) => {
  return api.post('/auth/login', data )
}

export const getInfo = () => {
  return api.get('/auth/userinfo')
}

export const logout = () => {
  return api.post('/auth/logout')
}

export default api