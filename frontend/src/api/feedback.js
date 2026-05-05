import request from '@/utils/request'

export const submitFeedback = (data) => {
    return request({
        url: '/api/feedback',
        method: 'post',
        data
    })
}

export const getMyFeedbacks = (params) => {
    return request({
        url: '/api/feedback/my',
        method: 'get',
        params
    })
}
