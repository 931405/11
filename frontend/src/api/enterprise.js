import request from '@/utils/request'

export const getEnterpriseInfo = () => {
    return request({
        url: '/api/enterprise/info',
        method: 'get'
    })
}

export const updateEnterpriseInfo = (data) => {
    return request({
        url: '/api/enterprise/info',
        method: 'put',
        data
    })
}

export const createJob = (data) => {
    return request({
        url: '/api/enterprise/jobs',
        method: 'post',
        data
    })
}

export const updateJob = (jobId, data) => {
    return request({
        url: `/api/enterprise/jobs/${jobId}`,
        method: 'put',
        data
    })
}

export const deleteJob = (jobId) => {
    return request({
        url: `/api/enterprise/jobs/${jobId}`,
        method: 'delete'
    })
}

export const getEnterpriseJobs = (params) => {
    return request({
        url: '/api/enterprise/jobs',
        method: 'get',
        params
    })
}

export const getJobCandidates = (jobId, params) => {
    return request({
        url: `/api/enterprise/jobs/${jobId}/candidates`,
        method: 'get',
        params
    })
}

export const updateApplicationStatus = (applicationId, status, remark) => {
    return request({
        url: `/api/enterprise/applications/${applicationId}/status`,
        method: 'put',
        params: { status, remark }
    })
}

export const sendInterviewInvitation = (data) => {
    return request({
        url: '/api/enterprise/interviews',
        method: 'post',
        data
    })
}

export const getInterviews = (params) => {
    return request({
        url: '/api/enterprise/interviews',
        method: 'get',
        params
    })
}

export const getStudentProfile = (studentUserId) => {
    return request({
        url: `/api/enterprise/student/${studentUserId}`,
        method: 'get'
    })
}

export const searchTalents = (params) => {
    return request({
        url: '/api/enterprise/talents',
        method: 'get',
        params
    })
}

export const inviteTalent = (data) => {
    return request({
        url: '/api/enterprise/talents/invite',
        method: 'post',
        data
    })
}

export const getDailyApplicationTrend = () => {
    return request({
        url: '/api/enterprise/dashboard/trend',
        method: 'get'
    })
}
