<template>
  <div class="enterprise-dashboard">
    <div class="welcome-header">
      <h2>你好，{{ userStore.userInfo.companyName || userStore.userInfo.realName }}！</h2>
      <p class="subtitle">欢迎使用兼职匹配平台企业版，今日系统为您精准推荐合适的人才</p>
    </div>

    <!-- Alert for uncertified enterprises -->
    <el-alert
      v-if="info && info.certificationStatus === 'REJECTED'"
      title="企业资质审核已被驳回"
      type="error"
      show-icon
      class="mb-24"
    >
      <template #default>
        <div>
          驳回原因: {{ info.auditRemark || '资料不全' }}
          <el-button type="primary" link @click="$router.push('/enterprise/profile')">重新提交资质</el-button>
        </div>
      </template>
    </el-alert>
    
    <el-alert
      v-else-if="info && info.certificationStatus === 'PENDING'"
      title="企业资质审核中"
      type="warning"
      description="您的企业资质正在审核中，在此期间您无法发布新的职位，请耐心等待或联系管理员。"
      show-icon
      class="mb-24"
    />
    
    <el-alert
      v-else-if="info && !info.certificationStatus"
      title="未提交企业资质"
      type="info"
      show-icon
      class="mb-24"
    >
      <template #default>
        <div>
          您还未提交企业认证资料，完成认证后方可发布职位。
          <el-button type="primary" link @click="$router.push('/enterprise/profile')">去认证</el-button>
        </div>
      </template>
    </el-alert>

    <div class="stats-cards">
      <el-card shadow="hover" class="stat-card" @click="$router.push('/enterprise/jobs')">
        <div class="stat-content">
          <div class="stat-icon jobs">
            <el-icon><Suitcase /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.activeJobs }}</div>
            <div class="stat-label">在招职位</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon candidates">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.newApplications }}</div>
            <div class="stat-label">待处理简历</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card" @click="$router.push('/enterprise/profile')">
        <div class="stat-content">
          <div class="stat-icon certification">
            <el-icon><Stamp /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value status-text" :class="getCertClass(info?.certificationStatus)">
              {{ getCertLabel(info?.certificationStatus) }}
            </div>
            <div class="stat-label">认证状态</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- Shortcut Actions -->
    <div class="section-title">
      <h3>
        <el-icon><Lightning /></el-icon> 快捷操作
      </h3>
    </div>
    
    <div class="shortcuts-grid">
      <el-card shadow="hover" class="shortcut-card" @click="$router.push('/enterprise/jobs/create')">
        <el-icon class="shortcut-icon"><DocumentAdd /></el-icon>
        <span class="shortcut-label">发布新职位</span>
      </el-card>
      
      <el-card shadow="hover" class="shortcut-card" @click="$router.push('/enterprise/jobs')">
        <el-icon class="shortcut-icon"><List /></el-icon>
        <span class="shortcut-label">职位管理</span>
      </el-card>
      
      <el-card shadow="hover" class="shortcut-card" @click="$router.push('/enterprise/profile')">
        <el-icon class="shortcut-icon"><OfficeBuilding /></el-icon>
        <span class="shortcut-label">完善主页</span>
      </el-card>
    </div>

    <!-- Charts Section -->
    <div class="section-title">
      <h3><el-icon><DataAnalysis /></el-icon> 数据概览</h3>
    </div>
    <div class="charts-row">
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="chart-card-header">
            <span>近7日投递趋势</span>
          </div>
        </template>
        <div ref="lineChartRef" class="chart-container"></div>
      </el-card>
      
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="chart-card-header">
            <span>职位状态分布</span>
          </div>
        </template>
        <div ref="pieChartRef" class="chart-container"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getEnterpriseInfo, getEnterpriseJobs, getDailyApplicationTrend } from '@/api/enterprise'
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()

const info = ref(null)
const loading = ref(false)
const stats = ref({
  activeJobs: 0,
  newApplications: 0
})

// Chart refs
const lineChartRef = ref(null)
const pieChartRef = ref(null)
let lineChart = null
let pieChart = null

