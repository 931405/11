<template>
  <div class="page">
    <div class="panel">
      <div class="header">
        <div>
          <h1>人才库</h1>
          <p>搜索公开简历，快速筛选适合岗位的候选人。</p>
        </div>
        <div class="actions">
          <el-input v-model="keyword" placeholder="学校 / 专业 / 技能" clearable @keyup.enter="loadTalents" />
          <el-button type="primary" @click="loadTalents">搜索</el-button>
          <el-button @click="router.push('/enterprise/jobs')">岗位管理</el-button>
        </div>
      </div>

      <el-table :data="talents" v-loading="loading" style="width: 100%">
        <el-table-column prop="studentName" label="候选人" min-width="180" />
        <el-table-column prop="university" label="学校" min-width="180" />
        <el-table-column prop="major" label="专业" min-width="160" />
        <el-table-column prop="expectedLocation" label="期望地点" min-width="140" />
        <el-table-column label="技能标签" min-width="220">
          <template #default="{ row }">
            <el-tag v-for="item in parseJson(row.skills).slice(0, 4)" :key="item" class="tag">{{ item }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDrawer(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-drawer v-model="drawerVisible" title="候选人详情" size="520px">
      <div v-if="currentTalent" class="drawer-body">
        <h3>{{ currentTalent.studentName || '匿名候选人' }}</h3>
        <p class="muted">{{ currentTalent.university || '未填写学校' }} / {{ currentTalent.major || '未填写专业' }}</p>
        <div class="desc-item"><span>学历</span><strong>{{ currentTalent.educationLevel || '未填写' }}</strong></div>
        <div class="desc-item"><span>期望地点</span><strong>{{ currentTalent.expectedLocation || '未填写' }}</strong></div>
        <div class="desc-item"><span>可工作时间</span><strong>{{ currentTalent.availableSchedule || '未填写' }}</strong></div>
        <div class="desc-item"><span>技能</span><strong>{{ parseJson(currentTalent.skills).join('、') || '未填写' }}</strong></div>
        <p class="intro">{{ currentTalent.selfIntro || '暂无个人介绍' }}</p>
        <div class="drawer-actions">
          <el-button @click="previewResume(currentTalent.resumeAttachments)">预览附件</el-button>
          <el-button type="primary" @click="openInviteDialog">发起邀约</el-button>
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="inviteVisible" title="发起邀约" width="520px">
      <el-form label-position="top">
        <el-form-item label="选择岗位">
          <el-select v-model="inviteForm.jobId" placeholder="请选择岗位" style="width: 100%">
            <el-option v-for="item in jobs" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="邀约留言">
          <el-input v-model="inviteForm.greeting" type="textarea" :rows="4" placeholder="请输入邀约留言" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inviteVisible = false">取消</el-button>
        <el-button type="primary" :loading="inviteLoading" @click="submitInvite">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getEnterpriseJobs, inviteTalent, searchTalents } from '@/api/enterprise'

const router = useRouter()
const loading = ref(false)
const inviteLoading = ref(false)
const keyword = ref('')
const talents = ref([])
const jobs = ref([])
const drawerVisible = ref(false)
const inviteVisible = ref(false)
const currentTalent = ref(null)

const inviteForm = reactive({
  jobId: null,
  greeting: '你好，我们认为你的背景与岗位较为匹配，欢迎进一步沟通。'
})

const parseJson = (value) => {
  if (!value) return []
  try {
    return JSON.parse(value)
  } catch (error) {
    return []
  }
}

const loadTalents = async () => {
  loading.value = true
  try {
    const data = await searchTalents({ keyword: keyword.value || undefined, page: 1, size: 50 })
    talents.value = data.content || []
  } finally {
    loading.value = false
  }
}

const loadJobs = async () => {
  const data = await getEnterpriseJobs({ page: 1, size: 50, status: 'OPEN' })
  jobs.value = data.content || []
}

const openDrawer = (row) => {
  currentTalent.value = row
  drawerVisible.value = true
}

const openInviteDialog = () => {
  if (jobs.value.length === 0) {
    ElMessage.warning('请先创建一个开放中的岗位')
    return
  }
  inviteForm.jobId = jobs.value[0]?.id || null
  inviteVisible.value = true
}

const submitInvite = async () => {
  if (!currentTalent.value || !inviteForm.jobId) {
    ElMessage.warning('请选择岗位')
    return
  }
  inviteLoading.value = true
  try {
    await inviteTalent({
      studentId: currentTalent.value.studentUserId || currentTalent.value.studentId,
      jobId: inviteForm.jobId,
      greeting: inviteForm.greeting
    })
    ElMessage.success('邀约已发送')
    inviteVisible.value = false
    drawerVisible.value = false
  } finally {
    inviteLoading.value = false
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
  loadTalents()
  loadJobs()
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

.actions {
  display: flex;
  gap: 12px;
}

.tag {
  margin-right: 6px;
}

.drawer-body {
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

.drawer-actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 900px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
  }

  .actions {
    width: 100%;
    flex-direction: column;
  }
}
</style>
