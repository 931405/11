<template>
  <div class="page">
    <div class="panel">
      <div class="header">
        <div>
          <h1>候选人管理</h1>
          <p>岗位 ID：{{ route.params.id }}，可查看申请并处理状态。</p>
        </div>
        <div class="actions">
          <el-button @click="router.push('/enterprise/jobs')">返回岗位管理</el-button>
        </div>
      </div>

      <el-table :data="candidates" v-loading="loading" style="width: 100%">
        <el-table-column prop="studentName" label="候选人" min-width="160" />
        <el-table-column prop="university" label="学校" min-width="180" />
        <el-table-column prop="major" label="专业" min-width="160" />
        <el-table-column label="技能标签" min-width="220">
          <template #default="{ row }">
            <el-tag v-for="item in parseJson(row.skills).slice(0, 4)" :key="item" class="tag">{{ item }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120" align="center" />
        <el-table-column label="操作" width="280" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">查看</el-button>
            <el-button type="primary" link @click="changeStatus(row, 'REVIEWING')">审核中</el-button>
            <el-button type="warning" link @click="openInterview(row)">面试</el-button>
            <el-button type="success" link @click="changeStatus(row, 'ACCEPTED')">录用</el-button>
            <el-button type="danger" link @click="changeStatus(row, 'REJECTED')">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-drawer v-model="detailVisible" title="候选人详情" size="520px">
      <div v-if="currentCandidate" class="detail-body">
        <h3>{{ currentCandidate.studentName || '匿名候选人' }}</h3>
        <p class="muted">{{ currentCandidate.university || '未填写学校' }} / {{ currentCandidate.major || '未填写专业' }}</p>
        <div class="desc-item"><span>学历</span><strong>{{ currentCandidate.educationLevel || '未填写' }}</strong></div>
        <div class="desc-item"><span>期望地点</span><strong>{{ currentCandidate.expectedLocation || '未填写' }}</strong></div>
        <div class="desc-item"><span>期望时薪</span><strong>{{ salaryText(currentCandidate) }}</strong></div>
        <div class="desc-item"><span>匹配度</span><strong>{{ currentCandidate.matchScore ? `${Math.round(currentCandidate.matchScore)}%` : '—' }}</strong></div>
        <p class="intro">{{ currentCandidate.selfIntro || '暂无个人介绍' }}</p>
        <el-button @click="previewResume(currentCandidate.resumeAttachments)">预览附件</el-button>
      </div>
    </el-drawer>

    <el-dialog v-model="interviewVisible" title="发送面试邀请" width="520px">
      <el-form label-position="top">
        <el-form-item label="面试时间">
          <el-date-picker
            v-model="interviewForm.interviewTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="面试地点">
          <el-input v-model="interviewForm.location" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="interviewForm.contact" />
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input v-model="interviewForm.message" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="interviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitInterview">发送邀请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getJobCandidates, sendInterviewInvitation, updateApplicationStatus } from '@/api/enterprise'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const submitting = ref(false)
const candidates = ref([])
const currentCandidate = ref(null)
const detailVisible = ref(false)
const interviewVisible = ref(false)

const interviewForm = reactive({
  applicationId: null,
  interviewTime: '',
  location: '',
  contact: '',
  message: ''
})

const parseJson = (value) => {
  if (!value) return []
  try {
    return JSON.parse(value)
  } catch (error) {
    return []
  }
}

const salaryText = (row) => {
  if (row.expectedSalaryMin == null && row.expectedSalaryMax == null) return '面议'
  return `${row.expectedSalaryMin ?? '-'} - ${row.expectedSalaryMax ?? '-'} 元/小时`
}

const loadCandidates = async () => {
  loading.value = true
  try {
    const data = await getJobCandidates(route.params.id, { page: 1, size: 50 })
    candidates.value = data.content || []
  } finally {
    loading.value = false
  }
}

const openDetail = (row) => {
  currentCandidate.value = row
  detailVisible.value = true
}

const changeStatus = async (row, status) => {
  await updateApplicationStatus(row.applicationId, status)
  ElMessage.success('状态更新成功')
  loadCandidates()
}

const openInterview = (row) => {
  interviewForm.applicationId = row.applicationId
  interviewForm.interviewTime = ''
  interviewForm.location = ''
  interviewForm.contact = ''
  interviewForm.message = ''
  interviewVisible.value = true
}

const submitInterview = async () => {
  submitting.value = true
  try {
    await sendInterviewInvitation({ ...interviewForm })
    ElMessage.success('面试邀请已发送')
    interviewVisible.value = false
    loadCandidates()
  } finally {
    submitting.value = false
  }
}

const previewResume = (attachmentsValue) => {
  try {
    const list = JSON.parse(attachmentsValue || '[]')
    if (list.length > 0 && list[0].url) {
      window.open(`http://localhost:8080${list[0].url}`, '_blank')
      return
    }
  } catch (error) {
    // ignore
  }
  ElMessage.warning('该候选人未上传附件')
}

onMounted(() => {
  loadCandidates()
})
</script>

<style scoped>
.page {
  padding: 32px 20px;
}

.panel {
  max-width: 1180px;
  margin: 0 auto;
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
  margin-bottom: 20px;
}

.header p,
.muted {
  color: var(--color-text-secondary);
}

.tag {
  margin-right: 6px;
}

.detail-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.desc-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-border);
}

.desc-item span {
  color: var(--color-text-secondary);
}

.intro {
  white-space: pre-wrap;
  color: var(--color-text-secondary);
}

@media (max-width: 900px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
