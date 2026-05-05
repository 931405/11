<template>
  <div class="candidate-list-container">
    <el-page-header @back="router.back()" class="page-header">
      <template #content>
        <span class="header-title">候选人管理 (职位ID: {{ jobId }})</span>
      </template>
    </el-page-header>

    <div class="main-content" v-loading="loading">
      <el-card shadow="never" class="table-card">
        <el-empty v-if="total === 0" description="暂无求职者投递" />
        
        <div v-else class="candidates-list">
          <el-table
            :data="candidates"
            style="width: 100%"
            :header-cell-style="{ background: 'var(--color-bg-secondary)', color: 'var(--color-text-primary)' }"
          >
            <el-table-column label="候选人" min-width="200">
              <template #default="scope">
                <div class="candidate-info">
                  <el-avatar :size="36" class="c-avatar">{{ scope.row.studentName?.charAt(0) || '?' }}</el-avatar>
                  <div class="c-detail">
                    <strong>{{ scope.row.studentName || '未知用户' }}</strong>
                    <div class="c-school text-muted">{{ scope.row.university || '未填写' }} · {{ scope.row.major || '' }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column label="学历/入学年" width="130" align="center">
              <template #default="scope">
                <span>{{ scope.row.educationLevel || '-' }}</span>
                <div class="text-muted" v-if="scope.row.enrollmentYear">{{ scope.row.enrollmentYear }}级</div>
              </template>
            </el-table-column>

            <el-table-column label="技能标签" min-width="180">
              <template #default="scope">
                <div class="skills-cell">
                  <el-tag 
                    v-for="skill in parseSkills(scope.row.skills)" 
                    :key="skill" 
                    size="small" 
                    type="info"
                    class="skill-tag"
                  >{{ skill }}</el-tag>
                  <span v-if="!parseSkills(scope.row.skills).length" class="text-muted">未填写</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="投递时间" width="150" align="center">
              <template #default="scope">
                <span class="text-muted">{{ formatDate(scope.row.appliedAt) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="当前状态" width="110" align="center">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)" effect="dark">
                  {{ getStatusLabel(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="290" fixed="right">
              <template #default="scope">
                <div class="action-buttons">
                  <el-button type="primary" link @click="openDrawer(scope.row)">
                    <el-icon><View /></el-icon> 查看简历
                  </el-button>
                  
                  <template v-if="scope.row.status === 'APPLIED' || scope.row.status === 'REVIEWING'">
                    <el-button 
                      v-if="scope.row.status === 'APPLIED'"
                      type="primary" 
                      plain
                      size="small"
                      @click="handleStatusChange(scope.row.applicationId, 'REVIEWING')"
                    >
                      标记审阅中
                    </el-button>
                    <el-button 
                      type="warning" 
                      plain
                      size="small"
                      @click="openInterviewDialog(scope.row)"
                    >
                      邀请面试
                    </el-button>
                    <el-button 
                      type="danger" 
                      plain
                      size="small"
                      @click="openActionDialog(scope.row.applicationId, 'REJECTED')"
                    >
                      回绝
                    </el-button>
                  </template>
                  
                  <template v-else-if="scope.row.status === 'INTERVIEW'">
                    <el-button 
                      type="success" 
                      size="small"
                      @click="openActionDialog(scope.row.applicationId, 'ACCEPTED')"
                    >
                      确认录用
                    </el-button>
                    <el-button 
                      type="danger" 
                      plain
                      size="small"
                      @click="openActionDialog(scope.row.applicationId, 'REJECTED')"
                    >
                      不合适
                    </el-button>
                  </template>
                  
                  <span v-else class="text-muted">已处理完成</span>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="page"
              v-model:page-size="size"
              :page-sizes="[10, 20]"
              background
              layout="total, sizes, prev, pager, next"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </el-card>
    </div>

    <!-- 处理操作弹窗 (带备注) -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <el-form label-position="top">
        <el-form-item label="给候选人的反馈/备注">
          <el-input 
            v-model="actionRemark" 
            type="textarea" 
            :rows="3" 
            :placeholder="targetStatus === 'INTERVIEW' ? '请留下您的联系方式或面试时间地点...' : '给候选人一些反馈...'" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button :type="dialogBtnType" :loading="submitLoading" @click="submitStatusChange">
            确认操作
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 发送面试邀请弹窗 -->
    <el-dialog v-model="interviewDialogVisible" title="发送面试邀请" width="480px">
      <el-form :model="interviewForm" :rules="interviewRules" ref="interviewFormRef" label-width="100px">
        <el-form-item label="面试时间" prop="interviewTime">
          <el-date-picker
            v-model="interviewForm.interviewTime"
            type="datetime"
            placeholder="选择面试时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="面试地点" prop="location">
          <el-input v-model="interviewForm.location" placeholder="请输入面试地点或在线会议链接" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="interviewForm.contact" placeholder="例如：张女士 138xxxxXXXX" />
        </el-form-item>
        <el-form-item label="附加信息" prop="message">
          <el-input 
            v-model="interviewForm.message" 
            type="textarea" 
            :rows="3" 
            placeholder="需要带什么材料，或其他注意事项..." 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="interviewDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="interviewSubmitting" @click="submitInterviewInvitation">
            发送邀请
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 简历详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="候选人简历详情" size="480px" direction="rtl">
      <div v-if="drawerCandidate" class="resume-drawer">
        <!-- Header -->
        <div class="resume-header">
          <el-avatar :size="56" class="resume-avatar">{{ drawerCandidate.studentName?.charAt(0) || '?' }}</el-avatar>
          <div class="resume-name-block">
            <h3>{{ drawerCandidate.studentName || '未知用户' }}</h3>
            <p class="text-muted">{{ drawerCandidate.university || '未填写' }} · {{ drawerCandidate.major || '' }}</p>
          </div>
          <el-tag :type="getStatusType(drawerCandidate.status)" effect="dark" class="resume-status-tag">
            {{ getStatusLabel(drawerCandidate.status) }}
          </el-tag>
        </div>

        <el-divider />

        <!-- 基本信息 -->
        <div class="resume-section">
          <h4><el-icon><User /></el-icon> 基本信息</h4>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="学历">{{ drawerCandidate.educationLevel || '-' }}</el-descriptions-item>
            <el-descriptions-item label="入学年份">{{ drawerCandidate.enrollmentYear ? drawerCandidate.enrollmentYear + '级' : '-' }}</el-descriptions-item>
            <el-descriptions-item label="期望薪资">
              <span v-if="drawerCandidate.expectedSalaryMin || drawerCandidate.expectedSalaryMax" class="salary-text">
                {{ drawerCandidate.expectedSalaryMin || '?' }} - {{ drawerCandidate.expectedSalaryMax || '?' }} 元/天
              </span>
              <span v-else>未填写</span>
            </el-descriptions-item>
            <el-descriptions-item label="期望地点">{{ drawerCandidate.expectedLocation || '-' }}</el-descriptions-item>
            <el-descriptions-item label="可用时间">{{ drawerCandidate.availableSchedule || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 技能标签 -->
        <div class="resume-section">
          <h4><el-icon><Promotion /></el-icon> 技能标签</h4>
          <div class="skills-cell" v-if="parseSkills(drawerCandidate.skills).length">
            <el-tag v-for="skill in parseSkills(drawerCandidate.skills)" :key="skill" class="skill-tag">{{ skill }}</el-tag>
          </div>
          <p v-else class="text-muted">该候选人尚未填写技能标签</p>
        </div>

        <!-- 附件简历 -->
        <div class="resume-section">
          <h4><el-icon><Document /></el-icon> 附件简历</h4>
          <div v-if="getResumeUrl(drawerCandidate.resumeAttachments)">
            <el-button type="primary" plain size="small" @click="downloadResume(drawerCandidate.resumeAttachments)">
              <el-icon><Download /></el-icon> 下载预览附件
            </el-button>
          </div>
          <p v-else class="text-muted">未上传附件简历</p>
        </div>

        <!-- 自我介绍 -->
        <div class="resume-section">
          <h4><el-icon><EditPen /></el-icon> 自我介绍</h4>
          <p class="self-intro">{{ drawerCandidate.selfIntro || '该候选人未填写自我介绍' }}</p>
        </div>

        <!-- 投递留言 -->
        <div class="resume-section">
          <h4><el-icon><ChatLineSquare /></el-icon> 投递留言</h4>
          <el-alert type="info" :closable="false" show-icon>
            <template #default>
              <span>{{ drawerCandidate.applyMessage || '无附加信息' }}</span>
            </template>
          </el-alert>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getJobCandidates, updateApplicationStatus, sendInterviewInvitation } from '@/api/enterprise'
import { ElMessage } from 'element-plus'
import { Download, Document, View, User, Promotion, EditPen, ChatLineSquare } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const jobId = route.params.id

const loading = ref(false)
const candidates = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

// Dialog variables
const dialogVisible = ref(false)
const submitLoading = ref(false)
const currentAppId = ref(null)
const targetStatus = ref('')
const actionRemark = ref('')

// Interview Dialog variables
const interviewDialogVisible = ref(false)
const interviewSubmitting = ref(false)
const interviewFormRef = ref(null)
const interviewForm = ref({
  applicationId: null,
  interviewTime: '',
  location: '',
  contact: '',
  message: ''
})
const interviewRules = {
  interviewTime: [{ required: true, message: '请选择面试时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入面试地点', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系人信息', trigger: 'blur' }]
}

// Drawer variables
const drawerVisible = ref(false)
const drawerCandidate = ref(null)

const fetchCandidates = async () => {
  loading.value = true
  try {
    const res = await getJobCandidates(jobId, { page: page.value, size: size.value })
    candidates.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to load candidates', error)
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  size.value = val
  fetchCandidates()
}

const handleCurrentChange = (val) => {
  page.value = val
  fetchCandidates()
}

const handleStatusChange = async (appId, status, remark = '') => {
  try {
    await updateApplicationStatus(appId, status, remark)
    ElMessage.success('状态更新成功')
    fetchCandidates()
  } catch (error) {
    // Handled globally
  }
}

const openActionDialog = (appId, status) => {
  currentAppId.value = appId
  targetStatus.value = status
  actionRemark.value = ''
  dialogVisible.value = true
}

const submitStatusChange = async () => {
  submitLoading.value = true
  try {
    await updateApplicationStatus(currentAppId.value, targetStatus.value, actionRemark.value)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    fetchCandidates()
  } catch (error) {
    // Handled globally
  } finally {
    submitLoading.value = false
  }
}

const openInterviewDialog = (candidate) => {
  interviewForm.value = {
    applicationId: candidate.applicationId,
    interviewTime: '',
    location: '',
    contact: '',
    message: ''
  }
  interviewDialogVisible.value = true
  // clear validation if any
  if (interviewFormRef.value) {
    interviewFormRef.value.clearValidate()
  }
}

const submitInterviewInvitation = async () => {
  if (!interviewFormRef.value) return
  await interviewFormRef.value.validate(async (valid) => {
    if (valid) {
      interviewSubmitting.value = true
      try {
        await sendInterviewInvitation(interviewForm.value)
        ElMessage.success('面试邀请发送成功')
        interviewDialogVisible.value = false
        fetchCandidates()
      } catch (error) {
        // Handled globally
      } finally {
        interviewSubmitting.value = false
      }
    }
  })
}

const openDrawer = (candidate) => {
  drawerCandidate.value = candidate
  drawerVisible.value = true
}

const parseSkills = (skillsJson) => {
  if (!skillsJson) return []
  try {
    return JSON.parse(skillsJson)
  } catch {
    return []
  }
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
    const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    const fullUrl = url.startsWith('http') ? url : baseUrl + url
    window.open(fullUrl, '_blank')
  } else {
    ElMessage.warning('候选人未上传附件简历')
  }
}

const dialogTitle = computed(() => {
  const map = {
    'INTERVIEW': '邀请面试',
    'REJECTED': '回绝候选人',
    'ACCEPTED': '确认录用'
  }
  return map[targetStatus.value] || '处理申请'
})

const dialogBtnType = computed(() => {
  if (targetStatus.value === 'INTERVIEW') return 'warning'
  if (targetStatus.value === 'REJECTED') return 'danger'
  if (targetStatus.value === 'ACCEPTED') return 'success'
  return 'primary'
})

const formatDate = (dateString) => {
  if (!dateString) return ''
  return dayjs(dateString).format('YYYY-MM-DD HH:mm')
}

const getStatusType = (status) => {
  const map = {
    'APPLIED': 'info',
    'REVIEWING': 'warning',
    'INTERVIEW': 'primary',
    'ACCEPTED': 'success',
    'REJECTED': 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = {
    'APPLIED': '新投递',
    'REVIEWING': '审阅中',
    'INTERVIEW': '面试中',
    'ACCEPTED': '已录用',
    'REJECTED': '已淘汰'
  }
  return map[status] || status
}

onMounted(() => {
  fetchCandidates()
})
</script>

<style scoped>
.page-header {
  margin-bottom: 24px;
  background: var(--color-bg-card);
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.header-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
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

.action-buttons {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
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

.resume-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.resume-avatar {
  background-color: var(--color-accent);
  color: #fff;
  font-size: 22px;
  font-weight: 600;
  flex-shrink: 0;
}

.resume-name-block h3 {
  margin: 0 0 4px;
  font-size: 18px;
  color: var(--color-text-primary);
}

.resume-name-block p {
  margin: 0;
}

.resume-status-tag {
  margin-left: auto;
}

.resume-section {
  margin-bottom: 24px;
}

.resume-section h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 12px;
}

.resume-section h4 .el-icon {
  color: var(--color-accent);
}

.self-intro {
  color: var(--color-text-secondary);
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
  background: var(--color-bg-secondary);
  padding: 12px 16px;
  border-radius: 6px;
}
</style>
