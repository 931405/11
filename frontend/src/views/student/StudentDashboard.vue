<template>
  <div class="student-dashboard-shell">
    <aside class="side-promo side-promo-left">
      <el-card
        v-for="company in promotedCompanies.slice(0, 1)"
        :key="`left-${company.id}`"
        class="side-promo-card"
        shadow="hover"
        :body-style="{ padding: '0' }"
        @click="goToEnterprise(company.id)"
      >
        <div class="side-promo-inner">
          <div class="side-promo-badge">推广企业</div>
          <el-avatar :size="50" shape="square" class="side-promo-logo">{{ company.name.charAt(0) }}</el-avatar>
          <div class="side-promo-name">{{ company.name }}</div>
          <div class="side-promo-industry">{{ company.industry }}</div>
          <div class="side-promo-desc">{{ company.description }}</div>

          <div class="side-promo-tags">
            <span v-for="tag in company.tags" :key="tag" class="promo-tag">{{ tag }}</span>
          </div>

          <div class="side-promo-jobs">
            <div class="promo-job-header">🔥 内部热招</div>
            <div class="promo-job-list">
              <div v-for="job in company.hotJobs" :key="job" class="promo-job-item">
                <span class="job-dot"></span>{{ job }}
              </div>
            </div>
          </div>

          <div class="space-filler"></div>

          <div class="side-promo-meta">{{ company.location }} | {{ company.jobCount }}个岗位</div>
          <el-button type="primary" color="#00a6a7" size="small" @click.stop="goToEnterprise(company.id)">查看企业</el-button>
        </div>
      </el-card>
    </aside>

    <div class="dashboard-center-wrap">
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
  </div>

    <aside class="side-promo side-promo-right">
      <el-card
        v-for="company in promotedCompanies.slice(1, 2)"
        :key="`right-${company.id}`"
        class="side-promo-card"
        shadow="hover"
        :body-style="{ padding: '0' }"
        @click="goToEnterprise(company.id)"
      >
        <div class="side-promo-inner">
          <div class="side-promo-badge">推广企业</div>
          <el-avatar :size="50" shape="square" class="side-promo-logo">{{ company.name.charAt(0) }}</el-avatar>
          <div class="side-promo-name">{{ company.name }}</div>
          <div class="side-promo-industry">{{ company.industry }}</div>
          <div class="side-promo-desc">{{ company.description }}</div>

          <div class="side-promo-tags">
            <span v-for="tag in company.tags" :key="tag" class="promo-tag">{{ tag }}</span>
          </div>

          <div class="side-promo-jobs">
            <div class="promo-job-header">🔥 内部热招</div>
            <div class="promo-job-list">
              <div v-for="job in company.hotJobs" :key="job" class="promo-job-item">
                <span class="job-dot"></span>{{ job }}
              </div>
            </div>
          </div>

          <div class="space-filler"></div>

          <div class="side-promo-meta">{{ company.location }} | {{ company.jobCount }}个岗位</div>
          <el-button type="primary" color="#00a6a7" size="small" @click.stop="goToEnterprise(company.id)">查看企业</el-button>
        </div>
      </el-card>
    </aside>
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

const goToEnterprise = (id) => {
  // router.push(`/enterprise/${id}`)
}

const promotedCompanies = [
  { 
    id: 1, 
    name: '字节跳动', 
    industry: '互联网/科技', 
    description: '全球领先的科技公司，提供丰富的技术、产品和运营实习岗位机会。', 
    location: '北京', 
    jobCount: 15,
    tags: ['弹性工作', '就近租房补贴', '免费三餐', '大牛带队'],
    hotJobs: ['前端实习生', '后端开发实习', '产品经理助理', '运营实习生', '算法工程师']
  },
  { 
    id: 2, 
    name: '腾讯', 
    industry: '互联网/科技', 
    description: '覆盖产品、研发、设计等方向，适合希望进入头部互联网企业的同学。', 
    location: '深圳', 
    jobCount: 12,
    tags: ['海量历练', '鹅厂福利', '班车接送', '完善培训'],
    hotJobs: ['产品策划实习生', 'iOS开发实习', '游戏运营实习', '交互设计', '数据分析实习']
  }
]

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
.student-dashboard-shell { position: relative; min-height: 100%; }
.dashboard-center-wrap { width: min(100%, 1180px); margin: 0 auto; }
.side-promo { position: fixed; top: 96px; width: 200px; z-index: 30; }
.side-promo-left { left: max(12px, calc((100vw - 1180px) / 2 - 220px)); }
.side-promo-right { right: max(12px, calc((100vw - 1180px) / 2 - 220px)); }
.side-promo-card { overflow: hidden; cursor: pointer; border-radius: 16px; border: none; box-shadow: 0 4px 20px rgba(0,0,0,0.05); transition: transform 0.3s, box-shadow 0.3s; }
.side-promo-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0, 166, 167, 0.15); }
.side-promo-inner { display: flex; flex-direction: column; align-items: center; text-align: center; padding: 24px 16px 20px; background: linear-gradient(180deg, #f0fdfc 0%, #ffffff 60%); position: relative; min-height: 900px; }
.side-promo-badge { position: absolute; top: 0; left: 0; padding: 4px 10px; border-radius: 0 0 12px 0; background: linear-gradient(90deg, #ff9a44, #fc6076); color: #fff; font-size: 12px; font-weight: 600; box-shadow: 2px 2px 8px rgba(255, 154, 68, 0.3); }
.side-promo-logo { margin-top: 12px; background: linear-gradient(135deg, #12b3a8, #0f766e); color: #fff; font-size: 24px; font-weight: bold; box-shadow: 0 4px 12px rgba(18, 179, 168, 0.3); border: 2px solid #fff; }
.side-promo-name { color: #1f2937; font-size: 18px; font-weight: 800; margin-top: 8px; }
.side-promo-industry { color: #00a6a7; font-size: 12px; background: rgba(0, 166, 167, 0.1); padding: 4px 12px; border-radius: 20px; margin-bottom: 8px; }
.side-promo-desc { color: #6b7280; font-size: 13px; line-height: 1.6; text-align: justify; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.side-promo-tags { display: flex; flex-wrap: wrap; gap: 6px; justify-content: center; margin-top: 12px; }
.promo-tag { padding: 2px 8px; background: #e5f6f6; color: #008f90; font-size: 11px; border-radius: 4px; }
.side-promo-jobs { margin-top: 24px; width: 100%; text-align: left; }
.promo-job-header { font-size: 14px; font-weight: 700; color: #374151; margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px dashed #e8eef2; }
.promo-job-list { display: flex; flex-direction: column; gap: 10px; }
.promo-job-item { font-size: 13px; color: #4b5563; display: flex; align-items: center; cursor: pointer; transition: color 0.2s; }
.promo-job-item:hover { color: #00a6a7; }
.job-dot { width: 6px; height: 6px; border-radius: 50%; background: #00a6a7; margin-right: 8px; flex-shrink: 0; }
.space-filler { margin-bottom: auto; }
.side-promo-meta { color: #9ca3af; font-size: 12px; margin: 12px 0; display: flex; align-items: center; gap: 6px; }
.side-promo-card .el-button { width: 100%; border-radius: 8px; font-weight: 600; font-size: 14px; padding: 10px 0; letter-spacing: 1px; transition: all 0.3s; }
.side-promo-card .el-button:hover { background-color: #008f90; transform: scale(1.02); }

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
