import request from '@/utils/request'

export const searchJobs = (params) => {
    return request({
        url: '/api/jobs',
        method: 'get',
        params
    })
}

export const getJobDetail = (jobId) => {
    return request({
        url: `/api/jobs/${jobId}`,
        method: 'get'
    })
}

export const getCategories = () => {
    return request({
        url: '/api/jobs/categories',
        method: 'get'
    })
}
