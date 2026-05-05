import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/HomePage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/ProfilePage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/student/dashboard',
    name: 'StudentDashboard',
    component: () => import('@/views/student/StudentDashboard.vue'),
    meta: { requiresAuth: true, roles: ['STUDENT', 'ADMIN'] }
  },
  {
    path: '/student/profile-edit',
    name: 'StudentProfileEdit',
    component: () => import('@/views/student/ProfileEdit.vue'),
    meta: { requiresAuth: true, roles: ['STUDENT', 'ADMIN'] }
  },
  {
    path: '/student/jobs',
    name: 'StudentJobList',
    component: () => import('@/views/student/JobList.vue'),
    meta: { requiresAuth: true, roles: ['STUDENT', 'ADMIN'] }
  },
  {
    path: '/student/jobs/:id',
    name: 'StudentJobDetail',
    component: () => import('@/views/student/JobDetail.vue'),
    meta: { requiresAuth: true, roles: ['STUDENT', 'ADMIN'] }
  },
  {
    path: '/enterprise/jobs',
    name: 'EnterpriseJobs',
    component: () => import('@/views/enterprise/JobManage.vue'),
    meta: { requiresAuth: true, roles: ['ENTERPRISE', 'ADMIN'] }
  },
  {
    path: '/enterprise/talents',
    name: 'EnterpriseTalents',
    component: () => import('@/views/enterprise/TalentPool.vue'),
    meta: { requiresAuth: true, roles: ['ENTERPRISE', 'ADMIN'] }
  },
  {
    path: '/enterprise/jobs/:id/candidates',
    name: 'EnterpriseCandidates',
    component: () => import('@/views/enterprise/CandidateList.vue'),
    meta: { requiresAuth: true, roles: ['ENTERPRISE', 'ADMIN'] }
  },
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/AdminDashboard.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/admin/UserManagement.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/admin/match-settings',
    name: 'AdminMatchSettings',
    component: () => import('@/views/admin/MatchSettings.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginPage.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterPage.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const role = userStore.userInfo.role

  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
    return
  }

  if ((to.path === '/login' || to.path === '/register') && userStore.token) {
    next('/home')
    return
  }

  if (to.meta.roles && !to.meta.roles.includes(role)) {
    next('/home')
    return
  }

  next()
})

export default router
