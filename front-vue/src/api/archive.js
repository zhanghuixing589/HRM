import request from '@/api/auth'

export const registerArchive = (data) => {
  return request.post('/hr/archive/register', data)
}

export const getMyArchives = (params) => {
  return request.get('/hr/archive/my-archives', { params })
}

export const getArchiveProcess = (arcId) => {
  return request.get(`/hr/archive/process/${arcId}`)
}

// 人事经理接口
export const getPendingReviewArchives = () => {
  return request.get('/hr/archive/pending-review')
}

export const reviewArchive = (data) => {
  return request.post('/hr/archive/review', data)
}

export const deleteArchive = (arcId) => {
  return request.delete(`/hr/archive/${arcId}`)
}

export const restoreArchive = (arcId) => {
  return request.put(`/hr/archive/restore/${arcId}`)
}

export const getArchiveDetail = (arcId) => {
  return request.get(`/hr/archive/detail/${arcId}`)
}

export const getArchiveStats = () => {
  return request.get('/hr/archive/stats')
}

export const resubmitArchive = (arcId, data) => {
  return request.post(`/hr/archive/resubmit/${arcId}`, data)
}

// 档案查询接口
export const queryArchives = (params) => {
  return request.get('/hr/archive/query-list', { params })
}