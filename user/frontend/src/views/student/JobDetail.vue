<template>
  <div class="page" v-loading="loading">
    <div class="hero" v-if="job">
      <div>
        <h1>{{ job.title }}</h1>
        <div class="hero-meta">
          <span>{{ job.companyName || '未命名企业' }}</span>
          <span>{{ job.workLocation || '地点待定' }}</span>
          <span>{{ salaryText }}</span>
        </div>
      </div>
      <div class="hero-actions">
        <el-button @click="toggleFavorite">{{ job.isFavorited ? '取消收藏' : '收藏岗位' }}</el-button>
        <el-button type="primary" :disabled="job.isApplied || job.status !== 'OPEN'" @click="dialogVisible = true">
          {{ job.isApplied ? '已投递' : '立即投递' }}
        </el-button>
      </div>
    </div>

    <div v-if="job" class="content">
      <div class="main-card">
        <h2>岗位介绍</h2>
        <p class="text">{{ job.description || '暂无岗位介绍' }}</p>

        <h2>岗位要求</h2>
        <p class="text">{{ job.requirements || '暂无岗位要求' }}</p>

        <div class="tags" v-if="job.skillsRequired">
          <el-tag v-for="item in parseJson(job.skillsRequired)" :key="item">{{ item }}</el-tag>
        </div>
      </div>

      <div class="side-card">
        <h2>企业信息</h2>
        <div class="info-row"><span>企业名称</span><strong>{{ job.companyName || '未命名企业' }}</strong></div>
        <div class="info-row"><span>行业</span><strong>{{ job.companyIndustry || '未填写' }}</strong></div>
        <div class="info-row"><span>规模</span><strong>{{ job.companyScale || '未填写' }}</strong></div>
        <div class="info-row"><span>分类</span><strong>{{ job.categoryName || '未分类' }}</strong></div>
        <div class="info-row"><span>浏览量</span><strong>{{ job.viewCount || 0 }}</strong></div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="投递岗位" width="520px">
      <el-form label-position="top">
        <el-form-item label="给企业留言（可选）">
          <el-input v-model="message" type="textarea" :rows="4" placeholder="介绍你的优势、到岗时间等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitApply">确认投递</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getJobDetail } from '@/api/job'
import { applyJob, favoriteJob, logBehavior, unfavoriteJob } from '@/api/student'

const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const message = ref('')
const job = ref(null)
const enterTime = ref(Date.now())

const salaryText = computed(() => {
  if (!job.value) return ''
  if (job.value.salaryMin == null && job.value.salaryMax == null) return '面议'
  return `${job.value.salaryMin ?? '-'} - ${job.value.salaryMax ?? '-'} 元/小时`
})

const parseJson = (value) => {
  if (!value) return []
  try {
    return JSON.parse(value)
  } catch (error) {
    return []
  }
}

const loadDetail = async () => {
  loading.value = true
  try {
    job.value = await getJobDetail(route.params.id)
  } finally {
    loading.value = false
  }
}

const toggleFavorite = async () => {
  if (!job.value) return
  if (job.value.isFavorited) {
    await unfavoriteJob(job.value.id)
    job.value.isFavorited = false
    ElMessage.success('已取消收藏')
  } else {
    await favoriteJob(job.value.id)
    job.value.isFavorited = true
    ElMessage.success('收藏成功')
  }
}

const submitApply = async () => {
  if (!job.value) return
  submitting.value = true
  try {
    await applyJob(job.value.id, message.value)
    job.value.isApplied = true
    dialogVisible.value = false
    ElMessage.success('投递成功')
  } finally {
    submitting.value = false
  }
}

const reportBehavior = async () => {
  const seconds = Math.floor((Date.now() - enterTime.value) / 1000)
  if (!route.params.id || seconds < 3) return
  try {
    await logBehavior({
      jobId: Number(route.params.id),
      actionType: 'VIEW_JOB',
      dwellTime: seconds
    })
  } catch (error) {
    // ignore
  }
}

onMounted(() => {
  enterTime.value = Date.now()
  loadDetail()
})

onUnmounted(() => {
  reportBehavior()
})
</script>

<style scoped>
.page {
  padding: 32px 20px;
}

.hero,
.content {
  max-width: 1180px;
  margin: 0 auto 20px;
}

.hero {
  background: #444c57;
  color: #fff;
  border-radius: 24px;
  padding: 28px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 10px;
  color: rgba(255, 255, 255, 0.85);
}

.hero-actions {
  display: flex;
  gap: 12px;
}

.content {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
}

.main-card,
.side-card {
  background: #fff;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 10px 40px rgba(27, 44, 51, 0.08);
}

.main-card h2,
.side-card h2 {
  margin-bottom: 12px;
}

.text {
  white-space: pre-wrap;
  color: var(--color-text-secondary);
  margin-bottom: 18px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
}

.info-row span {
  color: var(--color-text-secondary);
}

@media (max-width: 900px) {
  .hero {
    flex-direction: column;
  }

  .hero-actions {
    width: 100%;
    flex-direction: column;
  }

  .content {
    grid-template-columns: 1fr;
  }
}
</style>
