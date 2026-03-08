import request from '@/utils/request'

export const getMessages = (applicationId) => {
    return request({
        url: `/api/chat/${applicationId}`,
        method: 'get'
    })
}

export const sendMessage = (applicationId, content, msgType = 'TEXT') => {
    return request({
        url: `/api/chat/${applicationId}`,
        method: 'post',
        data: { content, msgType }
    })
}

export const markAsRead = (applicationId) => {
    return request({
        url: `/api/chat/${applicationId}/read`,
        method: 'put'
    })
}

export const getUnreadCount = () => {
    return request({
        url: '/api/chat/unread',
        method: 'get'
    })
}

export const getConversations = () => {
    return request({
        url: '/api/chat/conversations',
        method: 'get'
    })
}
