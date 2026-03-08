<template>
  <div class="job-list-container">
    <div class="search-bar">
      <el-card shadow="never" class="search-card">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" placeholder="职位名称/公司/描述" clearable style="width: 200px" />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="searchForm.categoryId" placeholder="全部" clearable style="width: 150px">
              <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="工作地点">
            <el-input v-model="searchForm.location" placeholder="如：北京/朝阳" clearable style="width: 150px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon> 搜索
            </el-button>
            <el-button @click="resetSearch">
              <el-icon><Refresh /></el-icon> 重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <div class="list-content" v-loading="loading">
      <el-empty v-if="total === 0" description="未找到匹配的职位" />
      
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
              <div class="company-industry">{{ job.categoryName }} <span class="divider">|</span> {{ job.applyCount }}人申请</div>
            </div>
          </div>
          
          <div class="job-banner">
            <div class="job-desc">{{ truncateText(job.requirements, 30) }}</div>
            <div class="job-is-applied" v-if="job.isApplied"><el-icon><Check /></el-icon> 已投递</div>
          </div>
        </el-card>
      </div>

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="searchForm.page"
          v-model:page-size="searchForm.size"
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
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { searchJobs, getCategories } from '@/api/job'
import { useAppStore } from '@/stores/app'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const loading = ref(false)
const jobs = ref([])
const total = ref(0)
const categories = ref([])

const searchForm = reactive({
  keyword: '',
  categoryId: '',
  location: appStore.currentLocation === '全国' ? '' : appStore.currentLocation,
  enterpriseId: '',
  page: 1,
  size: 10
})

watch(
  () => appStore.currentLocation,
  (newLoc) => {
    searchForm.location = newLoc === '全国' ? '' : newLoc
    searchForm.page = 1
    fetchJobs()
  }
)

const fetchCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch (error) {
    console.error('Failed to load categories', error)
  }
}

const fetchJobs = async () => {
  loading.value = true
  try {
    const params = { ...searchForm }
    // Clean up empty params
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null) {
        delete params[key]
      }
    })
    const res = await searchJobs(params)
    jobs.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to load jobs', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  searchForm.page = 1
  fetchJobs()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.categoryId = ''
  searchForm.enterpriseId = ''
  searchForm.location = appStore.currentLocation === '全国' ? '' : appStore.currentLocation
  searchForm.page = 1
  fetchJobs()
}

const handleSizeChange = (val) => {
  searchForm.size = val
  fetchJobs()
}

const handleCurrentChange = (val) => {
  searchForm.page = val
  fetchJobs()
}

const goToDetail = (id) => {
  router.push(`/student/jobs/${id}`)
}

const truncateText = (text, length) => {
  if (!text) return '暂无要求说明'
  if (text.length <= length) return text
  return text.substring(0, length) + '...'
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return dayjs(dateString).format('MM-DD')
}

onMounted(() => {
  // If navigated with a keyword from the header search or dashboard
  if (route.query.keyword) {
    searchForm.keyword = route.query.keyword
  }
  if (route.query.enterpriseId) {
    searchForm.enterpriseId = route.query.enterpriseId
  }
  fetchCategories()
  fetchJobs()
})
</script>

<style scoped>
.job-list-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.search-card {
  border-radius: 8px;
  border-color: transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.search-form .el-form-item {
  margin-bottom: 0;
  margin-right: 20px;
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

.divider {
  margin: 0 4px;
  color: var(--color-border);
}

.job-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(90deg, #f5fcfc, #fff);
  padding: 8px 12px;
  border-radius: 6px;
}

.job-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.job-is-applied {
  font-size: 12px;
  color: var(--color-success);
  display: flex;
  align-items: center;
  gap: 2px;
  white-space: nowrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>
