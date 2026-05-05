import { defineStore } from 'pinia'
import { ref } from 'vue'

const readStoredUserInfo = () => {
    try {
        const raw = localStorage.getItem('userInfo')
        return raw ? JSON.parse(raw) : {}
    } catch (error) {
        localStorage.removeItem('userInfo')
        return {}
    }
}

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(readStoredUserInfo())

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

    const updateUserInfo = (data) => {
        userInfo.value = {
            ...userInfo.value,
            userId: data.userId ?? userInfo.value.userId,
            username: data.username ?? userInfo.value.username,
            realName: data.realName ?? userInfo.value.realName,
            role: data.role ?? userInfo.value.role,
            avatar: data.avatar ?? userInfo.value.avatar,
            phone: data.phone ?? userInfo.value.phone,
            email: data.email ?? userInfo.value.email
        }
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }

    const isStudent = () => userInfo.value.role === 'STUDENT'
    const isEnterprise = () => userInfo.value.role === 'ENTERPRISE'
    const isAdmin = () => userInfo.value.role === 'ADMIN'

    return {
        token,
        userInfo,
        setLoginInfo,
        updateUserInfo,
        logout,
        isStudent,
        isEnterprise,
        isAdmin
    }
})
