<template>
  <div class="page">
    <div class="panel">
      <div class="header">
        <div>
          <h1>管理员看板</h1>
          <p>查看平台用户、岗位和投递的整体情况。</p>
        </div>
        <div class="actions">
          <el-button @click="router.push('/home')">返回首页</el-button>
          <el-button type="primary" @click="router.push('/admin/users')">用户管理</el-button>
        </div>
      </div>

      <div class="cards" v-loading="loading">
        <div class="card">
          <span>学生用户</span>
          <strong>{{ stats.totalStudents ?? 0 }}</strong>
        </div>
        <div class="card">
          <span>企业用户</span>
          <strong>{{ stats.totalEnterprises ?? 0 }}</strong>
        </div>
        <div class="card">
          <span>在招岗位</span>
          <strong>{{ stats.totalJobs ?? 0 }}</strong>
        </div>
        <div class="card">
          <span>累计投递</span>
          <strong>{{ stats.totalApplications ?? 0 }}</strong>
        </div>
      </div>

      <div class="sub-panel">
        <div class="sub-header">
          <h2>最近 7 天趋势</h2>
          <el-button type="primary" link @click="router.push('/admin/match-settings')">匹配参数设置</el-button>
        </div>
        <el-table :data="trendRows" v-loading="loading" style="width: 100%">
          <el-table-column prop="date" label="日期" width="160" />
          <el-table-column prop="studentCount" label="新增学生" width="160" />
          <el-table-column prop="applicationCount" label="新增投递" width="160" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboardStats } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const stats = ref({})

const trendRows = computed(() => {
  const dates = stats.value.trendDays || []
  const students = stats.value.trendStudentData || []
  const applications = stats.value.trendAppData || []
  return dates.map((date, index) => ({
    date,
    studentCount: students[index] ?? 0,
    applicationCount: applications[index] ?? 0
  }))
})

const loadData = async () => {
  loading.value = true
  try {
    stats.value = await getDashboardStats()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page {
  padding: 32px 20px;
}

.panel,
.sub-panel {
  max-width: 1100px;
  margin: 0 auto;
  background: #fff;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 10px 40px rgba(27, 44, 51, 0.08);
}

.sub-panel {
  margin-top: 20px;
}

.header,
.sub-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.header h1,
.sub-header h2 {
  margin-bottom: 6px;
}

.header p {
  color: var(--color-text-secondary);
}

.actions {
  display: flex;
  gap: 12px;
}

.cards {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.card {
  padding: 20px;
  border-radius: 18px;
  background: #f7fbfb;
  border: 1px solid rgba(0, 166, 167, 0.12);
}

.card span {
  display: block;
  color: var(--color-text-secondary);
  margin-bottom: 10px;
}

.card strong {
  font-size: 28px;
}

@media (max-width: 900px) {
  .cards {
    grid-template-columns: 1fr;
  }

  .header,
  .sub-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
