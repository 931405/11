<template>
  <div class="admin-dashboard">
    <div class="welcome-header">
      <h2>你好，超级管理员！</h2>
      <p class="subtitle">兼职匹配平台数据监控驾驶舱</p>
    </div>

    <!-- Top Key Metrics Cards -->
    <div class="stats-cards" v-loading="loading">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon students">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalStudents || 0 }}</div>
            <div class="stat-label">注册学生</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon enterprises">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalEnterprises || 0 }}</div>
            <div class="stat-label">入驻企业</div>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon jobs">
            <el-icon><Briefcase /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalJobs || 0 }}</div>
            <div class="stat-label">在招职位总数</div>
          </div>
        </div>
      </el-card>
      
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon match">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalApplications || 0 }}</div>
            <div class="stat-label">累计投递次数</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- Operations & Alerts Board -->
    <div class="dashboard-columns">
      <!-- Left Column: Pending Actions -->
      <div class="column">
        <el-card shadow="never" class="board-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><Bell /></el-icon> 待办事项</span>
            </div>
          </template>
          
          <div class="todo-list">
            <div class="todo-item" @click="$router.push('/admin/audits')">
              <div class="todo-label">
                <el-icon class="todo-icon warning"><Stamp /></el-icon> 
                待审核企业资质
              </div>
              <div class="todo-count">
                <el-tag type="danger" round v-if="stats.pendingAudits > 0">{{ stats.pendingAudits }} 条待处理</el-tag>
                <el-tag type="info" round v-else>暂无待办</el-tag>
              </div>
            </div>
            
            <el-divider border-style="dashed" />
            
            <div class="todo-item" @click="$router.push('/admin/feedback')">
              <div class="todo-label">
                <el-icon class="todo-icon info"><ChatLineSquare /></el-icon> 
                尚未回复的用户反馈
              </div>
              <div class="todo-count">
                <el-tag type="warning" round v-if="stats.pendingFeedback > 0">{{ stats.pendingFeedback }} 条未读</el-tag>
                <el-tag type="info" round v-else>暂无待办</el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- Right Column: Quick Links -->
      <div class="column">
        <el-card shadow="never" class="board-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><Setting /></el-icon> 系统配置</span>
            </div>
          </template>
          
          <div class="settings-grid">
            <div class="setting-item" @click="$router.push('/admin/users')">
              <div class="setting-icon"><el-icon><UserFilled /></el-icon></div>
              <div class="setting-name">用户状态管理</div>
            </div>
            <div class="setting-item" @click="$router.push('/admin/categories')">
              <div class="setting-icon"><el-icon><Files /></el-icon></div>
              <div class="setting-name">职位分类设置</div>
            </div>
            <div class="setting-item" @click="$router.push('/admin/match-settings')">
              <div class="setting-icon"><el-icon><Tools /></el-icon></div>
              <div class="setting-name">匹配算法参数</div>
            </div>
            <div class="setting-item" @click="$router.push('/admin/logs')">
              <div class="setting-icon"><el-icon><DataLine /></el-icon></div>
              <div class="setting-name">系统运行日志</div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="charts-row">
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="chart-card-header">
            <span><el-icon><TrendCharts /></el-icon> 平台近期活跃趋势</span>
          </div>
        </template>
        <div ref="barChartRef" class="chart-container"></div>
      </el-card>
      
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="chart-card-header">
            <span><el-icon><PieChart /></el-icon> 用户角色分布</span>
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
import { getDashboardStats } from '@/api/admin'
import * as echarts from 'echarts'

const router = useRouter()
const loading = ref(false)
const stats = ref({})

// Chart refs
const barChartRef = ref(null)
const pieChartRef = ref(null)
let barChart = null
let pieChart = null

const fetchStats = async () => {
  loading.value = true
  try {
    stats.value = await getDashboardStats()
    
    await nextTick()
    initBarChart()
    initPieChart()
  } catch (error) {
    console.error('Failed to load admin stats', error)
  } finally {
    loading.value = false
  }
}

const initBarChart = () => {
  if (!barChartRef.value) return
  barChart = echarts.init(barChartRef.value)
  
  const days = stats.value.trendDays || []
  const studentData = stats.value.trendStudentData || []
  const appData = stats.value.trendAppData || []

  barChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增注册', '新增投递'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '12%', containLabel: true },
    xAxis: { type: 'category', data: days },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '新增注册',
        type: 'bar',
        data: studentData,
        itemStyle: { color: '#5470c6', borderRadius: [4, 4, 0, 0] },
        barWidth: '35%'
      },
      {
        name: '新增投递',
        type: 'line',
        data: appData,
        smooth: true,
        lineStyle: { color: '#00a6a7', width: 2 },
        itemStyle: { color: '#00a6a7' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0,166,167,0.25)' },
            { offset: 1, color: 'rgba(0,166,167,0.02)' }
          ])
        }
      }
    ]
  })
}

const initPieChart = () => {
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
        { value: stats.value.totalStudents || 0, name: '学生', itemStyle: { color: '#5470c6' } },
        { value: stats.value.totalEnterprises || 0, name: '企业', itemStyle: { color: '#00a6a7' } },
        { value: stats.value.totalAdmins || 0, name: '管理员', itemStyle: { color: '#fc8452' } }
      ]
    }]
  })
}

const handleResize = () => {
  barChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.admin-dashboard {
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

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
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

.stat-icon.students { background-color: rgba(107, 143, 163, 0.1); color: var(--color-accent); }
.stat-icon.enterprises { background-color: rgba(123, 163, 123, 0.1); color: var(--color-success); }
.stat-icon.jobs { background-color: rgba(196, 163, 90, 0.1); color: var(--color-warning); }
.stat-icon.match { background-color: rgba(176, 112, 112, 0.1); color: var(--color-danger); }

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

.dashboard-columns {
  display: flex;
  gap: 20px;
}

.column {
  flex: 1;
}

.board-card {
  border-radius: 8px;
  border-color: var(--color-border);
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.todo-list {
  display: flex;
  flex-direction: column;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 8px;
  cursor: pointer;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.todo-item:hover {
  background-color: var(--color-bg-secondary);
}

.todo-label {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  color: var(--color-text-primary);
}

.todo-icon {
  font-size: 18px;
}
.todo-icon.warning { color: var(--color-warning); }
.todo-icon.info { color: var(--color-accent); }

.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  padding: 8px;
}

.setting-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  background-color: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.setting-item:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
  background-color: var(--color-bg-card);
}

.setting-item.disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.setting-item.disabled:hover {
  border-color: var(--color-border);
  color: inherit;
  background-color: var(--color-bg-primary);
}

.setting-icon {
  font-size: 28px;
  margin-bottom: 12px;
  color: var(--color-accent);
}

.setting-item.disabled .setting-icon {
  color: var(--color-text-muted);
}

.setting-name {
  font-size: 14px;
  color: var(--color-text-primary);
}

.disabled .setting-name {
  color: var(--color-text-muted);
}

.disabled .setting-icon {
  color: var(--color-text-muted);
}

/* --- Charts --- */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-top: 24px;
}

.chart-card {
  border-radius: 8px;
}

.chart-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
  color: var(--color-text-primary);
}

.chart-container {
  width: 100%;
  height: 280px;
}
</style>
