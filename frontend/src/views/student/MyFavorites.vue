<template>
  <div class="favorites-container">
    <div class="page-title">
      <h2>我收藏的职位</h2>
      <p class="subtitle">您保存的心仪兼职将会展示在这里</p>
    </div>

    <!-- Reusing similar layout to job listing -->
    <div class="list-content" v-loading="loading">
      <el-empty v-if="total === 0" description="您还没有收藏任何职位" />
      
      <div v-else class="jobs-grid">
        <el-card v-for="job in jobs" :key="job.id" class="job-card" shadow="hover" :body-style="{ padding: '20px' }" @click="goToDetail(job.id)">
          <div class="job-primary">
            <div class="job-top">
              <div class="job-title" :title="job.title">{{ job.title }}</div>
              <div class="job-salary">{{ job.salaryMin }}-{{ job.salaryMax }}<span class="salary-unit">元/时</span></div>
            </div>
            <div class="job-tags">
              <span class="tag">{{ job.workLocation }}</span>
              <span class="tag">{{ job.workSchedule }}</span>
              <span class="tag match-tag" v-if="job.matchScore">匹配 {{ Math.round(job.matchScore) }}%</span>
            </div>
          </div>
          
          <div class="job-company">
            <div class="company-avatar">
              <el-avatar :size="40" shape="square" style="background: var(--color-bg-secondary); color: var(--color-accent)">{{ job.companyName?.charAt(0) || '企' }}</el-avatar>
            </div>
            <div class="company-info">
              <div class="company-name">{{ job.companyName }}</div>
              <div class="company-industry">{{ job.categoryName }}</div>
            </div>
          </div>
          
          <div class="job-banner">
            <div class="fav-status">
              <el-icon color="#ff9f00"><StarFilled /></el-icon> <span style="font-size: 13px; color: var(--color-text-secondary); margin-left: 2px;">已收藏</span>
            </div>
            <el-button type="danger" link @click.stop="handleUnfavorite(job.id)" size="small">取消收藏</el-button>
          </div>
        </el-card>
      </div>

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFavorites, unfavoriteJob } from '@/api/student'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const jobs = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const fetchFavorites = async () => {
  loading.value = true
  try {
    const res = await getFavorites({ page: page.value, size: size.value })
    jobs.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to load favorites', error)
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  size.value = val
  fetchFavorites()
}

const handleCurrentChange = (val) => {
  page.value = val
  fetchFavorites()
}

const handleUnfavorite = (jobId) => {
  ElMessageBox.confirm(
    '确定要取消收藏该职位吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await unfavoriteJob(jobId)
      ElMessage.success('已取消收藏')
      fetchFavorites() // Reload list
    } catch (error) {
      // Handled globally
    }
  }).catch(() => {})
}

const goToDetail = (id) => {
  router.push(`/student/jobs/${id}`)
}

onMounted(() => {
  fetchFavorites()
})
</script>

<style scoped>
/* Reusing job list styles for consistency */
.page-title {
  margin-bottom: 24px;
}

.page-title h2 {
  color: var(--color-text-primary);
  margin-bottom: 8px;
  font-weight: 600;
}

.subtitle {
  color: var(--color-text-secondary);
  font-size: 14px;
}

.jobs-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.job-card {
  cursor: pointer;
  border-radius: 12px;
  border: none;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05) !important;
  transition: all 0.2s linear;
}

.job-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08) !important;
}

.job-primary {
  margin-bottom: 16px;
}

.job-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.job-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 60%;
  transition: color 0.2s;
}

.job-card:hover .job-title {
  color: var(--color-accent);
}

.job-salary {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-danger);
}

.salary-unit {
  font-size: 13px;
  font-weight: normal;
  margin-left: 2px;
}

.job-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  background-color: var(--color-bg-secondary);
  color: var(--color-text-secondary);
  font-size: 13px;
  padding: 2px 8px;
  border-radius: 4px;
}

.match-tag {
  background-color: rgba(0, 166, 167, 0.1);
  color: var(--color-accent);
}

.job-company {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px dashed var(--color-border);
  margin-bottom: 16px;
}

.company-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.company-name {
  font-size: 14px;
  color: var(--color-text-primary);
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.company-industry {
  font-size: 13px;
  color: var(--color-text-muted);
}

.job-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(90deg, #fef8f0, #fff);
  padding: 8px 12px;
  border-radius: 6px;
}

.fav-status {
  display: flex;
  align-items: center;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>
