import request from '@/utils/request'

export const getMyProfile = () => {
    return request({
        url: '/api/profile/me',
        method: 'get'
    })
}

export const updateMyProfile = (data) => {
    return request({
        url: '/api/profile/me',
        method: 'put',
        data
    })
}
