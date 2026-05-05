<template>
  <div class="settings-page">
    <div class="settings-card" v-loading="loading">
      <div v-if="enterprise">
        <div class="settings-header">
          <el-avatar :size="48" shape="square" class="company-logo">{{ enterprise.companyName?.charAt(0) || '企' }}</el-avatar>
          <h2>{{ enterprise.companyName }}</h2>
        </div>

        <div class="company-status-row" v-if="enterprise.certificationStatus === 'APPROVED'">
          <el-tag type="success" effect="light" size="small">已认证企业</el-tag>
        </div>

        <p class="settings-subtitle" v-if="enterprise.description">{{ enterprise.description }}</p>

        <!-- Company Info -->
        <div class="section">
          <h3 class="section-title">公司信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">行业领域</span>
              <span class="info-value">{{ enterprise.industry || '暂未填写' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">公司规模</span>
              <span class="info-value">{{ enterprise.companySize || '暂未填写' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">公司地址</span>
              <span class="info-value">{{ enterprise.companyAddress || '暂未填写' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">在招职位</span>
              <span class="info-value">{{ jobCount }} 个</span>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="section">
          <el-button type="primary" color="#00a6a7" @click="viewEnterpriseJobs">查看全部在招职位</el-button>
        </div>
      </div>

      <el-empty v-else description="企业信息不存在" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getEnterprisePublicInfo, getEnterpriseJobCount } from '@/api/enterprisePublic'
import { searchJobs } from '@/api/job'

const route = useRoute()
const router = useRouter()
const enterpriseId = route.params.id

const loading = ref(false)
const enterprise = ref(null)
const jobCount = ref(0)

const fetchEnterpriseInfo = async () => {
  loading.value = true
  try {
    enterprise.value = await getEnterprisePublicInfo(enterpriseId)
    const countRes = await getEnterpriseJobCount(enterpriseId)
    jobCount.value = countRes || 0
  } catch (error) {
    console.error('Failed to load enterprise info', error)
  } finally {
    loading.value = false
  }
}

const viewEnterpriseJobs = () => {
  router.push({ path: '/student/jobs', query: { enterpriseId: enterpriseId } })
}

onMounted(() => {
  fetchEnterpriseInfo()
})
</script>

<style scoped>
.settings-page {
  padding: 32px 0;
  max-width: 800px;
  margin: 0 auto;
}

.settings-card {
  background: var(--color-bg-card);
  border-radius: 12px;
  padding: 32px;
  border: 1px solid var(--color-border);
}

.settings-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.settings-header h2 {
  color: var(--color-text-primary);
  font-size: 20px;
  font-weight: 600;
}

.company-logo {
  border-radius: 8px;
  font-size: 20px;
  background: var(--color-bg-secondary);
  color: var(--color-accent);
}

.company-status-row {
  margin-bottom: 12px;
}

.settings-subtitle {
  color: var(--color-text-secondary);
  font-size: 14px;
  margin-bottom: 24px;
  line-height: 1.6;
}

.section {
  margin-bottom: 24px;
}

.section-title {
  color: var(--color-text-primary);
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-border);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 13px;
  color: var(--color-text-muted);
}

.info-value {
  font-size: 14px;
  color: var(--color-text-primary);
}
</style>
