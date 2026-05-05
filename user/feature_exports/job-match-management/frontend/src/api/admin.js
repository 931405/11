import request from '@/utils/request'

export const getDashboardStats = () => {
    return request({
        url: '/api/admin/dashboard',
        method: 'get'
    })
}

export const getPendingAudits = (params) => {
    return request({
        url: '/api/admin/audits',
        method: 'get',
        params
    })
}

export const auditEnterprise = (enterpriseId, status, remark) => {
    return request({
        url: `/api/admin/audits/${enterpriseId}`,
        method: 'put',
        params: { status, remark }
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

export const getFeedbackList = (params) => {
    return request({
        url: '/api/admin/feedback',
        method: 'get',
        params
    })
}

export const replyFeedback = (feedbackId, reply) => {
    return request({
        url: `/api/admin/feedback/${feedbackId}/reply`,
        method: 'put',
        params: { reply }
    })
}

export const createCategory = (params) => {
    return request({
        url: '/api/admin/categories',
        method: 'post',
        params
    })
}

export const updateCategory = (categoryId, params) => {
    return request({
        url: `/api/admin/categories/${categoryId}`,
        method: 'put',
        params
    })
}

export const deleteCategory = (categoryId) => {
    return request({
        url: `/api/admin/categories/${categoryId}`,
        method: 'delete'
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

export const getSystemLogs = (params) => {
    return request({
        url: '/api/admin/logs',
        method: 'get',
        params
    })
}

export const getJobList = (params) => {
    return request({
        url: '/api/admin/jobs',
        method: 'get',
        params
    })
}

export const updateJobStatus = (jobId, status) => {
    return request({
        url: `/api/admin/jobs/${jobId}/status`,
        method: 'put',
        params: { status }
    })
}
