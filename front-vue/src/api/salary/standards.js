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
    // 从本地存储获取登录令牌
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


// ==================== 编码与表单初始化 ====================

/**
 * 生成标准编码
 * @returns {Promise} 返回标准编码
 */
export function generateStandardCode() {
  return api.get(`/api/salary/standards/generate-code`)
}

/**
 * 初始化薪酬标准表单所需的所有数据
 * @returns {Promise} 返回表单初始化数据
 */
export function initFormData() {
  return api.get(`/api/salary/standards/init-form`)
}

// ==================== 标准CRUD操作 ====================

/**
 * 创建薪酬标准
 * @param {Object} data - 薪酬标准数据
 * @returns {Promise} 返回创建的薪酬标准
 */
export function createStandard(data) {
  return api.post(`/api/salary/standards/create`, data)
}

/**
 * 更新薪酬标准
 * @param {number} id - 标准ID
 * @param {Object} data - 薪酬标准数据
 * @returns {Promise} 返回更新后的薪酬标准
 */
// 更新薪酬标准
export function updateStandard(id, data) {
  return api({
    url: `/api/salary/standards/${id}`,
    method: 'put',
    data: data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

export function getStandardDetail(id) {
  return api.get(`/api/salary/standards/${id}/detail`)
}


/**
 * 分页查询薪酬标准
 * @param {Object} params - 查询参数 { pageNum, pageSize, standardCode, standardName, status }
 * @returns {Promise} 返回分页结果
 */
export function getStandardsByPage(params) {
  // 修正：使用正确的api调用方式
  return api.get('/api/salary/standards/page', { params })
}

export const getAllStandards = () => api.get(`/api/salary/standards/list`)

// 获取标准详情
export const getStandardById = (id, data) => api.put(`/api/salary/standards/${id}`, data)

// 根据编码获取标准详情
export function getStandardByCode(standardCode) {
  // 修正：使用api而不是未定义的request
  return api.get(`/api/salary/standards/code/${standardCode}`)
}

// 获取所有启用的标准
export function getActiveStandards() {
  // 修正：使用api而不是未定义的request
  return api.get('/api/salary/standards/active')
}

/**
 * 根据职位获取启用的薪酬标准
 * @param {number} positionId - 职位ID
 * @returns {Promise} 返回该职位的薪酬标准列表
 */
export function getActiveStandardsByPositionId(positionId) {
  return api.get(`/api/salary/standards/position/${positionId}/active`)
}


// 检查职位是否被占用
export function checkPositionOccupied(positionId, params = {}) {
  return api({
    url: `/api/salary/standards/positions/${positionId}/check-occupied`,
    method: 'get',
    params
  });
}

// 批量检查职位占用状态
export function batchCheckPositionsOccupied(positionIds, params = {}) {
  return api({
    url: '/api/salary/standards/positions/batch-check-occupied',
    method: 'post',
    data: positionIds,
    params
  });
}


// ==================== 审批流程 ====================

/**
 * 提交审核
 * @param {number} id - 标准ID
 * @param {string} comment - 提交意见（可选）
 * @returns {Promise}
 */
export function submitForApproval(id, comment) {
  return api.post(`/api/salary/standards/${id}/submit`, null, {
    params: { comment }
  })
}

/**
 * 审核通过
 * @param {number} id - 标准ID
 * @param {string} opinion - 审核意见（可选）
 * @returns {Promise}
 */
export function approveStandard(id, opinion) {
  return api.post(`/api/salary/standards/${id}/approve`, null, {
    params: { opinion }
  })
}

/**
 * 审核驳回
 * @param {number} id - 标准ID
 * @param {string} opinion - 驳回意见（可选）
 * @returns {Promise}
 */
export function rejectStandard(id, opinion) {
  return api.post(`/api/salary/standards/${id}/reject`, null, {
    params: { opinion }
  })
}

// ==================== 审核详情与历史记录 ====================

/**
 * 获取标准审核详情（专为审核页面设计）
 * @param {number} id - 标准ID
 * @returns {Promise} 返回审核详情数据
 */
export function getStandardApprovalDetail(id) {
  return api.get(`/api/salary/standards/${id}/approval-detail`)
}

/**
 * 获取详细的审核历史记录（包含操作日志）
 * @param {number} id - 标准ID
 * @returns {Promise} 返回详细的审核历史记录
 */
export function getDetailedApprovalHistory(id) {
  return api.get(`/api/salary/standards/${id}/approval-history-detailed`)
}

/**
 * 获取详细的审核日志
 * @param {number} id - 标准ID
 * @returns {Promise} 返回详细的审核日志
 */
export function getDetailedApprovalLogs(id) {
  return api.get(`/api/salary/standards/${id}/approval-logs-detailed`)
}

// ==================== 删除操作 ====================

/**
 * 删除薪酬标准
 * @param {number} id - 标准ID
 * @returns {Promise}
 */
export function deleteStandard(id) {
  return api.delete(`/api/salary/standards/${id}`)
}

/**
 * 批量删除薪酬标准
 * @param {Array<number>} ids - 标准ID列表
 * @returns {Promise}
 */
export function batchDeleteStandards(ids) {
  return api.delete(`/api/salary/standards/batch`, { data: ids })
}

// ==================== 职位相关 ====================

/**
 * 获取所有职位数据（用于选择）
 * @returns {Promise} 返回职位列表
 */
export function getAllPositions() {
  return api.get(`/api/salary/standards/positions/all`)
}

/**
 * 获取所有职位（用于选择）- 备用接口
 * @returns {Promise} 返回职位列表
 */
export function getPositionsForSelection() {
  return api.get(`/api/salary/standards/positions`)
}

/**
 * 批量检查职位是否已有关联标准
 * @param {Array<number>} positionIds - 职位ID列表
 * @param {number|null} currentStandardId - 当前标准ID（编辑模式）
 * @returns {Promise} 返回 { positionId: boolean } 映射
 */
export function batchCheckPositionHasStandard(positionIds, currentStandardId = null) {
  return api.post('/api/salary/standards/positions/batch-check', {
    positionIds: positionIds,
    currentStandardId: currentStandardId
  })
}

/**
 * 获取指定机构的职位
 * @param {number} orgId - 机构ID
 * @returns {Promise} 返回职位列表
 */
export function getPositionsByOrgId(orgId) {
  return api.get(`/api/salary/standards/positions/org/${orgId}`)
}

/**
 * 检查职位是否已有关联标准
 * @param {number} positionId - 职位ID
 * @returns {Promise} 返回 { positionId, hasStandard }
 */
export function checkPositionHasStandard(positionId, params = {}) {
  // 构建查询参数
  const queryParams = new URLSearchParams()
  
  // 如果是编辑模式，传入当前标准ID
  if (params.currentStandardId) {
    queryParams.append('currentStandardId', params.currentStandardId)
  }
  
  const queryString = queryParams.toString()
  const url = `/api/salary/standards/positions/${positionId}/has-standard${queryString ? '?' + queryString : ''}`
  
  return api({
    url: url,
    method: 'get'
  })
}

// ==================== 薪酬项目相关 ====================

/**
 * 获取所有薪酬项目数据
 * @returns {Promise} 返回项目列表
 */
export function getAllProjects() {
  return api.get(`/api/salary/standards/projects/all`)
}

/**
 * 获取薪酬项目用于选择 - 备用接口
 * @returns {Promise} 返回项目列表
 */
export function getProjectsForSelection() {
  return api.get(`/api/salary/standards/projects`)
}

// ==================== 审批历史与日志 ====================

/**
 * 获取审核记录
 * @param {number} id - 标准ID
 * @returns {Promise} 返回审核历史列表
 */
export function getApprovalHistory(id) {
  return api.get(`/api/salary/standards/${id}/approval-history`)
}

/**
 * 获取审核日志
 * @param {number} approvalId - 审核ID
 * @returns {Promise} 返回审核日志列表
 */
export function getApprovalLogs(approvalId) {
  return api.get(`/api/salary/standards/approval/${approvalId}/logs`)
}

// ==================== 枚举值 ====================

/**
 * 获取所有薪酬标准相关的枚举值
 * @returns {Promise} 返回所有枚举值对象
 */
export function getEnums() {
  return api.get(`/api/salary/standards/enums`)
}

/**
 * 获取标准状态枚举
 * @returns {Promise} 返回标准状态枚举列表
 */
export function getStandardStatuses() {
  return api.get(`/api/salary/standards/enums/standard-status`)
}

/**
 * 获取项目类型枚举
 * @returns {Promise} 返回项目类型枚举列表
 */
export function getProjectTypes() {
  return api.get(`/api/salary/standards/enums/project-types`)
}

/**
 * 获取项目类别枚举
 * @returns {Promise} 返回项目类别枚举列表
 */
export function getProjectCategories() {
  return api.get(`/api/salary/standards/enums/project-categories`)
}

/**
 * 获取计算方法枚举
 * @returns {Promise} 返回计算方法枚举列表
 */
export function getCalculationMethods() {
  return api.get(`/api/salary/standards/enums/calculation-methods`)
}