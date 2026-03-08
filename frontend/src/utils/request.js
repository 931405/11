import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const request = axios.create({
    baseURL: '', // Proxied via vite
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json;charset=utf-8'
    }
})

// Request interceptor
request.interceptors.request.use(config => {
    const userStore = useUserStore()
    if (userStore.token) {
        config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
}, error => {
    console.error('Request error', error)
    return Promise.reject(error)
})

// Response interceptor - conforming to exception-handling.mdc rules
request.interceptors.response.use(response => {
    const res = response.data

    // Custom successful code check (200 is configured in backend Result object)
    if (res.code === 200) {
        return res.data
    } else {
        ElMessage.error(res.message || 'Error occurred')
        return Promise.reject(new Error(res.message || 'Error occurred'))
    }
}, error => {
    if (error.response) {
        const { status, data } = error.response
        if (status === 401 || status === 403) {
            ElMessage.error('Authentication failed, please login again')
            const userStore = useUserStore()
            userStore.logout()
            window.location.href = '/login'
        } else {
            ElMessage.error(data?.message || `Error status: ${status}`)
        }
    } else {
        ElMessage.error('Network abnormal, please check your connection')
    }
    return Promise.reject(error)
})

export default request
