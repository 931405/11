<template>
  <div class="page">
    <div class="panel">
      <div class="header">
        <div>
          <h1>岗位大厅</h1>
          <p>可按关键字、分类和地点筛选岗位。</p>
        </div>
        <el-button @click="router.push('/student/dashboard')">返回工作台</el-button>
      </div>

      <el-form :inline="true" class="search-form">
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="岗位名称 / 企业名称" clearable />
        </el-form-item>
        <el-form-item label="岗位分类">
          <el-select v-model="query.categoryId" placeholder="全部" clearable style="width: 180px">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作地点">
          <el-input v-model="query.location" placeholder="例如：上海、远程" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="jobs" v-loading="loading">
      <el-empty v-if="jobs.length === 0" description="暂无符合条件的岗位" />
      <div v-else class="job-grid">
        <div v-for="job in jobs" :key="job.id" class="job-card" @click="router.push(`/student/jobs/${job.id}`)">
          <div class="job-top">
            <div>
              <h3>{{ job.title }}</h3>
              <p>{{ job.companyName || '未命名企业' }}</p>
            </div>
            <strong>{{ salaryText(job) }}</strong>
          </div>
          <div class="meta">
            <span>{{ job.workLocation || '地点待定' }}</span>
            <span>{{ job.categoryName || '未分类' }}</span>
            <span v-if="job.matchScore">匹配 {{ Math.round(job.matchScore) }}%</span>
          </div>
          <p class="desc">{{ truncate(job.requirements || job.description) }}</p>
        </div>
      </div>
    </div>

    <div class="pager" v-if="total > 0">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :page-sizes="[6, 12, 24]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="loadJobs"
        @current-change="loadJobs"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategories, searchJobs } from '@/api/job'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const categories = ref([])
const jobs = ref([])
const total = ref(0)

const query = reactive({
  keyword: '',
  categoryId: '',
  location: '',
  page: 1,
  size: 6
})

const salaryText = (job) => {
  if (job.salaryMin == null && job.salaryMax == null) return '面议'
  return `${job.salaryMin ?? '-'} - ${job.salaryMax ?? '-'} 元/小时`
}

const truncate = (text) => {
  if (!text) return '暂无岗位描述'
  return text.length > 72 ? `${text.slice(0, 72)}...` : text
}

const loadCategories = async () => {
  categories.value = await getCategories()
}

const loadJobs = async () => {
  loading.value = true
  try {
    const data = await searchJobs({
      keyword: query.keyword || undefined,
      categoryId: query.categoryId || undefined,
      location: query.location || undefined,
      page: query.page,
      size: query.size
    })
    jobs.value = data.content || []
    total.value = data.totalElements || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  loadJobs()
}

const handleReset = () => {
  query.keyword = ''
  query.categoryId = ''
  query.location = ''
  query.page = 1
  loadJobs()
}

onMounted(() => {
  if (route.query.keyword) query.keyword = String(route.query.keyword)
  loadCategories()
  loadJobs()
})
</script>

<style scoped>
.page {
  padding: 32px 20px;
}

.panel {
  max-width: 1180px;
  margin: 0 auto 20px;
  background: #fff;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 10px 40px rgba(27, 44, 51, 0.08);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 18px;
}

.header p {
  color: var(--color-text-secondary);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.jobs {
  max-width: 1180px;
  margin: 0 auto;
}

.job-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.job-card {
  background: #fff;
  border-radius: 20px;
  padding: 22px;
  box-shadow: 0 10px 32px rgba(27, 44, 51, 0.08);
  cursor: pointer;
  transition: transform 0.2s ease;
}

.job-card:hover {
  transform: translateY(-2px);
}

.job-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.job-top h3 {
  margin-bottom: 4px;
}

.job-top p {
  color: var(--color-text-secondary);
}

.job-top strong {
  color: var(--color-danger);
  white-space: nowrap;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.meta span {
  background: #f7fbfb;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.desc {
  color: var(--color-text-secondary);
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 900px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
  }

  .job-grid {
    grid-template-columns: 1fr;
  }
}
</style>
