import request from '@/utils/request'

export const searchJobs = (params) => {
  return request({
    url: '/api/jobs',
    method: 'get',
    params
  })
}

export const getJobDetail = (id) => {
  return request({
    url: `/api/jobs/${id}`,
    method: 'get'
  })
}

export const getCategories = () => {
  return request({
    url: '/api/jobs/categories',
    method: 'get'
  })
}
