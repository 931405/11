<template>
  <div class="interviews-container">
    <div class="header-section">
      <h2 class="page-title">面试邀请</h2>
      <p class="description">查看和管理企业向您发送的面试邀请。</p>
    </div>

    <div class="list-section" v-loading="loading">
      <el-empty v-if="total === 0" description="暂无面试邀请" />
      
      <div v-else class="interview-list">
        <el-card 
          v-for="inv in interviews" 
          :key="inv.id" 
          class="interview-card" 
          shadow="hover"
        >
          <div class="card-header">
            <div class="company-info">
              <el-avatar :size="48" class="ent-avatar">{{ inv.companyName?.charAt(0) || '企' }}</el-avatar>
              <div>
                <h3 class="job-title">{{ inv.jobTitle }}</h3>
                <p class="company-name">{{ inv.companyName }}</p>
              </div>
            </div>
            <div class="status-badge">
              <el-tag :type="getStatusType(inv.status)" effect="dark" size="small">
                {{ getStatusLabel(inv.status) }}
              </el-tag>
            </div>
          </div>

          <el-divider style="margin: 16px 0;" />
          
          <div class="interview-details">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="面试时间">
                <strong class="highlight-text">{{ formatDateTime(inv.interviewTime) }}</strong>
              </el-descriptions-item>
              <el-descriptions-item label="地点/链接">
                {{ inv.location }}
              </el-descriptions-item>
              <el-descriptions-item label="联系人">
                {{ inv.contact }}
              </el-descriptions-item>
              <el-descriptions-item label="发送时间">
                <span class="text-muted">{{ formatDateTime(inv.createdAt) }}</span>
              </el-descriptions-item>
            </el-descriptions>
            
            <div v-if="inv.message" class="message-box">
              <strong>HR留言:</strong> {{ inv.message }}
            </div>
          </div>

          <div class="card-actions" v-if="inv.status === 'PENDING'">
            <el-button type="success" @click="handleStatusUpdate(inv.id, 'ACCEPTED')">接受面试</el-button>
            <el-button type="danger" plain @click="handleStatusUpdate(inv.id, 'REJECTED')">婉拒</el-button>
          </div>
        </el-card>
      </div>
      
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="total"
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[5, 10, 20]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getInterviews, updateInterviewStatus } from '@/api/student'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)
const interviews = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const fetchInterviews = async () => {
  loading.value = true
  try {
    const res = await getInterviews({ page: page.value, size: size.value })
    interviews.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to load interviews', error)
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  size.value = val
  fetchInterviews()
}

const handleCurrentChange = (val) => {
  page.value = val
  fetchInterviews()
}

const handleStatusUpdate = async (id, status) => {
  try {
    await updateInterviewStatus(id, status)
    ElMessage.success('状态已更新')
    fetchInterviews()
  } catch (error) {
    // handled globally
  }
}

const getStatusType = (status) => {
  switch(status) {
    case 'PENDING': return 'warning'
    case 'ACCEPTED': return 'success'
    case 'REJECTED': return 'danger'
    case 'CANCELLED': return 'info'
    default: return 'info'
  }
}

const getStatusLabel = (status) => {
  switch(status) {
    case 'PENDING': return '待确认'
    case 'ACCEPTED': return '已接受'
    case 'REJECTED': return '已婉拒'
    case 'CANCELLED': return '已取消'
    default: return status
  }
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  fetchInterviews()
})
</script>

<style scoped>
.interviews-container {
  max-width: 900px;
  margin: 0 auto;
}
.header-section {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.page-title {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: var(--color-text-primary);
}
.description {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 14px;
}
.interview-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.interview-card {
  border-radius: 8px;
  transition: transform 0.2s, box-shadow 0.2s;
}
.interview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.company-info {
  display: flex;
  align-items: center;
  gap: 16px;
}
.ent-avatar {
  background-color: var(--color-accent);
  color: #fff;
  font-size: 18px;
  font-weight: bold;
}
.job-title {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: var(--color-text-primary);
}
.company-name {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 13px;
}
.highlight-text {
  color: var(--color-accent);
  font-size: 15px;
}
.text-muted {
  color: var(--color-text-muted);
}
.message-box {
  margin-top: 16px;
  padding: 12px;
  background-color: var(--color-bg-secondary);
  border-radius: 6px;
  font-size: 13px;
  color: var(--color-text-secondary);
  border-left: 3px solid var(--color-accent);
}
.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}
.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
</style>
