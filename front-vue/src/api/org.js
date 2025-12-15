import api from '@/api/auth'

const base = '/hr/organization'

export const getOrgTree = () => {
  return api.get(`${base}/tree`)
}

export const saveOrg = (data) => {
  return api.post(`${base}/save`, data)
}

export const delOrg = (id) => {
  return api.delete(`${base}/delete/${id}`)
}

export const toggleOrg = (id, st) => {
  return api.put(`${base}/${id}/status/${st}`)
}

// 新增：获取机构详情
export const getOrgById = (id) => {
  return api.get(`${base}/${id}`)
}

// 新增：根据级别获取机构
export const getOrgsByLevel = (level) => {
  return api.get(`${base}/level/${level}`)
}