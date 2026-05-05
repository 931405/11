<template>
  <div class="page">
    <div class="panel">
      <div class="header">
        <div>
          <h1>学生工作台</h1>
          <p>查看简历状态、投递概览和推荐岗位。</p>
        </div>
        <div class="actions">
          <el-button @click="router.push('/home')">返回首页</el-button>
          <el-button type="primary" @click="router.push('/student/profile-edit')">完善简历</el-button>
        </div>
      </div>

      <div class="cards" v-loading="loading">
        <div class="card">
          <span>简历完整度</span>
          <strong>{{ summary.profileCompleteness ?? 0 }}%</strong>
        </div>
        <div class="card">
          <span>已投递岗位</span>
          <strong>{{ summary.totalApplications ?? 0 }}</strong>
        </div>
        <div class="card">
          <span>待处理投递</span>
          <strong>{{ summary.pendingApplications ?? 0 }}</strong>
        </div>
        <div class="card">
          <span>推荐岗位</span>
          <strong>{{ summary.recommendationCount ?? 0 }}</strong>
        </div>
      </div>

      <div class="sub-panel">
        <div class="sub-header">
          <h2>推荐岗位</h2>
          <el-button type="primary" link @click="router.push('/student/jobs')">查看全部岗位</el-button>
        </div>

        <el-table :data="jobs" v-loading="loading" style="width: 100%">
          <el-table-column prop="title" label="岗位名称" min-width="220" />
          <el-table-column prop="companyName" label="企业" min-width="180" />
          <el-table-column prop="workLocation" label="地点" min-width="150" />
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
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboard, getRecommendations } from '@/api/student'

const router = useRouter()
const loading = ref(false)
const summary = ref({})
const jobs = ref([])

const loadData = async () => {
  loading.value = true
  try {
    summary.value = await getDashboard()
    const data = await getRecommendations({ page: 1, size: 8 })
    jobs.value = data.content || []
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
  max-width: 1180px;
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
  .header,
  .sub-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .actions {
    width: 100%;
    flex-direction: column;
  }

  .cards {
    grid-template-columns: 1fr;
  }
}
</style>
