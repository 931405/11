<template>
  <div class="app-layout">
    <template v-if="userStore.isStudent() || !userStore.token">
      <!-- 仿BOSS直聘：学生端/游客使用顶部导航布局 -->
      <div class="top-nav-layout">
        <header class="top-header">
          <div class="header-inner">
            <div class="header-left">
              <div class="logo" @click="$router.push('/')">
                <span class="logo-text">BOSS<span style="font-weight: 300;">直聘</span></span>
              </div>
              
              <div class="location-selector" @click="locationDialogVisible = true">
                <el-icon><Location /></el-icon>
                <span>{{ appStore.currentLocation }}</span>
                <span class="switch-btn">[切换]</span>
              </div>

              <nav class="main-nav" v-if="userStore.token">
                <router-link to="/student/dashboard" class="nav-item" active-class="active">首页</router-link>
                <router-link to="/student/jobs" class="nav-item" active-class="active">职位</router-link>
                <router-link to="/student/favorites" class="nav-item" active-class="active">收藏</router-link>
                <router-link to="/student/interviews" class="nav-item" active-class="active">面试</router-link>
                <router-link to="/student/applications" class="nav-item" active-class="active">
                  <el-badge :value="appStore.unreadCount" :hidden="appStore.unreadCount === 0" class="nav-badge">
                    消息
                  </el-badge>
                </router-link>
              </nav>
            </div>
            
            <div class="header-right">
              <div class="search-box" v-if="userStore.token">
                <el-input v-model="headerSearch" placeholder="搜索职位、公司" class="header-search" @keyup.enter="doHeaderSearch">
                  <template #suffix>
                    <el-icon class="search-icon" @click="doHeaderSearch"><Search /></el-icon>
                  </template>
                </el-input>
              </div>

              <div class="quick-links" v-if="userStore.token">
                <router-link to="/student/profile" class="ql-item">简历</router-link>
              </div>

              <div v-if="userStore.token" class="user-menu">
                <el-dropdown trigger="hover" popper-class="boss-dropdown" @command="handleCommand">
                  <span class="user-avatar-link">
                    <span class="username">{{ userStore.userInfo.realName || userStore.userInfo.username }}</span>
                    <el-avatar :size="28" :src="userStore.userInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" class="avatar-img" />
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu class="custom-dropdown-menu">
                      <!-- Profile -->
                      <el-dropdown-item command="profile">
                        <div class="dropdown-item-row">
                          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                          <div class="dropdown-item-text">
                            <span class="dropdown-item-title">个人中心</span>
                            <span class="dropdown-item-desc">查看投递状态、编辑在线简历</span>
                          </div>
                        </div>
                      </el-dropdown-item>

                      <!-- Settings -->
                      <el-dropdown-item command="settings" divided>
                        <div class="dropdown-item-row">
                          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
                          <span class="dropdown-item-title">账号与安全中心</span>
                        </div>
                      </el-dropdown-item>

                      <!-- Privacy -->
                      <el-dropdown-item command="privacy">
                        <div class="dropdown-item-row">
                          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                          <span class="dropdown-item-title">隐私保护</span>
                        </div>
                      </el-dropdown-item>

                      <!-- Notifications -->
                      <el-dropdown-item command="notifications" divided>
                        <div class="dropdown-item-row">
                          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
                          <span class="dropdown-item-title">消息通知</span>
                        </div>
                      </el-dropdown-item>

                      <!-- Switch role -->
                      <el-dropdown-item command="switch-role" divided>
                        <div class="dropdown-item-row">
                          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polyline points="16 3 21 3 21 8"/><line x1="4" y1="20" x2="21" y2="3"/><polyline points="21 16 21 21 16 21"/><line x1="15" y1="15" x2="21" y2="21"/><line x1="4" y1="4" x2="9" y2="9"/></svg>
                          <span class="dropdown-item-title">切换为招聘者</span>
                        </div>
                      </el-dropdown-item>

                      <!-- Logout -->
                      <el-dropdown-item command="logout" divided>
                        <div class="dropdown-item-row logout-row">
                          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
                          <span class="dropdown-item-title">退出登录</span>
                        </div>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
              <div v-else class="auth-buttons">
                <el-button color="#00a6a7" plain @click="$router.push('/login')">登录</el-button>
                <el-button color="#00a6a7" @click="$router.push('/register')">免费入驻</el-button>
              </div>
            </div>
          </div>
        </header>

        <main class="top-nav-main">
          <div :class="['content-wrapper', { 'full-width': route.meta.fullWidth }]">
             <router-view v-slot="{ Component }">
              <transition name="fade" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>
          </div>
        </main>
      </div>
    </template>
    
    <template v-else>
      <!-- 企业端/管理端：保留后台侧边栏布局 -->
      <el-container class="dashboard-layout">
        <el-header height="60px" class="header">
          <div class="logo">
            <span style="color: var(--color-accent); font-weight: bold; font-size: 20px;">兼职直聘 <span style="font-size:14px; color: var(--color-text-secondary);">{{ userStore.isEnterprise() ? '企业工作台' : '管理后台' }}</span></span>
          </div>
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                {{ userStore.userInfo.realName || userStore.userInfo.username }} ({{ userStore.isEnterprise() ? '企业' : '管理员' }})
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="userStore.isEnterprise()" command="profile">企业信息配置</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <el-container class="main-container">
          <el-aside width="220px" class="aside">
            <el-menu
              :default-active="activeMenu"
              class="el-menu-vertical"
              router
              background-color="var(--color-bg-card)"
              text-color="var(--color-text-primary)"
              active-text-color="var(--color-accent)"
            >
              <el-menu-item :index="dashboardRoute">
                <el-icon><DataBoard /></el-icon>
                <span>工作台首页</span>
              </el-menu-item>
              
              <!-- 企业菜单 -->
              <template v-if="userStore.isEnterprise()">
                <el-menu-item index="/enterprise/jobs">
                  <el-icon><Suitcase /></el-icon>
                  <span>职位管理</span>
                </el-menu-item>
                <el-menu-item index="/enterprise/profile">
                  <el-icon><OfficeBuilding /></el-icon>
                  <span>企业信息认证</span>
                </el-menu-item>
                <el-menu-item index="/enterprise/messages">
                  <el-icon>
                    <el-badge :value="appStore.unreadCount" :hidden="appStore.unreadCount === 0" is-dot>
                      <ChatDotRound />
                    </el-badge>
                  </el-icon>
                  <span>消息互动</span>
                </el-menu-item>
                <el-menu-item index="/enterprise/talents">
                  <el-icon><User /></el-icon>
                  <span>发现牛人</span>
                </el-menu-item>
              </template>
              
              <!-- 管理员菜单 -->
              <template v-if="userStore.isAdmin()">
                <el-menu-item index="/admin/audits">
                  <el-icon><Stamp /></el-icon>
                  <span>企业审核</span>
                </el-menu-item>
                <el-menu-item index="/admin/users">
                  <el-icon><Avatar /></el-icon>
                  <span>用户管理</span>
                </el-menu-item>
                <el-menu-item index="/admin/jobs">
                  <el-icon><Suitcase /></el-icon>
                  <span>职位审核</span>
                </el-menu-item>
                <el-menu-item index="/admin/categories">
                  <el-icon><Files /></el-icon>
                  <span>职位分类配置</span>
                </el-menu-item>
                <el-menu-item index="/admin/feedback">
                  <el-icon><ChatLineSquare /></el-icon>
                  <span>意见反馈处理</span>
                </el-menu-item>
              </template>
            </el-menu>
          </el-aside>
          
          <el-main class="main-content">
            <router-view v-slot="{ Component }">
              <transition name="fade" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>
          </el-main>
        </el-container>
      </el-container>
    </template>
    <!-- Location Selection Dialog -->
    <el-dialog v-model="locationDialogVisible" title="选择工作城市" width="600px" custom-class="location-dialog">
      <div class="city-selector">
        <div class="city-group">
          <div class="cg-title">热门城市</div>
          <div class="cg-list">
            <el-button 
              v-for="city in topCities" 
              :key="city" 
              :type="appStore.currentLocation === city ? 'primary' : 'default'"
              @click="handleSelectLocation(city)"
            >
              {{ city }}
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { getUnreadCount } from '@/api/chat'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const headerSearch = ref('')
const locationDialogVisible = ref(false)
const topCities = ['北京', '上海', '广州', '深圳', '杭州', '成都', '武汉', '西安', '南京', '苏州', '全国']

