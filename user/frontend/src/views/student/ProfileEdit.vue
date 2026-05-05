<template>
  <div class="page">
    <div class="panel">
      <div class="header">
        <div>
          <h1>简历完善</h1>
          <p>支持中文填写、PDF 简历解析和个人意向管理。</p>
        </div>
        <div class="actions">
          <el-button @click="router.push('/student/dashboard')">返回工作台</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">保存简历</el-button>
        </div>
      </div>

      <el-form ref="formRef" :model="form" label-position="top" v-loading="loading">
        <div class="section">
          <div class="section-title">
            <h2>教育背景</h2>
            <el-upload :show-file-list="false" :http-request="handleUpload" accept=".pdf">
              <el-button>上传 PDF 简历并解析</el-button>
            </el-upload>
          </div>

          <div class="grid">
            <el-form-item label="学校名称">
              <el-input v-model="form.university" placeholder="请输入学校名称" />
            </el-form-item>
            <el-form-item label="专业名称">
              <el-input v-model="form.major" placeholder="请输入专业名称" />
            </el-form-item>
            <el-form-item label="学历层次">
              <el-select v-model="form.educationLevel" placeholder="请选择学历">
                <el-option label="本科" value="BACHELOR" />
                <el-option label="硕士" value="MASTER" />
                <el-option label="博士" value="PHD" />
              </el-select>
            </el-form-item>
            <el-form-item label="入学年份">
              <el-input-number v-model="form.enrollmentYear" :min="2000" :max="2100" />
            </el-form-item>
          </div>
        </div>

        <div class="section">
          <div class="section-title">
            <h2>求职意向</h2>
          </div>

          <div class="grid">
            <el-form-item label="期望最低时薪">
              <el-input-number v-model="form.expectedSalaryMin" :min="0" :step="5" />
            </el-form-item>
            <el-form-item label="期望最高时薪">
              <el-input-number v-model="form.expectedSalaryMax" :min="0" :step="5" />
            </el-form-item>
            <el-form-item label="期望地点">
              <el-input v-model="form.expectedLocation" placeholder="例如：上海、远程" />
            </el-form-item>
            <el-form-item label="可工作时间">
              <el-input v-model="form.availableSchedule" placeholder="例如：周一到周五晚间、周末全天" />
            </el-form-item>
          </div>
        </div>

        <div class="section">
          <div class="section-title">
            <h2>技能与介绍</h2>
          </div>

          <el-form-item label="技能标签">
            <div class="tags-box">
              <el-tag v-for="item in skills" :key="item" closable @close="removeSkill(item)">{{ item }}</el-tag>
              <el-input
                v-model="skillInput"
                placeholder="输入技能后按回车"
                class="skill-input"
                @keyup.enter="addSkill"
              />
            </div>
          </el-form-item>

          <el-form-item label="个人介绍">
            <el-input
              v-model="form.selfIntro"
              type="textarea"
              :rows="6"
              placeholder="介绍你的优势、项目经历和可到岗时间"
            />
          </el-form-item>
        </div>

        <div class="section">
          <div class="section-title">
            <h2>隐私设置</h2>
          </div>

          <div class="switch-grid">
            <div class="switch-item">
              <span>公开简历</span>
              <el-switch v-model="form.privacyResumeOpen" />
            </div>
            <div class="switch-item">
              <span>显示姓名</span>
              <el-switch v-model="form.privacyShowName" />
            </div>
            <div class="switch-item">
              <span>显示联系方式</span>
              <el-switch v-model="form.privacyShowContact" />
            </div>
          </div>
        </div>

        <div class="section">
          <div class="section-title">
            <h2>已上传附件</h2>
          </div>
          <el-empty v-if="attachments.length === 0" description="暂无附件" />
          <div v-else class="attachment-list">
            <div v-for="(item, index) in attachments" :key="`${item.url}-${index}`" class="attachment-item">
              <span>{{ item.name || `附件 ${index + 1}` }}</span>
              <div class="attachment-actions">
                <el-button type="primary" link @click="previewResume(item.url)">预览</el-button>
                <el-button type="danger" link @click="removeAttachment(index)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProfile, parseResume, updateProfile } from '@/api/student'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const saving = ref(false)
