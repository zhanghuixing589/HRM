import request from '@/api/auth'

const base = '/hr/change'

// 申请档案变更
export const applyArchiveChange = (data) => {
  return request.post(`${base}/apply`, data)
}

// 获取我的变更记录
export const getMyChangeRecords = (params) => {
  return request.get(`${base}/my-records`, { params })
}

// 获取变更详情
export const getChangeDetail = (changeId) => {
  return request.get(`${base}/detail/${changeId}`)
}

// 获取变更流程
export const getChangeProcess = (changeId) => {
  return request.get(`${base}/process/${changeId}`)
}

// 重新提交变更申请
export const resubmitChange = (changeId, data) => {
  return request.post(`${base}/resubmit/${changeId}`, data)
}

// 人事经理接口
// 复核变更申请
export const reviewChange = (data) => {
  return request.post(`${base}/review`, data)
}

// 获取待复核变更申请
export const getPendingReviewChanges = () => {
  return request.get(`${base}/pending-review`)
}

// 查询所有变更申请
export const queryChanges = (params) => {
  return request.get(`${base}/query-list`, { params })
}