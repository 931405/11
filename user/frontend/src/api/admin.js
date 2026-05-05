import request from '@/utils/request'

export const getDashboardStats = () => {
  return request({
    url: '/api/admin/dashboard',
    method: 'get'
  })
}

export const getUserList = (params) => {
  return request({
    url: '/api/admin/users',
    method: 'get',
    params
  })
}

export const updateUserStatus = (userId, status) => {
  return request({
    url: `/api/admin/users/${userId}/status`,
    method: 'put',
    params: { status }
  })
}

export const getMatchSettings = () => {
  return request({
    url: '/api/admin/settings/match',
    method: 'get'
  })
}

export const saveMatchSettings = (data) => {
  return request({
    url: '/api/admin/settings/match',
    method: 'put',
    data
  })
}