const skillInput = ref('')
const skills = ref([])
const attachments = ref([])

const form = reactive({
  university: '',
  major: '',
  educationLevel: '',
  enrollmentYear: null,
  expectedSalaryMin: null,
  expectedSalaryMax: null,
  expectedLocation: '',
  availableSchedule: '',
  selfIntro: '',
  privacyResumeOpen: true,
  privacyShowName: true,
  privacyShowContact: false
})

const fillForm = (data = {}) => {
  form.university = data.university || ''
  form.major = data.major || ''
  form.educationLevel = data.educationLevel || ''
  form.enrollmentYear = data.enrollmentYear || null
  form.expectedSalaryMin = data.expectedSalaryMin || null
  form.expectedSalaryMax = data.expectedSalaryMax || null
  form.expectedLocation = data.expectedLocation || ''
  form.availableSchedule = data.availableSchedule || ''
  form.selfIntro = data.selfIntro || ''
  form.privacyResumeOpen = data.privacyResumeOpen ?? true
  form.privacyShowName = data.privacyShowName ?? true
  form.privacyShowContact = data.privacyShowContact ?? false

  try {
    skills.value = JSON.parse(data.skills || '[]')
  } catch (error) {
    skills.value = []
  }
  try {
    attachments.value = JSON.parse(data.resumeAttachments || '[]')
  } catch (error) {
    attachments.value = []
  }
}

const loadProfile = async () => {
  loading.value = true
  try {
    const data = await getProfile()
    fillForm(data)
  } finally {
    loading.value = false
  }
}

const addSkill = () => {
  const value = skillInput.value.trim()
  if (!value) return
  if (!skills.value.includes(value)) {
    skills.value.push(value)
  }
  skillInput.value = ''
}

const removeSkill = (value) => {
  skills.value = skills.value.filter((item) => item !== value)
}

const handleUpload = async (options) => {
  const data = await parseResume(options.file)
  if (data.university && !form.university) form.university = data.university
  if (data.selfIntro && !form.selfIntro) form.selfIntro = data.selfIntro
  ;(data.skills || []).forEach((item) => {
    if (!skills.value.includes(item)) skills.value.push(item)
  })
  attachments.value.push({
    name: data.fileName || options.file.name,
    url: data.fileUrl
  })
  ElMessage.success('简历解析成功')
}

const previewResume = (url) => {
  if (!url) return
  const target = url.startsWith('http') ? url : `http://localhost:8080${url}`
  window.open(target, '_blank')
}

const removeAttachment = (index) => {
  attachments.value.splice(index, 1)
}

const handleSave = async () => {
  saving.value = true
  try {
    await updateProfile({
      ...form,
      skills: JSON.stringify(skills.value),
      resumeAttachments: JSON.stringify(attachments.value)
    })
    ElMessage.success('简历保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.page {
  padding: 32px 20px;
}

.panel {
  max-width: 1100px;
  margin: 0 auto;
  background: #fff;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 10px 40px rgba(27, 44, 51, 0.08);
}

.header,
.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.header {
  margin-bottom: 20px;
}

.header p {
  color: var(--color-text-secondary);
}

.actions {
  display: flex;
  gap: 12px;
}

.section {
  margin-bottom: 20px;
  padding: 22px;
  background: #f7fbfb;
  border-radius: 18px;
  border: 1px solid rgba(0, 166, 167, 0.1);
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.tags-box {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background: #fff;
}

.skill-input {
  width: 220px;
}

.switch-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.switch-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 16px;
  border-radius: 12px;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.attachment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: #fff;
  border-radius: 12px;
}

.attachment-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 900px) {
  .header,
  .section-title {
    flex-direction: column;
    align-items: flex-start;
  }

  .actions {
    width: 100%;
    flex-direction: column;
  }

  .grid,
  .switch-grid {
    grid-template-columns: 1fr;
  }
}
</style>
