<template>
  <div class="talent-pool-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="header-title">发现牛人</h2>
        <span class="header-desc">主动出击，寻找最适合您的候选人</span>
      </div>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索学校、专业、技能标签"
          clearable
          style="width: 250px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
      </div>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="talents" v-loading="loading" style="width: 100%" stripe>
        <el-table-column label="候选人" min-width="180">
          <template #default="scope">
            <div class="candidate-info">
              <el-avatar :size="40" :src="scope.row.avatar" class="c-avatar">
                {{ scope.row.studentName?.charAt(0) || '匿' }}
              </el-avatar>
              <div class="c-detail">
                <strong>{{ scope.row.studentName || '匿名用户' }}</strong>
                <div class="c-school text-muted">
                  {{ scope.row.university }} | {{ scope.row.major }} | {{ scope.row.educationLevel }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="期望城市" prop="expectedLocation" width="120" />
        
        <el-table-column label="期望薪资" width="120">
          <template #default="scope">
            <span v-if="scope.row.expectedSalaryMin" class="salary-text">
              {{ scope.row.expectedSalaryMin }}k - {{ scope.row.expectedSalaryMax }}k
            </span>
            <span v-else class="text-muted">面议</span>
          </template>
        </el-table-column>

        <el-table-column label="技能标签" min-width="200">
          <template #default="scope">
            <div class="skills-cell">
              <el-tag
                v-for="skill in parseSkills(scope.row.skills).slice(0, 3)"
                :key="skill"
                size="small"
                effect="plain"
                class="skill-tag"
              >
                {{ skill }}
              </el-tag>
              <el-tag v-if="parseSkills(scope.row.skills).length > 3" size="small" type="info" effect="plain" class="skill-tag">
                +{{ parseSkills(scope.row.skills).length - 3 }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="openDrawer(scope.row)">
              查看简历
            </el-button>
          </template>
        </el-table-column>
        
        <template #empty>
          <el-empty description="暂无符合条件的活跃候选人" />
        </template>
      </el-table>

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 简历详情弹窗 -->
    <el-drawer
      v-model="drawerVisible"
      title="候选人微简历"
      size="500px"
      destroy-on-close
    >
      <div v-if="drawerCandidate" class="resume-drawer">
        <div class="rd-header mb-4">
          <el-avatar :size="64" :src="drawerCandidate.avatar" class="rd-avatar">
            {{ drawerCandidate.studentName?.charAt(0) || '匿' }}
          </el-avatar>
          <div class="rd-info">
            <h3 class="rd-name">{{ drawerCandidate.studentName || '匿名用户' }}</h3>
            <p class="rd-sub">
              毕业时间：{{ drawerCandidate.enrollmentYear ? drawerCandidate.enrollmentYear + 4 : '保密' }}
              <span class="divider"></span> 
              {{ drawerCandidate.educationLevel || '保密' }}
            </p>
          </div>
        </div>
        
        <el-descriptions :column="1" border class="rd-desc">
          <el-descriptions-item label="毕业院校">
            {{ drawerCandidate.university || '未填写' }} - {{ drawerCandidate.major || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="期望城市">
            {{ drawerCandidate.expectedLocation || '不限' }}
          </el-descriptions-item>
          <el-descriptions-item label="期望薪水">
            <span class="salary-text">{{ drawerCandidate.expectedSalaryMin ? `${drawerCandidate.expectedSalaryMin}k-${drawerCandidate.expectedSalaryMax}k` : '面议' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="到岗时间">
            {{ drawerCandidate.availableSchedule || '随时' }}
          </el-descriptions-item>
          <el-descriptions-item label="核心技能">
            <div class="skills-cell">
              <el-tag
                v-for="skill in parseSkills(drawerCandidate.skills)"
                :key="skill"
                size="small"
                effect="plain"
              >
                {{ skill }}
              </el-tag>
              <span v-if="parseSkills(drawerCandidate.skills).length === 0" class="text-muted">未填写</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="附件简历">
            <div v-if="getResumeUrl(drawerCandidate.resumeAttachments)">
              <el-button type="primary" link @click="downloadResume(drawerCandidate.resumeAttachments)">
                <el-icon><Download /></el-icon> 下载预览附件
              </el-button>
            </div>
            <span v-else class="text-muted">未上传附件简历</span>
          </el-descriptions-item>
          <el-descriptions-item label="个人优势">
            <div style="white-space: pre-wrap;">{{ drawerCandidate.selfIntro || '暂无填写' }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <div class="rd-actions mt-4">
          <el-button type="primary" color="#00a6a7" size="large" style="width: 100%" @click="handleInviteClick">
            主动沟通
          </el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 主动沟通表单弹窗 -->
    <el-dialog v-model="inviteDialogVisible" title="发起沟通邀请" width="500px">
      <el-form :model="inviteForm" :rules="inviteRules" ref="inviteFormRef" label-width="80px">
        <el-form-item label="选择职位" prop="jobId">
          <el-select v-model="inviteForm.jobId" placeholder="请选择要沟通的职位" style="width: 100%">
            <el-option
              v-for="job in activeJobs"
              :key="job.id"
              :label="job.title"
              :value="job.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="打招呼" prop="greeting">
          <el-input
            type="textarea"
            v-model="inviteForm.greeting"
            :rows="4"
            placeholder="例如：您好，看了您的简历我们觉得您非常适合这个岗位，希望和您聊聊！"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="inviteDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="inviteLoading" @click="submitInvite">
            发送邀请
          </el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { searchTalents, inviteTalent, getEnterpriseJobs } from '@/api/enterprise'
import { ElMessage } from 'element-plus'
import { Search, Download } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const talents = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

const drawerVisible = ref(false)
const drawerCandidate = ref(null)

const inviteDialogVisible = ref(false)
const inviteFormRef = ref(null)
const inviteLoading = ref(false)
const activeJobs = ref([])
const inviteForm = reactive({
  jobId: null,
  greeting: '您好，看了您的简历我们觉得您非常适合我们的岗位，希望和您聊聊！'
})
const inviteRules = {
  jobId: [{ required: true, message: '请选择一个职位', trigger: 'change' }],
  greeting: [{ required: true, message: '打招呼内容不能为空', trigger: 'blur' }]
}

const fetchTalents = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value
    }
    const res = await searchTalents(params)
    talents.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    ElMessage.error('加载候选人失败')
  } finally {
    loading.value = false
  }
}

const fetchActiveJobs = async () => {
  try {
    // Fetch first 50 jobs to ensure we get most of them for the dropdown
    const res = await getEnterpriseJobs({ page: 1, size: 50 })
    // Only allow inviting to OPEN jobs
    activeJobs.value = (res.content || []).filter(job => job.status === 'OPEN')
  } catch (error) {
    console.error('Failed to load active jobs', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchTalents()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchTalents()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchTalents()
}

const parseSkills = (skillsJson) => {
  if (!skillsJson) return []
  try {
    return JSON.parse(skillsJson)
  } catch {
    return []
  }
}

const openDrawer = (candidate) => {
  drawerCandidate.value = candidate
  drawerVisible.value = true
}

const getResumeUrl = (attachmentsJson) => {
  if (!attachmentsJson) return null
  try {
    const attachments = JSON.parse(attachmentsJson)
    if (attachments && attachments.length > 0) {
      return attachments[0].url
    }
  } catch (e) {
    return null
  }
  return null
}

const downloadResume = (attachmentsJson) => {
  const url = getResumeUrl(attachmentsJson)
  if (url) {
    // Determine the base URL dynamically if it's a relative path starting with /uploads
    const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    const fullUrl = url.startsWith('http') ? url : baseUrl + url
    window.open(fullUrl, '_blank')
  } else {
    ElMessage.warning('候选人未上传附件简历')
  }
}

const handleInviteClick = () => {
  if (activeJobs.value.length === 0) {
    ElMessage.warning('您当前没有招聘中的职位，请先前往职位管理发布职位。')
    return
  }
  // Initialize form
  inviteForm.jobId = activeJobs.value[0]?.id || null
  inviteForm.greeting = `您好 ${drawerCandidate.value?.studentName || ''}，看了您的简历我们觉得您非常适合我们的岗位，希望和您聊聊！`
  inviteDialogVisible.value = true
}

const submitInvite = async () => {
  if (!inviteFormRef.value) return
  await inviteFormRef.value.validate(async (valid) => {
    if (valid) {
      inviteLoading.value = true
      try {
        await inviteTalent({
          studentId: drawerCandidate.value.studentId,
          jobId: inviteForm.jobId,
          greeting: inviteForm.greeting
        })
        ElMessage.success('已发送沟通邀请！')
        inviteDialogVisible.value = false
        drawerVisible.value = false
        // Option A: Refresh talent list (maybe they disappear if already invited?)
        // Option B: Redirect to chat immediately
        router.push('/enterprise/messages')
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '发送邀请失败')
      } finally {
        inviteLoading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchTalents()
  fetchActiveJobs() // Preload jobs for the invite dialog
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
  background: var(--color-bg-card);
  padding: 20px 24px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 8px 0;
}

.header-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.table-card {
  border-radius: 8px;
  border-color: var(--color-border);
}

.candidate-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.c-avatar {
  background-color: var(--color-accent);
  color: #fff;
  font-weight: 600;
  flex-shrink: 0;
}

.c-detail strong {
  font-size: 14px;
  display: block;
  color: var(--color-text-primary);
}

.c-school {
  font-size: 12px;
  margin-top: 2px;
}

.text-muted {
  color: var(--color-text-muted);
  font-size: 13px;
}

.skills-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.skill-tag {
  border-radius: 3px;
}

.salary-text {
  color: var(--color-danger);
  font-weight: bold;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
}

/* === Resume Drawer Styles === */
.resume-drawer {
  padding: 0 8px;
}

.rd-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.rd-avatar {
  background-color: var(--color-accent);
  color: #fff;
  font-size: 24px;
}

.rd-name {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: var(--color-text-primary);
}

.rd-sub {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 14px;
  display: flex;
  align-items: center;
}

.divider {
  display: inline-block;
  width: 1px;
  height: 12px;
  background-color: var(--color-border);
  margin: 0 10px;
}

.mt-4 { margin-top: 24px; }
.mb-4 { margin-bottom: 24px; }
</style>
