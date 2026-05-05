import request from '@/utils/request'

export const getEnterprisePublicInfo = (id) => {
    return request({
        url: /api/enterprises/,
        method: 'get'
    })
}

export const getEnterpriseJobCount = (id) => {
    return request({
        url: /api/enterprises//job-count,
        method: 'get'
    })
}
