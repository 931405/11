import request from '@/utils/request'

export const getProfile = () => {
  return request({
    url: '/api/student/profile',
    method: 'get'
  })
}

export const updateProfile = (data) => {
  return request({
    url: '/api/student/profile',
    method: 'put',
    data
  })
}

export const getDashboard = () => {
  return request({
    url: '/api/student/dashboard',
    method: 'get'
  })
}

export const getRecommendations = (params) => {
  return request({
    url: '/api/student/recommendations',
    method: 'get',
    params
  })
}

export const applyJob = (jobId, message) => {
  return request({
    url: `/api/student/apply/${jobId}`,
    method: 'post',
    params: { message }
  })
}

export const getApplications = (params) => {
  return request({
    url: '/api/student/applications',
    method: 'get',
    params
  })
}

export const favoriteJob = (jobId) => {
  return request({
    url: `/api/student/favorite/${jobId}`,
    method: 'post'
  })
}

export const unfavoriteJob = (jobId) => {
  return request({
    url: `/api/student/favorite/${jobId}`,
    method: 'delete'
  })
}

export const getFavorites = (params) => {
  return request({
    url: '/api/student/favorites',
    method: 'get',
    params
  })
}

export const parseResume = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/student/resume/parse',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const logBehavior = (data) => {
  return request({
    url: '/api/student/behavior',
    method: 'post',
    data
  })
}

export const getInterviews = (params) => {
  return request({
    url: '/api/student/interviews',
    method: 'get',
    params
  })
}

export const updateInterviewStatus = (id, status) => {
  return request({
    url: `/api/student/interviews/${id}/status`,
    method: 'put',
    params: { status }
  })
}
