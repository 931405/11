import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
    state: () => ({
        sidebarCollapsed: false,
        currentLocation: '北京', // Default location
        unreadCount: 0
    }),
    actions: {
        toggleSidebar() {
            this.sidebarCollapsed = !this.sidebarCollapsed
        },
        setLocation(location) {
            this.currentLocation = location
        },
        async fetchUnreadCount() {
            try {
                const { getUnreadCount } = await import('@/api/chat')
                const res = await getUnreadCount()
                this.unreadCount = res || 0
            } catch (error) {
                console.error('Failed to fetch unread count', error)
            }
        }
    },
    persist: true
})
