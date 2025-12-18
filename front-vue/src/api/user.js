import api from "./auth";

/**
 * 用户相关API
 */

// 职位相关API
export const getPositionNameById = (posId) => {
  return api.get(`/hr/position/${posId}/name`)
}

export const getBatchPositionNames = (posIds) => {
  return api.post('/hr/position/batch-names', { posIds })
}

// 获取当前登录用户信息（包括档案信息）
export const getCurrentUser = () => {
  return api.get('/api/user/current')
}

// 根据用户ID获取用户信息
export const getUserById = (userId) => {
  return api.get(`/api/user/${userId}`)
}

// 根据工号获取用户信息
export const getUserByCode = (userCode) => {
  return api.get(`/api/user/code/${userCode}`)
}

// 获取用户基本信息（简化版）
export const getUserInfo = () => {
  return api.get('/api/user/info')
}

// 更新用户信息
export const updateUser = (data) => {
  return api.put('/api/user/update', data)
}

// 修改密码
export const changePassword = (data) => {
  return api.put('/api/user/change-password', data)
}

// 更新头像
export const updateAvatar = (formData) => {
  return api.post('/api/user/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取用户档案详情
export const getUserArchive = (archiveId) => {
  return api.get(`/api/user/archive/${archiveId}`)
}

// 获取用户所在机构信息
export const getUserOrgInfo = () => {
  return api.get('/api/user/org-info')
}

// 获取用户权限信息
export const getUserPermissions = () => {
  return api.get('/api/user/permissions')
}

// 测试API连接
export const testApiConnection = () => {
  return api.get('/api/user/health')
}



/**
 * 档案相关API
 */

// 获取档案详情
export const getArchiveDetail = (archiveId) => {
  return api.get(`/api/archive/${archiveId}`)
}

// 根据档案编码获取档案
export const getArchiveByCode = (arcCode) => {
  return api.get(`/api/archive/code/${arcCode}`)
}

// 更新档案信息
export const updateArchive = (data) => {
  return api.put('/api/archive/update', data)
}

// 获取档案列表
export const getArchives = (params) => {
  return api.get('/api/archive/list', { params })
}

/**
 * 通用工具API
 */

// 获取所有机构
export const getAllOrganizations = () => {
  return api.get('/api/organization/all')
}

// 获取机构树
export const getOrgTree = () => {
  return api.get('/apiorganization/tree')
}

// 获取所有职位
export const getAllPositions = () => {
  return api.get('/api/salary/standard/position/all')
}

// 获取薪资标准
export const getSalaryStandards = () => {
  return api.get('/api/salary/standards')
}

// 获取当前用户薪资信息
export const getUserSalaryInfo = () => {
  return api.get('/salary/user')
}



export default {
  // 用户相关
  getCurrentUser,
  getUserById,
  getUserByCode,
  getUserInfo,
  updateUser,
  changePassword,
  updateAvatar,
  getUserArchive,
  getUserOrgInfo,
  getUserPermissions,
  testApiConnection,

  getPositionNameById,        // 添加这行
  getBatchPositionNames,      // 添加这行
  
  // 档案相关
  getArchiveDetail,
  getArchiveByCode,
  updateArchive,
  getArchives,
  
  // 通用
  getAllOrganizations,
  getOrgTree,
  getAllPositions,
  getSalaryStandards,
  getUserSalaryInfo
}