let pollingTimer = null

onMounted(() => {
  if (userStore.token && !userStore.isAdmin()) {
    appStore.fetchUnreadCount()
    pollingTimer = setInterval(() => {
      appStore.fetchUnreadCount()
    }, 10000) // Poll every 10 seconds
  }
})

onUnmounted(() => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
  }
})

const handleSelectLocation = (city) => {
  appStore.setLocation(city)
  locationDialogVisible.value = false
  // Re-emit logic or let the components react automatically
}

const activeMenu = computed(() => {
  return route.path
})

const dashboardRoute = computed(() => {
  if (userStore.isStudent()) return '/student/dashboard'
  if (userStore.isEnterprise()) return '/enterprise/dashboard'
  if (userStore.isAdmin()) return '/admin/dashboard'
  return '/'
})

const doHeaderSearch = () => {
  if (!headerSearch.value.trim()) return
  router.push({ path: '/student/jobs', query: { keyword: headerSearch.value } })
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    if (userStore.isStudent()) router.push('/student/profile')
    else if (userStore.isEnterprise()) router.push('/enterprise/profile')
  } else if (command === 'settings') {
    router.push('/student/security')
  } else if (command === 'privacy') {
    router.push('/student/privacy')
  } else if (command === 'notifications') {
    router.push('/student/notifications')
  } else if (command === 'switch-role') {
    router.push('/login')
  }
}
</script>

