import api from './auth'

// 查询员工列表
export const getEmployeeList = (params) => {
  return api.get('/api/admin/users', { params })
}

// 获取用户详情
export const getUserDetail = (userId) => {
  return api.get(`/api/admin/users/${userId}`)
}

// 分配角色
export const assignUserRole = (data) => {
  return api.post('/api/admin/users/assign-role', data)
}

// 恢复用户
export const restoreUser = (userId) => {
  return api.put(`/api/admin/users/${userId}/restore`)
}

// 禁用用户
export const disableUser = (userId) => {
  return api.put(`/api/admin/users/${userId}/disable`)
}

// 获取所有角色
export const getAllRoles = () => {
  return api.get('/api/admin/users/roles')
}

// 获取所有职位
export const getAllPositions = () => {
  return api.get('/api/admin/users/positions')
}