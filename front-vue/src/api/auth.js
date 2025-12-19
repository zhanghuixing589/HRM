import axios from 'axios';
import { Message } from "element-ui";
import router from '@/router'

// 创建axios实例
const api = axios.create({
    baseURL: 'http://localhost:8080', // 后端API地址
    timeout: 30000, // 请求超时时间
})

// 请求拦截器
api.interceptors.request.use(
  config => { 
    let token = localStorage.getItem('token');
    if (token) {
      token = token.trim();
      
      // 如果 token 不包含 "Bearer " 前缀，添加它
      if (!token.startsWith('Bearer ')) {
        // 检查是否以 "Bearer" 开头但没有空格
        if (token.startsWith('Bearer')) {
          // 修复 "Bearer" 后没有空格的情况
          token = 'Bearer ' + token.substring(6); // 去掉 "Bearer"
        } else {
          // 完全没有 "Bearer" 前缀，添加
          token = 'Bearer ' + token;
        }
      }
      
      // 确保只有一个 "Bearer " 前缀
      token = token.replace(/^Bearer\s+/, 'Bearer ');
      
      // 将处理后的 token 设置到请求头
      config.headers.Authorization = token;
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    console.log('API响应：',response)
    return response.data
  },
  error => {
    console.log('API请求错误：',error)
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

export function changePassword(data) {
  return api.post('/auth/changePwd', data)
}

export default api