<style scoped>
/* =========== Top Nav Layout (Boss Zhipin Style) =========== */
.top-nav-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg-primary);
}

.top-header {
  background: linear-gradient(90deg, var(--header-bg-start), var(--header-bg-end));
  height: 50px; /* Boss Zhipin uses a slimmer header */
  position: sticky;
  top: 0;
  z-index: 100;
  color: var(--header-text);
}

:deep(.nav-badge .el-badge__content) {
  top: auto;
  bottom: 0;
  transform: translateY(20%) translateX(80%);
  font-size: 10px;
  padding: 0 4px;
  height: 16px;
  line-height: 16px;
  border-radius: 8px;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  cursor: pointer;
  margin-right: 20px;
}

.logo-text {
  font-size: 20px;
  font-weight: bold;
  color: #fff;
  letter-spacing: 1px;
}

.location-selector {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #fff;
  cursor: pointer;
  margin-right: 30px;
}
.switch-btn {
  color: rgba(255,255,255,0.7);
}

.main-nav {
  display: flex;
  gap: 24px;
}

.nav-item {
  position: relative;
  font-size: 14px;
  color: #fff;
  text-decoration: none;
  line-height: 50px;
  transition: opacity 0.2s;
}

.nav-item:hover, .nav-item.active {
  color: #fff;
  font-weight: 500;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: 6px;
  left: 50%;
  transform: translateX(-50%);
  width: 16px;
  height: 2px;
  border-radius: 2px;
  background-color: #fff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.search-box {
  width: 240px;
}

:deep(.header-search .el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.15) !important;
  box-shadow: none !important;
  border-radius: 16px;
  height: 32px;
}
:deep(.header-search .el-input__inner) {
  color: #fff;
  font-size: 13px;
}
:deep(.header-search .el-input__inner::placeholder) {
  color: rgba(255,255,255,0.6);
}
.search-icon {
  color: rgba(255,255,255,0.8);
  cursor: pointer;
}

.quick-links {
  display: flex;
  gap: 20px;
}

.ql-item {
  color: #fff;
  font-size: 14px;
  text-decoration: none;
}
.ql-item:hover {
  opacity: 0.8;
}

.user-avatar-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-size: 14px;
}
.avatar-img {
  border: 1px solid rgba(255,255,255,0.2);
}

/* === Dropdown Overrides === */
:deep(.boss-dropdown) {
  padding: 0 !important;
  border-radius: 8px !important;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1) !important;
  transform: translateY(10px);
}
.custom-dropdown-menu {
  padding: 0 !important;
  width: 260px;
}
.dropdown-item-row {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}
.dropdown-item-row svg {
  flex-shrink: 0;
  color: var(--color-text-secondary);
}
.dropdown-item-text {
  display: flex;
  flex-direction: column;
}
.dropdown-item-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
}
.dropdown-item-desc {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 2px;
}
.logout-row {
  color: #F56C6C;
}
.logout-row svg {
  color: #F56C6C;
}
.logout-row .dropdown-item-title {
  color: #F56C6C;
}
:deep(.custom-dropdown-menu .el-dropdown-menu__item) {
  padding: 12px 20px;
  font-size: 14px;
  line-height: 1.4;
}
:deep(.custom-dropdown-menu .el-dropdown-menu__item:hover) {
  background-color: var(--color-bg-primary);
  color: var(--color-accent);
}

.top-nav-main {
  flex: 1;
  padding: 24px 0;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.content-wrapper.full-width {
  max-width: none;
  padding: 0;
}

/* =========== Sidebar Layout (Enterprise/Admin) =========== */
.dashboard-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: var(--color-bg-card);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  z-index: 10;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: var(--color-text-primary);
  font-weight: 500;
}

.main-container {
  height: calc(100vh - 60px);
}

.aside {
  background-color: var(--color-bg-card);
  border-right: 1px solid var(--color-border);
}

.el-menu-vertical {
  border-right: none;
  height: 100%;
}

.main-content {
  background-color: var(--color-bg-primary);
  padding: 24px;
  overflow-y: auto;
}

/* Animations */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Location Dialog Styles */
.city-selector {
  padding: 10px;
}
.city-group {
  margin-bottom: 20px;
}
.cg-title {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 12px;
}
.cg-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
</style>
