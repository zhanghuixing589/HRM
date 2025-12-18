// archiveSalary.js - 新API文件
import request from '@/api/auth'

export const getSalaryStandardsByPosition = (positionId, title) => {
  return request.get('/hr/archive/salary/by-position-title', {
    params: {
      positionId: positionId,
      title: title
    }
  })
}

export const searchSalaryStandards = (keyword) => {
  return request.get('/hr/archive/salary/search', {
    params: {
      keyword: keyword
    }
  })
}

export const getAllActiveSalaryStandards = () => {
  return request.get('/hr/archive/salary/all-active')
}