const fetchDashboardData = async () => {
  loading.value = true
  try {
    const [infoRes, jobsRes] = await Promise.all([
      getEnterpriseInfo(),
      getEnterpriseJobs({ page: 1, size: 50, status: 'OPEN' })
    ])
    
    info.value = infoRes
    stats.value.activeJobs = jobsRes.totalElements || 0
    
    let totalApps = 0
    let openCount = 0
    let closedCount = 0
    if (jobsRes.content) {
      jobsRes.content.forEach(job => {
        totalApps += (job.applyCount || 0)
        if (job.status === 'OPEN') openCount++
        else closedCount++
      })
    }
    stats.value.newApplications = totalApps

    // Fetch real trend data
    const trendRes = await getDailyApplicationTrend()
    const trendData = trendRes?.data || trendRes || { dates: [], counts: [] }

    // Initialize charts after data is loaded
    await nextTick()
    initLineChart(trendData.dates, trendData.counts)
    initPieChart(openCount, closedCount)

  } catch (error) {
    console.error('Failed to load enterprise dashboard', error)
  } finally {
    loading.value = false
  }
}

const initLineChart = (days, data) => {
  if (!lineChartRef.value) return
  lineChart = echarts.init(lineChartRef.value)

  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: days, boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '投递量',
      type: 'line',
      data: data,
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0,166,167,0.35)' },
          { offset: 1, color: 'rgba(0,166,167,0.05)' }
        ])
      },
      lineStyle: { color: '#00a6a7', width: 2 },
      itemStyle: { color: '#00a6a7' }
    }]
  })
}

const initPieChart = (openCount, closedCount) => {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)
  
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, itemWidth: 10, itemHeight: 10 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: [
        { value: openCount, name: '招聘中', itemStyle: { color: '#00a6a7' } },
        { value: closedCount, name: '已关闭', itemStyle: { color: '#909399' } }
      ]
    }]
  })
}

const getCertLabel = (status) => {
  const map = {
    'APPROVED': '已认证',
    'PENDING': '审核中',
    'REJECTED': '已驳回'
  }
  return map[status] || '未认证'
}

const getCertClass = (status) => {
  const map = {
    'APPROVED': 'color-success',
    'PENDING': 'color-warning',
    'REJECTED': 'color-danger'
  }
  return map[status] || 'color-muted'
}

// Resize handler
const handleResize = () => {
  lineChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  fetchDashboardData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  lineChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.enterprise-dashboard {
  display: flex;
  flex-direction: column;
}

.welcome-header {
  margin-bottom: 24px;
}

.welcome-header h2 {
  color: var(--color-text-primary);
  margin-bottom: 8px;
  font-weight: 600;
}

.subtitle {
  color: var(--color-text-secondary);
  font-size: 14px;
}

.mb-24 {
  margin-bottom: 24px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  cursor: pointer;
  border-radius: 8px;
  border-color: var(--color-border);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.jobs {
  background-color: rgba(107, 143, 163, 0.1);
  color: var(--color-accent);
}

.stat-icon.candidates {
  background-color: rgba(123, 163, 123, 0.1);
  color: var(--color-success);
}

.stat-icon.certification {
  background-color: rgba(196, 163, 90, 0.1);
  color: var(--color-warning);
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: var(--color-text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-muted);
  margin-top: 4px;
}

.status-text {
  font-size: 20px;
}

.color-success { color: var(--color-success); }
.color-warning { color: var(--color-warning); }
.color-danger { color: var(--color-danger); }
.color-muted { color: var(--color-text-muted); }

.section-title {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.section-title h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-primary);
  font-weight: 500;
}

.section-title h3 .el-icon {
  color: var(--color-accent);
}

.shortcuts-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.shortcut-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.shortcut-card:hover {
  transform: translateY(-2px);
  color: var(--color-accent);
}

.shortcut-icon {
  font-size: 20px;
  color: var(--color-accent);
}

.shortcut-label {
  font-size: 14px;
}

/* --- Charts --- */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.chart-card {
  border-radius: 8px;
}

.chart-card-header {
  font-weight: 600;
  font-size: 15px;
  color: var(--color-text-primary);
}

.chart-container {
  width: 100%;
  height: 280px;
}
</style>
