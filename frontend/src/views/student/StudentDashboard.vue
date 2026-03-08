<template>
  <div class="dashboard-container">
    
    <!-- Top Search & Banner Area -->
    <div class="home-search-banner">
      <div class="search-tabs">
        <span v-for="tab in mainTabs" :key="tab" class="tab-btn" :class="{ active: activeMainTab === tab }" @click="handleTabClick(tab)">{{ tab }}</span>
      </div>
      <div class="search-box-large">
        <el-input v-model="searchKeyword" class="main-search-input" :placeholder="`搜索${activeMainTab}职位、公司`" @keyup.enter="handleSearch">
          <template #prepend>
            <el-select v-model="searchType" style="width: 100px" placeholder="职位类型">
              <el-option label="职位类型" value="job" />
              <el-option label="公司名称" value="company" />
            </el-select>
          </template>
          <template #append>
            <el-button color="#00a6a7" class="search-btn-large" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <!-- Hot Categories Area (Mockup similar to screenshot) -->
    <div class="hot-sections">
      <div class="hot-block flex-2">
        <div class="block-header">
          <div class="title"><el-avatar size="small" src="https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png" /> 有转正机会</div>
          <el-link :underline="false" class="more-link">查看更多职位 <el-icon><ArrowRight/></el-icon></el-link>
        </div>
        <div class="block-list">
          <div class="mini-job" v-for="job in recommendedJobs.slice(0, 2)" :key="job.id" @click="goToDetail(job.id)">
            <div class="mj-title" :title="job.title">{{ job.title }}</div>
            <div class="mj-salary">{{ job.salaryMin }}-{{ job.salaryMax }}元/天</div>
          </div>
        </div>
      </div>
      <div class="hot-block flex-2">
        <div class="block-header">
          <div class="title"><el-icon color="#00a6a7" size="18"><PieChart/></el-icon> 短期实习</div>
          <el-link :underline="false" class="more-link">查看更多职位 <el-icon><ArrowRight/></el-icon></el-link>
        </div>
        <div class="block-list">
          <div class="mini-job" v-for="job in recommendedJobs.slice(2, 4)" :key="job.id" @click="goToDetail(job.id)">
            <div class="mj-title" :title="job.title">{{ job.title }}</div>
            <div class="mj-salary">{{ job.salaryMin }}-{{ job.salaryMax }}元/天</div>
          </div>
        </div>
      </div>
      <div class="hot-block flex-1 align-center justify-center">
        <div class="more-modules">
          <div class="title"><el-icon size="18" color="#8b5cf6"><Menu/></el-icon> 更多模块</div>
          <div class="empty-illustration">
            <!-- Simplified SVG for empty/coming soon state -->
            <svg width="60" height="60" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M21 7.5V18C21 19.1046 20.1046 20 19 20H5C3.89543 20 3 19.1046 3 18V7.5M21 7.5L12 12L3 7.5M21 7.5L12 3L3 7.5" stroke="#ccc" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <p>即将开放更多招聘模块<br/>敬请期待哦~</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Recommended Jobs Area -->
    <div class="section-title">
      <h3>
        热门实习
        <div class="sub-tabs">
          <span v-for="tab in subTabs" :key="tab" class="sub-tab" :class="{ active: activeSubTab === tab }" @click="handleSubTabClick(tab)">{{ tab }}</span>
        </div>
      </h3>
    </div>

    <div v-loading="loading" class="job-list">
      <el-empty v-if="recommendedJobs.length === 0" description="暂无推荐职位，请去完善简历或浏览职位大厅" />
      
      <el-card v-for="job in recommendedJobs" :key="job.id" class="job-card" shadow="hover" :body-style="{ padding: '20px' }" @click="goToDetail(job.id)">
        <div class="job-primary">
          <div class="job-top">
            <div class="job-title" :title="job.title">{{ job.title }}</div>
            <div class="job-salary">{{ job.salaryMin }}-{{ job.salaryMax }}<span class="salary-unit">元/天</span></div>
          </div>
          <div class="job-tags">
            <span class="tag">{{ job.workLocation }}</span>
            <span class="tag">{{ job.workSchedule }}</span>
            <span class="tag match-tag" v-if="job.matchScore">匹配 {{ Math.round(job.matchScore) }}%</span>
          </div>
        </div>
        
        <div class="job-company">
          <div class="company-avatar">
            <el-avatar :size="24" shape="square" style="background: var(--color-bg-secondary); color: var(--color-accent); font-size: 12px;">{{ job.companyName?.charAt(0) || '企' }}</el-avatar>
          </div>
          <div class="company-info-inline">
            <span class="company-name">{{ job.companyName }}</span>
            <span class="company-industry">{{ job.categoryName }}</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { getRecommendations } from '@/api/student'
// import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const loading = ref(false)
const recommendedJobs = ref([])
const searchKeyword = ref('')
const searchType = ref('job')

const mainTabs = ['校招', '实习', '社招', '兼职']
const subTabs = ['综合', '暑期实习', '远程实习', '应届实习']
const activeMainTab = ref('实习')
const activeSubTab = ref('综合')

const fetchDashboardData = async () => {
  loading.value = true
  try {
    const recRes = await getRecommendations({ 
      page: 1, 
      size: 6,
      location: appStore.currentLocation === '全国' ? '' : appStore.currentLocation
    })
    recommendedJobs.value = recRes.content || []
  } catch (error) {
    console.error('Failed to load dashboard', error)
  } finally {
    loading.value = false
  }
}

