<template>
  <div class="job-detail-page" v-loading="loading">
    <!-- Dark Hero Banner -->
    <div class="boss-hero" v-if="job">
      <div class="hero-inner">
        <div class="hero-main">
          <div class="hero-title-row">
            <h1 class="job-title">{{ job.title }}</h1>
            <span class="job-salary">{{ job.salaryMin }}-{{ job.salaryMax }}元/天</span>
          </div>
          <div class="hero-meta-row">
            <span class="meta-item"><el-icon><Location /></el-icon> {{ job.workLocation }}</span>
            <span class="meta-item"><el-icon><Calendar /></el-icon> {{ job.durationRequirement || '3个月' }}</span>
            <span class="meta-item"><el-icon><School /></el-icon> {{ job.educationRequirement || '本科' }}</span>
          </div>
          <div class="hero-tags-row" v-if="jobTags.length > 0">
            <span class="hero-tag" v-for="(tag, index) in jobTags" :key="index">{{ tag }}</span>
          </div>
        </div>
        <div class="hero-actions">
          <el-button 
            :type="job.isFavorited ? 'primary' : 'default'" 
            plain 
            size="large"
            class="action-btn interest-btn"
            @click="toggleFavorite"
          >
            <el-icon><Star v-if="!job.isFavorited" /><StarFilled v-else /></el-icon>
            {{ job.isFavorited ? '已感兴趣' : '感兴趣' }}
          </el-button>
          <el-button 
            type="primary" 
            size="large" 
            class="action-btn chat-btn"
            :disabled="job.isApplied || job.status !== 'OPEN'"
            @click="openApplyDialog"
          >
            {{ job.isApplied ? '已投递申请' : (job.status !== 'OPEN' ? '已停止招聘' : '立即沟通') }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="detail-container" v-if="job">
      <div class="detail-body">
        <!-- 左侧详细描述 -->
        <div class="main-column">
          <el-card shadow="never" class="section-card">
            <h3 class="section-title">职位描述</h3>
            <div class="text-content html-format">{{ cleanDescription }}</div>
            
            <div class="skills-section" v-if="jobSkills.length > 0">
              <div class="skills-tags">
                <el-tag v-for="(skill, index) in jobSkills" :key="index" effect="plain" class="skill-tag">
                  {{ skill }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 右侧企业信息 -->
        <div class="side-column">
          <el-card shadow="never" class="company-card">
            <h3 class="section-title">公司基本信息</h3>
            <div class="company-info">
              <div class="company-header">
                <el-avatar :size="48" shape="square" class="company-logo">{{ job.companyName?.charAt(0) || '企' }}</el-avatar>
                <div class="company-name">{{ job.companyName }}</div>
              </div>
              <div class="company-meta">
                <div class="meta-row"><el-icon><Money /></el-icon> 不需要融资</div>
                <div class="meta-row"><el-icon><User /></el-icon> {{ job.companyScale || '1000-9999人' }}</div>
                <div class="meta-row"><el-icon><DataLine /></el-icon> {{ job.companyIndustry || '互联网' }}</div>
              </div>
              <el-button class="view-all-btn" plain @click="router.push({ path: '/student/jobs', query: { keyword: job.companyName } })">查看全部职位</el-button>
            </div>
          </el-card>
        </div>
      </div>
    </div>

    <!-- 投递确认弹窗 -->
    <el-dialog v-model="applyDialogVisible" title="职位申请" width="500px">
      <div class="apply-dialog-content">
        <el-alert
          title="系统将使用您当前的个人简历进行投递"
          type="info"
          show-icon
          :closable="false"
          class="mb-20"
        />
        
        <el-form :model="applyForm" label-position="top">
          <el-form-item label="附加留言（选填，给企业HR的话）">
            <el-input 
              v-model="applyForm.message" 
              type="textarea" 
              rows="4"
              placeholder="简短描述您的优势和入职意愿..."
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="applyDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitApply">确认投递</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getJobDetail } from '@/api/job'
import { applyJob, favoriteJob, unfavoriteJob, logBehavior } from '@/api/student'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const jobId = route.params.id

const loading = ref(true)
const job = ref(null)
const applyDialogVisible = ref(false)
const submitLoading = ref(false)

const applyForm = ref({
  message: ''
})

// Variables for Dwell Time tracking
const enterTime = ref(null)

const recordDwellTime = async () => {
  if (enterTime.value && getCookie('token')) {
    const leaveTime = new Date()
    const dwellSeconds = Math.floor((leaveTime - enterTime.value) / 1000)
    
    // Only log if they spent at least 3 seconds investigating
    if (dwellSeconds > 2) {
      try {
        await logBehavior({
          jobId: parseInt(jobId),
          actionType: 'VIEW_JOB',
          dwellTime: dwellSeconds
        })
      } catch (err) {
        console.error('Failed to log behavior', err)
      }
    }
  }
}

// Simple cookie parser for tracking if logged in
const getCookie = (name) => {
  const value = `; ${document.cookie}`
  const parts = value.split(`; ${name}=`)
  if (parts.length === 2) return parts.pop().split(';').shift()
  return localStorage.getItem('token') // Fallback to localStorage just in case
}

const jobSkills = computed(() => {
  if (!job.value || !job.value.skillsRequired) return []
  try {
    return JSON.parse(job.value.skillsRequired)
  } catch (e) {
    return []
  }
})

const jobTags = computed(() => {
  if (!job.value || !job.value.jobTags) return []
  try {
    return JSON.parse(job.value.jobTags)
  } catch (e) {
    return []
  }
})

const cleanDescription = computed(() => {
  if (!job.value || !job.value.description) return '暂无详细描述'
  let text = job.value.description
  if (text.startsWith('职位描述')) {
    text = text.replace(/^职位描述\s*/, '')
  }
  return text
})

const fetchDetail = async () => {
  loading.value = true
  try {
    job.value = await getJobDetail(jobId)
  } catch (error) {
    console.error('Failed to load job detail', error)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return dayjs(dateString).format('YYYY-MM-DD HH:mm')
}

const toggleFavorite = async () => {
  try {
    if (job.value.isFavorited) {
      await unfavoriteJob(jobId)
      job.value.isFavorited = false
      ElMessage.success('已取消收藏')
    } else {
      await favoriteJob(jobId)
      job.value.isFavorited = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    // Error handled globally
  }
}

const openApplyDialog = () => {
  applyForm.value.message = ''
  applyDialogVisible.value = true
}

const submitApply = async () => {
  submitLoading.value = true
  try {
    await applyJob(jobId, applyForm.value.message)
    ElMessage.success('投递成功！企业将很快查看您的简历')
    applyDialogVisible.value = false
    job.value.isApplied = true
    job.value.applyCount++
  } catch (error) {
    // Error handled globally
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  enterTime.value = new Date()
  fetchDetail()
})

onUnmounted(() => {
  recordDwellTime()
})
</script>

<style scoped>
.job-detail-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f6f6f8;
  padding-bottom: 40px;
}

.boss-hero {
  background-color: #444c57;
  width: 100%;
  color: #fff;
  padding: 40px 0;
}

.hero-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.hero-main {
  flex: 1;
}

.hero-title-row {
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 16px;
}

.job-title {
  font-size: 32px;
  font-weight: bold;
  margin: 0;
  color: #fff;
}

.job-salary {
  font-size: 32px;
  font-weight: bold;
  color: #f26d49;
}

.hero-meta-row {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
  font-size: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #fff;
}

.hero-tags-row {
  display: flex;
  gap: 12px;
}

.hero-tag {
  background: rgba(255,255,255,0.1);
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  color: #fff;
}

.hero-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.action-btn {
  height: 44px;
  font-size: 16px;
  border-radius: 6px;
  padding: 0 32px;
}

.interest-btn {
  background: transparent;
  color: #fff;
  border-color: rgba(255,255,255,0.4);
}
.interest-btn:hover {
  border-color: #fff;
}

.chat-btn {
  background: #00a6a7;
  border-color: #00a6a7;
  color: #fff;
}
.chat-btn:hover {
  background: #008f90;
}

.detail-container {
  max-width: 1200px;
  margin: 20px auto 0;
}

.detail-body {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.main-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.side-column {
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-card {
  border-radius: 8px;
  border: none;
  padding: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #222;
  margin-top: 0;
  margin-bottom: 20px;
}

.text-content {
  color: #333;
  line-height: 1.8;
  font-size: 15px;
  white-space: pre-wrap;
}

.skills-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.skills-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.skill-tag {
  background-color: #f8f8f8;
  border-color: #e5e5e5;
  color: #666;
  padding: 0 16px;
  height: 28px;
  line-height: 26px;
  border-radius: 14px;
}

.company-card {
  border-radius: 8px;
  border: none;
  padding: 24px;
}

.company-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.company-logo {
  border-radius: 8px;
  font-size: 20px;
  background: #f0f2f5;
  color: #00a6a7;
}

.company-name {
  font-size: 16px;
  font-weight: 600;
  color: #222;
}

.company-meta {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

.view-all-btn {
  width: 100%;
  color: #00a6a7;
  border-color: #00a6a7;
}
.view-all-btn:hover {
  background: rgba(0, 166, 167, 0.05);
}

.download-app-banner {
  background: linear-gradient(135deg, #444c57, #242831);
  border-radius: 8px;
  padding: 24px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.banner-content {
  position: relative;
  z-index: 1;
}

.b-icon {
  font-size: 13px;
  color: rgba(255,255,255,0.7);
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 12px;
}

.download-app-banner h4 {
  margin: 0 0 6px 0;
  font-size: 20px;
}

.download-app-banner p {
  margin: 0 0 20px 0;
  font-size: 14px;
  color: rgba(255,255,255,0.8);
}

.mb-20 {
  margin-bottom: 20px;
}
</style>
