import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

    const setLoginInfo = (data) => {
        token.value = data.token
        userInfo.value = {
            userId: data.userId,
            username: data.username,
            realName: data.realName,
            role: data.role,
            avatar: data.avatar
        }
        localStorage.setItem('token', data.token)
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }

    const logout = () => {
        token.value = ''
        userInfo.value = {}
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
    }

    const isStudent = () => userInfo.value.role === 'STUDENT'
    const isEnterprise = () => userInfo.value.role === 'ENTERPRISE'
    const isAdmin = () => userInfo.value.role === 'ADMIN'

    return {
        token,
        userInfo,
        setLoginInfo,
        logout,
        isStudent,
        isEnterprise,
        isAdmin
    }
})
