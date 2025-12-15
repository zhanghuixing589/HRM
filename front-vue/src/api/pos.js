import api from '@/api/auth'

const base = '/hr/position'

/* 单机构查询 */
export const listByOrg = (orgId) => {
  return api.get(`${base}/org/${orgId}`)
}
/* 批量机构查询 */
export const listByOrgIds = (orgIds) => {
  return api.post(`${base}/by-org-ids`, orgIds)
}
export const savePos = (data) => {
  return api.post(`${base}/save`, data)
}
export const delPos = (id) => {
  return api.delete(`${base}/${id}`)
}
export const togglePos = (id, st) => {
  return api.put(`${base}/${id}/status/${st}`)
}

/* 职位分页查询 */
export const getPositionsPage = (page = 0, size = 10) => {
  return api.get(`${base}/page`, {
    params: {
      page: page,
      size: size
    }
  })
}