<template>
  <div class="page">
    <div class="shell">
      <section class="hero">
        <div class="hero-main">
          <div class="hero-user">
            <el-avatar :size="56" :src="userStore.userInfo.avatar || ''" class="hero-avatar">
              {{ displayInitial }}
            </el-avatar>
            <div>
              <p class="eyebrow">兼职通 · {{ roleText }}中心</p>
              <h1>你好，{{ displayName }}</h1>
            </div>
          </div>

          <p class="desc">{{ heroIntro }}</p>

          <div class="hero-tags">
            <span v-for="item in quickActions" :key="item.title" class="hero-tag">{{ item.title }}</span>
          </div>

          <div class="hero-actions">
            <el-button @click="router.push('/profile')">基础资料</el-button>
            <el-button type="primary" @click="router.push(primaryAction.path)">进入{{ primaryAction.title }}</el-button>
            <el-button plain @click="handleLogout">退出登录</el-button>
          </div>

          <div class="hero-stats">
            <div v-for="item in heroStats" :key="item.label" class="hero-stat">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <p>{{ item.note }}</p>
            </div>
          </div>
        </div>

        <div class="hero-banner">
          <div class="banner-head">
            <div>
              <p class="banner-tip">首页轮播</p>
              <h2>让首页更有层次感</h2>
            </div>
            <span>自动播放，可手动切换</span>
          </div>

          <el-carousel class="carousel" :interval="4500" arrow="always" indicator-position="outside" height="318px">
            <el-carousel-item v-for="slide in heroSlides" :key="slide.title">
              <div class="banner-card" :style="{ '--banner-bg': slide.bg, '--banner-accent': slide.accent }">
                <div class="banner-grid">
                  <div>
                    <span class="banner-tag">{{ slide.tag }}</span>
                    <h3>{{ slide.title }}</h3>
                    <p>{{ slide.desc }}</p>
                    <div class="banner-pills">
                      <span v-for="pill in slide.pills" :key="pill">{{ pill }}</span>
                    </div>
                  </div>

                  <div class="banner-metrics">
                    <div v-for="metric in slide.metrics" :key="metric.label" class="metric-box">
                      <span>{{ metric.label }}</span>
                      <strong>{{ metric.value }}</strong>
                    </div>
                  </div>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
      </section>

      <div class="summary-grid">
        <div class="summary-card">
          <span class="label">用户编号</span>
          <strong>{{ userStore.userInfo.userId || '-' }}</strong>
        </div>
        <div class="summary-card">
          <span class="label">登录账号</span>
          <strong>{{ userStore.userInfo.username || '-' }}</strong>
        </div>
        <div class="summary-card">
          <span class="label">当前角色</span>
          <strong>{{ roleText }}</strong>
        </div>
      </div>

      <div class="panel">
        <div class="panel-header">
          <h2>快捷入口</h2>
          <span>根据当前角色自动展示</span>
        </div>
        <div class="action-grid">
          <div v-for="item in quickActions" :key="item.title" class="action-card" @click="router.push(item.path)">
            <span class="action-no">{{ item.no }}</span>
            <div class="action-title">{{ item.title }}</div>
            <div class="action-desc">{{ item.desc }}</div>
          </div>
        </div>
      </div>

      <div v-if="isStudent" class="panel">
        <div class="panel-header">
          <h2>学生概览</h2>
          <span>推荐岗位与简历状态</span>
        </div>

        <div class="summary-grid inner-grid">
          <div class="summary-card">
            <span class="label">简历完整度</span>
            <strong>{{ formatPercent(studentSummary.profileCompleteness) }}</strong>
          </div>
          <div class="summary-card">
            <span class="label">已投递岗位</span>
            <strong>{{ studentSummary.totalApplications ?? 0 }}</strong>
          </div>
          <div class="summary-card">
            <span class="label">推荐岗位数</span>
            <strong>{{ studentSummary.recommendationCount ?? 0 }}</strong>
          </div>
        </div>

        <el-table :data="studentJobs" v-loading="loading" style="width: 100%">
          <el-table-column prop="title" label="岗位名称" min-width="220" />
          <el-table-column prop="companyName" label="企业名称" min-width="180" />
          <el-table-column label="匹配度" width="120" align="center">
            <template #default="{ row }">
              {{ row.matchScore ? `${Math.round(row.matchScore)}%` : '—' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="{ row }">
              <el-button type="primary" link @click="router.push(`/student/jobs/${row.id}`)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="isEnterprise" class="panel">
        <div class="panel-header">
          <h2>企业概览</h2>
          <span>岗位数据与候选人管理</span>
        </div>

        <div class="summary-grid inner-grid">
          <div class="summary-card">
            <span class="label">在招岗位</span>
            <strong>{{ enterpriseSummary.activeJobs ?? 0 }}</strong>
          </div>
          <div class="summary-card">
            <span class="label">累计申请</span>
            <strong>{{ enterpriseSummary.totalApplications ?? 0 }}</strong>
          </div>
          <div class="summary-card">
            <span class="label">高匹配人才</span>
            <strong>{{ enterpriseSummary.matchedTalents ?? 0 }}</strong>
          </div>
        </div>

        <el-table :data="enterpriseJobs" v-loading="loading" style="width: 100%">
          <el-table-column prop="title" label="岗位名称" min-width="220" />
          <el-table-column prop="status" label="状态" width="120" align="center" />
          <el-table-column prop="applyCount" label="申请人数" width="120" align="center" />
          <el-table-column label="操作" width="180" align="center">
            <template #default="{ row }">
              <el-button type="primary" link @click="router.push(`/enterprise/jobs/${row.id}/candidates`)">候选人</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="isAdmin" class="panel">
        <div class="panel-header">
          <h2>管理员概览</h2>
          <span>平台核心数据</span>
        </div>

        <div class="summary-grid inner-grid">
          <div class="summary-card">
            <span class="label">学生用户</span>
            <strong>{{ adminSummary.totalStudents ?? 0 }}</strong>
          </div>
          <div class="summary-card">
            <span class="label">企业用户</span>
            <strong>{{ adminSummary.totalEnterprises ?? 0 }}</strong>
          </div>
          <div class="summary-card">
            <span class="label">在招岗位</span>
            <strong>{{ adminSummary.totalJobs ?? 0 }}</strong>
          </div>
        </div>

        <el-table :data="adminTrendRows" v-loading="loading" style="width: 100%">
          <el-table-column prop="date" label="日期" width="140" />
          <el-table-column prop="studentCount" label="新增学生" width="140" />
          <el-table-column prop="applicationCount" label="新增投递" width="140" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getDashboardStats } from '@/api/admin'
import { getEnterpriseAnalytics, getEnterpriseJobs } from '@/api/enterprise'
import { getDashboard, getRecommendations } from '@/api/student'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const adminSummary = ref({})
const studentSummary = ref({})
const enterpriseSummary = ref({})
const studentJobs = ref([])
const enterpriseJobs = ref([])

const isStudent = computed(() => userStore.userInfo.role === 'STUDENT')
const isEnterprise = computed(() => userStore.userInfo.role === 'ENTERPRISE')
const isAdmin = computed(() => userStore.userInfo.role === 'ADMIN')

const displayName = computed(() => userStore.userInfo.realName || userStore.userInfo.username || '用户')
const displayInitial = computed(() => displayName.value?.charAt(0) || '用')

const roleText = computed(() => {
  if (isStudent.value) return '学生'
  if (isEnterprise.value) return '企业'
  if (isAdmin.value) return '管理员'
  return '未知'
})

const formatPercent = (value) => `${Math.round(Number(value ?? 0))}%`
const formatCount = (value, unit = '项') => `${value ?? 0}${unit}`

const quickActions = computed(() => {
  if (isStudent.value) {
    return [
      { no: '01', title: '学生工作台', desc: '查看推荐岗位和投递概览', path: '/student/dashboard' },
      { no: '02', title: '岗位大厅', desc: '浏览并申请兼职岗位', path: '/student/jobs' },
      { no: '03', title: '简历完善', desc: '编辑简历与个人意向', path: '/student/profile-edit' }
    ]
  }
  if (isEnterprise.value) {
    return [
      { no: '01', title: '岗位管理', desc: '新增、编辑和维护岗位', path: '/enterprise/jobs' },
      { no: '02', title: '人才库', desc: '搜索可公开的学生简历', path: '/enterprise/talents' },
      { no: '03', title: '基础资料', desc: '维护企业基本信息', path: '/profile' }
    ]
  }
  return [
    { no: '01', title: '管理员看板', desc: '查看平台统计数据', path: '/admin/dashboard' },
    { no: '02', title: '用户管理', desc: '查看并管理账号状态', path: '/admin/users' },
    { no: '03', title: '匹配参数', desc: '调整匹配算法权重', path: '/admin/match-settings' }
  ]
})

const primaryAction = computed(() => quickActions.value[0] || { title: '首页', path: '/home' })

const heroIntro = computed(() => {
  if (isStudent.value) return '推荐岗位、简历进度和常用入口都集中在首页，回来后可以更快继续求职。'
  if (isEnterprise.value) return '岗位管理、人才筛选和资料维护汇总在同一个首页，让招聘流程更顺手。'
  if (isAdmin.value) return '把平台数据、管理入口和趋势信息放在统一首页里，减少来回切页。'
  return '首页会根据账号角色显示不同的中文功能入口。'
})

const heroStats = computed(() => {
  if (isStudent.value) {
    return [
      { label: '简历完整度', value: formatPercent(studentSummary.value.profileCompleteness), note: '越完整推荐越准确' },
      { label: '推荐岗位', value: formatCount(studentSummary.value.recommendationCount, '个'), note: '首页可继续查看' },
      { label: '已投递', value: formatCount(studentSummary.value.totalApplications, '次'), note: '记录你的进度' }
    ]
  }
  if (isEnterprise.value) {
    return [
      { label: '在招岗位', value: formatCount(enterpriseSummary.value.activeJobs, '个'), note: '当前招聘规模' },
      { label: '累计申请', value: formatCount(enterpriseSummary.value.totalApplications, '份'), note: '候选人持续汇入' },
      { label: '高匹配人才', value: formatCount(enterpriseSummary.value.matchedTalents, '位'), note: '适合优先联系' }
    ]
  }
  return [
    { label: '学生用户', value: formatCount(adminSummary.value.totalStudents, '人'), note: '平台学生侧规模' },
    { label: '企业用户', value: formatCount(adminSummary.value.totalEnterprises, '家'), note: '企业入驻情况' },
    { label: '在招岗位', value: formatCount(adminSummary.value.totalJobs, '个'), note: '当前活跃岗位' }
  ]
})

const heroSlides = computed(() => {
  if (isStudent.value) {
    return [
      {
        tag: '智能推荐',
        title: '把适合你的兼职放到首页中间',
        desc: '根据简历与意向匹配岗位，减少无效浏览，让首页更像一个求职入口页。',
        pills: ['推荐优先', '中文界面', '一键查看岗位'],
        metrics: [
          { label: '推荐岗位', value: formatCount(studentSummary.value.recommendationCount, '个') },
          { label: '简历完整度', value: formatPercent(studentSummary.value.profileCompleteness) }
        ],
        bg: 'linear-gradient(135deg, #0f766e 0%, #155e75 50%, #0b132b 100%)',
        accent: '#f4c95d'
      },
      {
        tag: '进度反馈',
        title: '投递和简历状态一眼就能看到',
        desc: '首页保留关键数字和高频入口，回到系统后能立刻知道下一步该做什么。',
        pills: ['继续投递', '完善简历', '减少跳转'],
        metrics: [
          { label: '已投递', value: formatCount(studentSummary.value.totalApplications, '次') },
          { label: '快捷入口', value: formatCount(quickActions.value.length, '个') }
        ],
        bg: 'linear-gradient(135deg, #3d405b 0%, #457b9d 48%, #2a9d8f 100%)',
        accent: '#f28482'
      }
    ]
  }
  if (isEnterprise.value) {
    return [
      {
        tag: '招聘效率',
        title: '岗位、人才和候选人线索集中展示',
        desc: '企业账号进入系统后，能直接从首页进入招聘主线，不再只有单调表单。',
        pills: ['岗位管理', '人才库', '候选人列表'],
        metrics: [
          { label: '在招岗位', value: formatCount(enterpriseSummary.value.activeJobs, '个') },
          { label: '高匹配人才', value: formatCount(enterpriseSummary.value.matchedTalents, '位') }
        ],
        bg: 'linear-gradient(135deg, #0b3c49 0%, #127475 52%, #ed6a5a 100%)',
        accent: '#ffd166'
      },
      {
        tag: '企业工作台',
        title: '首页先讲重点，再承接详细操作',
        desc: '轮播区负责突出重点，下方保留数据和功能入口，页面层次会更完整。',
        pills: ['重点前置', '视觉更强', '操作更顺'],
        metrics: [
          { label: '累计申请', value: formatCount(enterpriseSummary.value.totalApplications, '份') },
          { label: '快捷入口', value: formatCount(quickActions.value.length, '个') }
        ],
        bg: 'linear-gradient(135deg, #1f2937 0%, #0f766e 52%, #f4a261 100%)',
        accent: '#fef3c7'
      }
    ]
  }
  return [
    {
      tag: '平台运营',
      title: '管理员首页先看到关键平台数据',
      desc: '把核心数字做成轮播 Banner，后台首页会更像工作台，而不是单纯数据列表。',
      pills: ['核心数据', '用户管理', '参数配置'],
      metrics: [
        { label: '学生用户', value: formatCount(adminSummary.value.totalStudents, '人') },
        { label: '企业用户', value: formatCount(adminSummary.value.totalEnterprises, '家') }
      ],
      bg: 'linear-gradient(135deg, #111827 0%, #1d4ed8 45%, #0f766e 100%)',
      accent: '#fbbf24'
    },
    {
      tag: '后台氛围',
      title: '减少只有卡片和表格的单调感',
      desc: '一个轮播区负责吸引视线，一个数据区负责承接详情，首页层次会更清楚。',
      pills: ['看板优先', '信息分层', '中文后台'],
      metrics: [
        { label: '在招岗位', value: formatCount(adminSummary.value.totalJobs, '个') },
        { label: '快捷入口', value: formatCount(quickActions.value.length, '个') }
      ],
      bg: 'linear-gradient(135deg, #2b2d42 0%, #355070 48%, #2a9d8f 100%)',
      accent: '#ffb4a2'
    }
  ]
})

const adminTrendRows = computed(() => {
  const dates = adminSummary.value.trendDays || []
  const students = adminSummary.value.trendStudentData || []
  const apps = adminSummary.value.trendAppData || []
  return dates.map((date, index) => ({
    date,
    studentCount: students[index] ?? 0,
    applicationCount: apps[index] ?? 0
  }))
})

const loadHomeData = async () => {
  loading.value = true
  try {
    if (isStudent.value) {
      studentSummary.value = await getDashboard()
      const jobs = await getRecommendations({ page: 1, size: 5 })
      studentJobs.value = jobs.content || []
    } else if (isEnterprise.value) {
      enterpriseSummary.value = await getEnterpriseAnalytics()
      const jobs = await getEnterpriseJobs({ page: 1, size: 5 })
      enterpriseJobs.value = jobs.content || []
    } else if (isAdmin.value) {
      adminSummary.value = await getDashboardStats()
    }
  } finally {
    loading.value = false
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  loadHomeData()
})
</script>

<style scoped>
.page {
  --deep: #123b3d;
  --mid: #0f766e;
  --warm: #f59e0b;
  min-height: 100vh;
  padding: 36px 20px 48px;
  background:
    radial-gradient(circle at top left, rgba(15, 118, 110, 0.18), transparent 30%),
    radial-gradient(circle at top right, rgba(245, 158, 11, 0.1), transparent 26%),
    linear-gradient(180deg, #f6fbfb 0%, #edf3f2 100%);
}

.shell {
  max-width: 1240px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero {
  display: grid;
  grid-template-columns: minmax(320px, 0.95fr) minmax(0, 1.35fr);
  gap: 20px;
}

.hero-main,
.hero-banner,
.panel {
  border-radius: 26px;
  box-shadow: 0 24px 60px rgba(18, 38, 43, 0.12);
}

.hero-main {
  position: relative;
  overflow: hidden;
  padding: 28px;
  color: #f5fbfb;
  background:
    radial-gradient(circle at 20% 20%, rgba(244, 201, 93, 0.15), transparent 24%),
    linear-gradient(160deg, var(--deep) 0%, #11484a 52%, #0d2d2f 100%);
}

.hero-main::after {
  content: "";
  position: absolute;
  right: -48px;
  bottom: -48px;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
}

.hero-user {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.hero-avatar {
  border: 2px solid rgba(255, 255, 255, 0.16);
  background: linear-gradient(135deg, rgba(244, 201, 93, 0.96), rgba(255, 255, 255, 0.9));
  color: #184042;
  font-size: 24px;
  font-weight: 700;
}

.eyebrow,
.banner-tip {
  margin-bottom: 6px;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.eyebrow {
  color: rgba(255, 255, 255, 0.72);
}

.hero-main h1,
.banner-head h2,
.banner-card h3,
.panel-header h2,
.action-title {
  font-family: "Trebuchet MS", "PingFang SC", "Microsoft YaHei", sans-serif;
}

.hero-main h1 {
  font-size: 34px;
  line-height: 1.15;
}

.desc {
  position: relative;
  z-index: 1;
  margin-bottom: 18px;
  color: rgba(245, 251, 251, 0.84);
}

.hero-tags,
.hero-actions,
.banner-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-tags {
  position: relative;
  z-index: 1;
  margin-bottom: 20px;
}

.hero-tag,
.banner-pills span {
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  font-size: 12px;
}

.hero-tag {
  background: rgba(255, 255, 255, 0.08);
}

.hero-actions {
  position: relative;
  z-index: 1;
  margin-bottom: 20px;
}

.hero-stats {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.hero-stat,
.metric-box {
  padding: 14px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(12px);
}

.hero-stat span,
.metric-box span,
.label,
.panel-header span,
.banner-head span {
  color: var(--color-text-secondary);
  font-size: 12px;
}

.hero-stat span,
.metric-box span {
  color: rgba(255, 255, 255, 0.72);
  display: block;
  margin-bottom: 8px;
}

.hero-stat strong,
.metric-box strong {
  display: block;
  margin-bottom: 4px;
  font-size: 22px;
  line-height: 1.15;
}

.hero-stat p {
  color: rgba(245, 251, 251, 0.72);
  font-size: 12px;
}

.hero-banner,
.panel {
  padding: 20px;
  background: rgba(255, 255, 255, 0.95);
}

.banner-head,
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.banner-tip {
  color: var(--mid);
}

.banner-head h2,
.panel-header h2 {
  font-size: 24px;
  color: #173638;
}

.banner-card {
  position: relative;
  overflow: hidden;
  height: 318px;
  padding: 24px;
  border-radius: 24px;
  color: #fff;
  background: var(--banner-bg);
}

.banner-card::before,
.banner-card::after {
  content: "";
  position: absolute;
  border-radius: 50%;
}

.banner-card::before {
  right: 22px;
  top: 20px;
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.16) 0%, transparent 70%);
}

.banner-card::after {
  right: 120px;
  bottom: 18px;
  width: 84px;
  height: 84px;
  background: radial-gradient(circle, var(--banner-accent) 0%, transparent 70%);
  opacity: 0.36;
}

.banner-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(220px, 0.8fr);
  gap: 16px;
  height: 100%;
}

.banner-tag {
  display: inline-block;
  margin-bottom: 12px;
  padding: 7px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.12);
  font-size: 12px;
  letter-spacing: 0.08em;
}

.banner-card h3 {
  max-width: 460px;
  margin-bottom: 10px;
  font-size: 30px;
  line-height: 1.15;
}

.banner-card p {
  max-width: 460px;
  color: rgba(255, 255, 255, 0.84);
}

.banner-pills {
  margin-top: 18px;
}

.banner-metrics {
  display: grid;
  gap: 12px;
  align-content: end;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.inner-grid {
  margin-bottom: 18px;
}

.summary-card,
.action-card {
  position: relative;
  overflow: hidden;
  padding: 20px;
  border-radius: 22px;
  border: 1px solid rgba(15, 118, 110, 0.12);
}

.summary-card {
  background:
    radial-gradient(circle at top right, rgba(15, 118, 110, 0.12), transparent 32%),
    linear-gradient(180deg, #f8fcfb 0%, #f3f8f8 100%);
}

.label {
  display: block;
  margin-bottom: 10px;
}

.summary-card strong {
  font-size: 28px;
  color: #173638;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.action-card {
  cursor: pointer;
  background:
    radial-gradient(circle at top right, rgba(245, 158, 11, 0.12), transparent 28%),
    linear-gradient(135deg, rgba(15, 118, 110, 0.08), rgba(15, 118, 110, 0.02));
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 32px rgba(15, 118, 110, 0.14);
}

.action-no {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  margin-bottom: 16px;
  border-radius: 14px;
  background: #173638;
  color: #f8fbfb;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.action-title {
  margin-bottom: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #173638;
}

.action-desc {
  color: var(--color-text-secondary);
  font-size: 14px;
}

:deep(.carousel .el-carousel__container) {
  border-radius: 24px;
}

:deep(.carousel .el-carousel__arrow) {
  background: rgba(17, 24, 39, 0.28);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

:deep(.carousel .el-carousel__indicator button) {
  width: 24px;
  height: 4px;
  border-radius: 999px;
  background: rgba(23, 54, 56, 0.24);
}

:deep(.carousel .el-carousel__indicator.is-active button) {
  background: linear-gradient(90deg, #0f766e, #f59e0b);
}

@media (max-width: 1080px) {
  .hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .page {
    padding: 24px 14px 36px;
  }

  .hero-main,
  .hero-banner,
  .panel {
    padding: 18px;
    border-radius: 22px;
  }

  .hero-actions,
  .hero-stats,
  .summary-grid,
  .action-grid,
  .banner-grid {
    grid-template-columns: 1fr;
  }

  .hero-actions,
  .banner-head,
  .panel-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-actions {
    align-items: stretch;
  }

  .banner-card {
    height: auto;
    min-height: 380px;
  }
}
</style>
