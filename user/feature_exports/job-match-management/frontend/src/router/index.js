import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
    {
        path: '/',
        component: () => import('@/layout/AppLayout.vue'),
        children: [
            {
                path: '',
                redirect: to => {
                    const userStore = useUserStore()
                    if (!userStore.token) return '/login'
                    if (userStore.isStudent()) return '/student/dashboard'
                    if (userStore.isEnterprise()) return '/enterprise/dashboard'
                    if (userStore.isAdmin()) return '/admin/dashboard'
                    return '/login'
                }
            },
            // === Student Routes ===
            {
                path: 'student/dashboard',
                name: 'StudentDashboard',
                component: () => import('@/views/student/StudentDashboard.vue'),
                meta: { requiresAuth: true, role: 'STUDENT' }
            },
            {
                path: 'student/jobs',
                name: 'StudentJobs',
                component: () => import('@/views/student/JobList.vue'),
                meta: { requiresAuth: true, role: 'STUDENT' }
            },
            {
                path: 'student/jobs/:id',
                name: 'StudentJobDetail',
                component: () => import('@/views/student/JobDetail.vue'),
                meta: { requiresAuth: true, role: 'STUDENT', fullWidth: true }
            },
            {
                path: 'student/applications',
                name: 'StudentApplications',
                component: () => import('@/views/student/MyApplications.vue'),
                meta: { requiresAuth: true, role: 'STUDENT' }
            },
            {
                path: 'student/interviews',
                name: 'StudentInterviews',
                component: () => import('@/views/student/MyInterviews.vue'),
                meta: { requiresAuth: true, role: 'STUDENT' }
            },
            {
                path: 'student/favorites',
                name: 'StudentFavorites',
                component: () => import('@/views/student/MyFavorites.vue'),
                meta: { requiresAuth: true, role: 'STUDENT' }
            },
            {
                path: 'student/profile',
                name: 'StudentProfile',
                component: () => import('@/views/student/ProfileEdit.vue'),
                meta: { requiresAuth: true, role: 'STUDENT' }
            },
            {
                path: 'student/security',
                name: 'AccountSecurity',
                component: () => import('@/views/student/AccountSecurity.vue'),
                meta: { requiresAuth: true, role: 'STUDENT' }
            },
            {
                path: 'student/privacy',
                name: 'PrivacySettings',
                component: () => import('@/views/student/PrivacySettings.vue'),
                meta: { requiresAuth: true, role: 'STUDENT' }
            },
            {
                path: 'student/notifications',
                name: 'NotificationSettings',
                component: () => import('@/views/student/NotificationSettings.vue'),
                meta: { requiresAuth: true, role: 'STUDENT' }
            },

            // === Enterprise Routes ===
            {
                path: 'enterprise/dashboard',
                name: 'EnterpriseDashboard',
                component: () => import('@/views/enterprise/EnterpriseDashboard.vue'),
                meta: { requiresAuth: true, role: 'ENTERPRISE' }
            },
            {
                path: 'enterprise/jobs',
                name: 'EnterpriseJobs',
                component: () => import('@/views/enterprise/JobManagement.vue'),
                meta: { requiresAuth: true, role: 'ENTERPRISE' }
            },
            {
                path: 'enterprise/jobs/create',
                name: 'EnterpriseJobCreate',
                component: () => import('@/views/enterprise/JobPostForm.vue'),
                meta: { requiresAuth: true, role: 'ENTERPRISE' }
            },
            {
                path: 'enterprise/jobs/:id/edit',
                name: 'EnterpriseJobEdit',
                component: () => import('@/views/enterprise/JobPostForm.vue'),
                meta: { requiresAuth: true, role: 'ENTERPRISE' }
            },
            {
                path: 'enterprise/jobs/:id/candidates',
                name: 'EnterpriseJobCandidates',
                component: () => import('@/views/enterprise/CandidateList.vue'),
                meta: { requiresAuth: true, role: 'ENTERPRISE' }
            },
            {
                path: 'enterprise/profile',
                name: 'EnterpriseProfile',
                component: () => import('@/views/enterprise/EnterpriseProfile.vue'),
                meta: { requiresAuth: true, role: 'ENTERPRISE' }
            },
            {
                path: 'enterprise/messages',
                name: 'EnterpriseMessages',
                component: () => import('@/views/enterprise/EnterpriseMessages.vue'),
                meta: { requiresAuth: true, role: 'ENTERPRISE' }
            },
            {
                path: 'enterprise/talents',
                name: 'EnterpriseTalents',
                component: () => import('@/views/enterprise/TalentPool.vue'),
                meta: { requiresAuth: true, role: 'ENTERPRISE' }
            },

            // === Admin Routes ===
            {
                path: 'admin/dashboard',
                name: 'AdminDashboard',
                component: () => import('@/views/admin/AdminDashboard.vue'),
                meta: { requiresAuth: true, role: 'ADMIN' }
            },
            {
                path: 'admin/audits',
                name: 'AdminAudits',
                component: () => import('@/views/admin/EnterpriseAudit.vue'),
                meta: { requiresAuth: true, role: 'ADMIN' }
            },
            {
                path: 'admin/users',
                name: 'AdminUsers',
                component: () => import('@/views/admin/UserManagement.vue'),
                meta: { requiresAuth: true, role: 'ADMIN' }
            },
            {
                path: 'admin/jobs',
                name: 'AdminJobs',
                component: () => import('@/views/admin/JobAudit.vue'),
                meta: { requiresAuth: true, role: 'ADMIN' }
            },
            {
                path: 'admin/categories',
                name: 'AdminCategories',
                component: () => import('@/views/admin/CategoryManagement.vue'),
                meta: { requiresAuth: true, role: 'ADMIN' }
            },
            {
                path: 'admin/feedback',
                name: 'AdminFeedback',
                component: () => import('@/views/admin/FeedbackList.vue'),
                meta: { requiresAuth: true, role: 'ADMIN' }
            },
            {
                path: 'admin/match-settings',
                name: 'AdminMatchSettings',
                component: () => import('@/views/admin/MatchSettings.vue'),
                meta: { requiresAuth: true, role: 'ADMIN' }
            },
            {
                path: 'admin/logs',
                name: 'AdminSystemLogs',
                component: () => import('@/views/admin/SystemLogs.vue'),
                meta: { requiresAuth: true, role: 'ADMIN' }
            }
        ]
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
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// Navigation Guards
router.beforeEach((to, from, next) => {
    const userStore = useUserStore()

    if (to.meta.requiresAuth && !userStore.token) {
        next('/login')
    } else if (to.meta.role && userStore.userInfo.role !== to.meta.role) {
        // Role matching
        next('/')
    } else {
        next()
    }
})

export default router