watch(
  () => appStore.currentLocation,
  () => {
    fetchDashboardData()
  }
)

const handleTabClick = (tab) => {
  activeMainTab.value = tab
  router.push({
    path: '/student/jobs',
    query: { keyword: tab, type: 'job' }
  })
}

import { searchJobs } from '@/api/job'

const handleSubTabClick = async (tab) => {
  activeSubTab.value = tab
  const jobType = tab === '综合' ? '' : tab
  
  if (!jobType) {
    // If '综合', just fetch default recommendations again
    await fetchDashboardData()
    return
  }

  loading.value = true
  try {
    const res = await searchJobs({
      jobType: jobType,
      page: 1,
      size: 6,
      location: appStore.currentLocation === '全国' ? '' : appStore.currentLocation
    })
    recommendedJobs.value = res.content || []
  } catch (error) {
    console.error('Failed to search jobs for tab', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  const finalKeyword = searchKeyword.value.trim() || activeMainTab.value
  router.push({
    path: '/student/jobs',
    query: { keyword: finalKeyword, type: searchType.value }
  })
}

const goToDetail = (id) => {
  router.push(`/student/jobs/${id}`)
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
.dashboard-container {
  padding-top: 10px;
}

/* --- Search & Banner --- */
.home-search-banner {
  background: var(--color-bg-card);
  border-radius: 12px;
  padding: 30px;
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.02);
}

.search-tabs {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
}

.tab-btn {
  font-size: 16px;
  color: var(--color-text-primary);
  cursor: pointer;
  padding: 6px 16px;
  border-radius: 20px;
  transition: all 0.2s;
}

.tab-btn:hover {
  color: var(--color-accent);
}

.tab-btn.active {
  background-color: var(--color-accent);
  color: #fff;
  font-weight: 500;
}

.search-box-large {
  width: 100%;
  max-width: 800px;
  border: 2px solid var(--color-accent);
  border-radius: 6px;
  display: flex;
  overflow: hidden;
  background: #fff;
}

:deep(.main-search-input) {
  width: 100%;
}

:deep(.main-search-input > .el-input__wrapper) {
  height: 48px;
  border: none !important;
  box-shadow: none !important;
  border-radius: 0;
  background: transparent;
}

:deep(.main-search-input .el-input-group__prepend) {
  background-color: transparent;
  border: none;
  border-right: 1px solid #e4e7ed;
  border-radius: 0;
  padding: 0;
  box-shadow: none !important;
}

:deep(.main-search-input .el-input-group__prepend .el-select__wrapper),
:deep(.main-search-input .el-input-group__prepend .el-select__wrapper.is-focused),
:deep(.main-search-input .el-input-group__prepend .el-select__wrapper:hover),
:deep(.main-search-input .el-input-group__prepend .el-select__wrapper.is-hovering) {
  box-shadow: none !important;
  background: transparent !important;
  background-color: transparent !important;
  height: 48px;
  border: none !important;
  outline: none !important;
}

:deep(.main-search-input .el-input-group__prepend .el-select) {
  --el-select-border-color-hover: transparent;
  --el-select-input-focus-border-color: transparent;
}

:deep(.main-search-input .el-input-group__append) {
  background-color: var(--color-accent);
  border: none;
  color: #fff;
  border-radius: 0;
  padding: 0;
  box-shadow: none !important;
}

.search-btn-large {
  height: 48px;
  width: 100px;
  border-radius: 0;
  font-size: 16px;
  margin: 0;
  border: none;
  background-color: var(--color-accent);
  color: white;
}

/* --- Hot Sections / Mockups --- */
.hot-sections {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.flex-2 { flex: 4; }
.flex-1 { flex: 2; }
.align-center { align-items: center; }
.justify-center { justify-content: center; }

.hot-block {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.02);
}

.block-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.block-header .title {
  font-size: 16px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
}
.more-link {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.block-list {
  display: grid;
  gap: 16px;
}
.mini-job {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}
.mj-title {
  color: var(--color-text-secondary);
  cursor: pointer;
}
.mj-title:hover { color: var(--color-accent); }
.mj-salary { color: var(--color-danger); }

.more-modules {
  text-align: center;
}
.more-modules .title {
  font-weight: bold;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.empty-illustration p {
  color: var(--color-text-muted);
  font-size: 13px;
  margin-top: 8px;
  line-height: 1.5;
}

/* --- Recommended Section --- */
.section-title h3 {
  font-size: 20px;
  color: var(--color-text-primary);
  font-weight: bold;
  display: flex;
  align-items: baseline;
  gap: 24px;
  margin-bottom: 20px;
}

.sub-tabs {
  display: flex;
  gap: 20px;
}

.sub-tab {
  font-size: 15px;
  color: var(--color-text-secondary);
  font-weight: normal;
  cursor: pointer;
}
.sub-tab:hover { color: var(--color-accent); }
.sub-tab.active {
  background-color: var(--color-accent);
  color: white;
  padding: 2px 12px;
  border-radius: 14px;
  font-size: 13px;
}

.job-list {
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
  margin-bottom: 12px;
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
  font-size: 12px;
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
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid var(--color-bg-primary); /* Use a softer border */
}

.company-info-inline {
  display: flex;
  align-items: center;
  flex: 1;
  font-size: 13px;
  justify-content: space-between;
}

.company-name {
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}

.company-industry {
  color: var(--color-text-muted);
}
</style>
