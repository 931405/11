<template>
  <div class="profile-edit-page" v-loading.fullscreen.lock="parseLoading" element-loading-text="正在使用后台技术为您智能抽取简历信息...">
    <div class="resume-wrapper">
      
      <!-- Left Sidebar: Anchor Menu -->
      <aside class="sidebar-left">
        <div class="sidebar-title">简历目录</div>
        <ul class="anchor-menu">
          <li v-for="item in sidebarMenuItems" :key="item.id" class="menu-item" :class="{ active: activeSidebarItem === item.id }" @click="scrollToSection(item.id)">{{ item.label }}</li>
          <el-divider />
          <el-dropdown trigger="click" placement="bottom-start" popper-class="custom-add-dropdown">
            <div class="sub-title dropdown-trigger">
              自定义添加 <el-icon><arrow-down/></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item class="custom-add-item" @click="addCustomSection('certifications')">
                  <span class="item-text">资格证书</span>
                  <span class="tag-rec">推荐</span>
                </el-dropdown-item>
                <el-dropdown-item class="custom-add-item" @click="addCustomSection('clubs')">
                  <span class="item-text">社团/组织...</span>
                  <span class="tag-rec">推荐</span>
                </el-dropdown-item>
                <el-dropdown-item class="custom-add-item" @click="addCustomSection('volunteers')">
                  <span class="item-text">志愿者经历</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </ul>
      </aside>

      <!-- Center Content: Main Form -->
      <main class="main-content" v-loading="loading">
        <div class="resume-top-banner">
          <div class="score-area">
            <el-avatar size="large" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <span class="score-text">简历专业评分<span class="score-num">{{ resumeScore }}</span>分</span>
          </div>
          <div class="opt-hint clickable-hint" @click="showOptimizationDialog = true" v-if="optimizationItems.length > 0">
            {{ optimizationItems.length }}个待优化项 <el-icon><arrow-right/></el-icon>
          </div>
          <div class="opt-hint text-success" v-else>
            简历已非常完善 <el-icon><Select/></el-icon>
          </div>
        </div>

        <el-form 
          ref="formRef" 
          :model="form" 
          :rules="rules" 
          label-position="top"
          class="profile-form"
        >
          <!-- 个人信息 & 教育经历 (Combined for layout sim) -->
          <div class="form-section" id="section-info">
            <h3 class="section-title">编辑个人/教育信息</h3>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="学校名称" prop="university">
                  <el-input v-model="form.university" placeholder="例如：清华大学" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="当前求职状态">
                  <el-select v-model="form.educationLevel" class="w-100" placeholder="在校-月内到岗">
                    <el-option label="在校-月内到岗" value="BACHELOR" />
                    <el-option label="在校-随时到岗" value="MASTER" />
                    <el-option label="应届生-随时到岗" value="PHD" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="专业名称" prop="major">
                  <el-input v-model="form.major" placeholder="例如：计算机科学与技术" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="入学年份">
                  <el-date-picker
                    v-model="form.enrollmentYear"
                    type="year"
                    placeholder="选择入学年份"
                    value-format="YYYY"
                    class="w-100"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- 求职意向 -->
          <div class="form-section mt-24" id="section-intent">
            <h3 class="section-title"><span class="title-decor"></span> 求职意向</h3>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="期望时薪区间（元/小时）">
                  <div class="salary-range">
                    <el-input-number v-model="form.expectedSalaryMin" :min="0" :step="5" placeholder="最低" controls-position="right" class="w-100" />
                    <span class="separator">-</span>
                    <el-input-number v-model="form.expectedSalaryMax" :min="form.expectedSalaryMin || 0" :step="5" placeholder="最高" controls-position="right" class="w-100" />
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="期望工作地点">
                  <el-input v-model="form.expectedLocation" placeholder="匹配算法将基于此推算，如：海淀区/朝阳区" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="可用时间安排">
                  <el-input 
                    v-model="form.availableSchedule" 
                    placeholder="例如：周一周三全天，周末下午" 
                    maxlength="200" 
                    show-word-limit
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- 个人优势 & 技能 -->
          <div class="form-section mt-24" id="section-skills">
            <h3 class="section-title"><span class="title-decor"></span> 个人优势 / 专业技能</h3>
            <el-row :gutter="24">
              <el-col :span="24">
                <el-form-item label="个人技能标签（输入后按回车添加）">
                  <div class="skills-input-container">
                    <el-tag
                      v-for="tag in skillTags"
                      :key="tag"
                      class="skill-tag"
                      closable
                      :disable-transitions="false"
                      @close="handleCloseTag(tag)"
                      effect="plain"
                    >
                      {{ tag }}
                    </el-tag>
                    <el-input
                      v-if="inputVisible"
                      ref="InputRef"
                      v-model="inputValue"
                      class="skill-input"
                      size="small"
                      @keyup.enter="handleInputConfirm"
                      @blur="handleInputConfirm"
                    />
                    <el-button v-else class="button-new-tag" size="small" @click="showInput">
                      + 添加标签
                    </el-button>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="自我介绍">
                  <el-input
                    v-model="form.selfIntro"
                    type="textarea"
                    :rows="5"
                    placeholder="介绍一下你的特长、经验以及为什么适合兼职工作..."
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- 自定义板块：资格证书 -->
          <div v-if="customSections.certifications" class="form-section mt-24" id="section-certifications">
            <h3 class="section-title">
              <div><span class="title-decor"></span> 资格证书</div>
              <el-button link type="danger" @click="removeCustomSection('certifications')">删除</el-button>
            </h3>
            <el-row :gutter="24">
              <el-col :span="24">
                <el-form-item label="证书名称">
                  <el-input v-model="form.certifications" placeholder="例如：大学英语六级、CPA等" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- 自定义板块：社团/组织 -->
          <div v-if="customSections.clubs" class="form-section mt-24" id="section-clubs">
            <h3 class="section-title">
              <div><span class="title-decor"></span> 社团/组织经历</div>
              <el-button link type="danger" @click="removeCustomSection('clubs')">删除</el-button>
            </h3>
            <el-row :gutter="24">
              <el-col :span="24">
                <el-form-item label="社团名称及职务">
                  <el-input v-model="form.clubs" placeholder="例如：学生会 外联部部长" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="经历描述">
                  <el-input type="textarea" :rows="3" v-model="form.clubsDesc" placeholder="简述你在社团中的主要工作和成就..." />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- 自定义板块：志愿者经历 -->
          <div v-if="customSections.volunteers" class="form-section mt-24" id="section-volunteers">
            <h3 class="section-title">
              <div><span class="title-decor"></span> 志愿者经历</div>
              <el-button link type="danger" @click="removeCustomSection('volunteers')">删除</el-button>
            </h3>
            <el-row :gutter="24">
              <el-col :span="24">
                <el-form-item label="志愿活动名称">
                  <el-input v-model="form.volunteers" placeholder="例如：2023国际马拉松志愿者" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="经历描述">
                  <el-input type="textarea" :rows="3" v-model="form.volunteersDesc" placeholder="描述你的志愿服务内容..." />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <div class="form-actions mt-24">
            <el-button @click="$router.push('/student/dashboard')">取消</el-button>
            <el-button color="#00a6a7" :loading="submitLoading" @click="saveProfile">
              完成并保存
            </el-button>
          </div>
        </el-form>
      </main>

      <!-- Right Sidebar: Widgets -->
      <aside class="sidebar-right">
        <div class="widget-card attachments-card">
          <div class="widget-header">
            <h4>附件管理</h4>
            <el-upload
              class="add-upload"
              action="#"
              :http-request="uploadAndParse"
              :show-file-list="false"
              :limit="3"
              :on-exceed="handleExceed"
              accept=".pdf"
            >
              <el-icon class="add-icon" v-if="fileList.length < 3"><Plus/></el-icon>
            </el-upload>
          </div>
          <div class="file-count">文件 ({{ fileList.length }}/3)</div>
          
          <div v-if="fileList.length > 0" class="attachment-list">
            <div v-for="(file, index) in fileList" :key="index" class="attachment-item">
              <el-icon class="file-icon"><Document /></el-icon>
              <span class="file-name">{{ file.name }}</span>
              <el-icon class="remove-icon" @click="removeFile(index)"><Close /></el-icon>
            </div>
          </div>
          <div v-else class="empty-tip">暂无附件，点击右上角添加</div>
        </div>

        <div class="widget-card privacy-card">
          <div class="widget-header">
            <h4>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-2px;margin-right:6px;"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
              隐私设置
            </h4>
            <router-link to="/student/privacy" class="settings-link">设置</router-link>
          </div>
          <div class="privacy-item">
            <span class="privacy-label">简历公开</span>
            <el-switch v-model="privacyResumeOpen" active-color="#00a6a7" size="small" />
          </div>
          <div class="privacy-item">
            <span class="privacy-label">显示真实姓名</span>
            <el-switch v-model="privacyShowName" active-color="#00a6a7" size="small" />
          </div>
          <div class="privacy-item">
            <span class="privacy-label">显示联系方式</span>
            <el-switch v-model="privacyShowContact" active-color="#00a6a7" size="small" />
          </div>
          <div class="privacy-item">
            <span class="privacy-label">屏蔽公司</span>
            <span class="value">已屏蔽0个</span>
          </div>
        </div>
      </aside>

    </div>

    <!-- Optimization Dialog -->
    <el-dialog v-model="showOptimizationDialog" title="简历优化建议" width="480px">
      <div class="opt-dialog-content">
        <div class="opt-dialog-header">
          <div class="current-score">
            当前评分: <span>{{ resumeScore }}</span> 分
          </div>
          <div class="score-ring-wrap">
            <el-progress type="dashboard" :percentage="resumeScore" :color="scoreColor" :width="80" />
          </div>
        </div>
        <div class="opt-list-title">您可以继续完善以下内容：</div>
        <div class="opt-list">
          <div class="opt-item" v-for="(item, index) in optimizationItems" :key="index">
            <div class="opt-info">
              <span class="opt-label">{{ item.label }}</span>
              <span class="opt-points">+{{ item.points }}分</span>
            </div>
            <p class="opt-desc">{{ item.desc }}</p>
            <el-button size="small" type="primary" plain @click="goToSectionAndCloseDialog(item.elementId)">
              去完善
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { getProfile, updateProfile } from '@/api/student'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { Plus, Document, Close, ArrowRight, Select } from '@element-plus/icons-vue'

const userStore = useUserStore()

const customSections = reactive({
  certifications: false,
  clubs: false,
  volunteers: false
})

const addCustomSection = (key) => {
  customSections[key] = true
  // Optional: scroll to the new section
  setTimeout(() => scrollToSection('section-' + key), 100)
}

const removeCustomSection = (key) => {
  customSections[key] = false
}

const fileList = ref([])
const parseLoading = ref(false)

// Privacy widget toggles
const privacyResumeOpen = ref(true)
const privacyShowName = ref(true)
const privacyShowContact = ref(false)

const uploadAndParse = async (options) => {
  const file = options.file
  if (fileList.value.length >= 3) {
    ElMessage.warning('最多只能上传 3 个附件')
    return
  }
  
  parseLoading.value = true
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const res = await request({
      url: '/api/student/resume/parse',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    // Add to file list
    const fileItem = {
      name: res.fileName || file.name,
      url: res.fileUrl || '',
      size: (file.size / 1024).toFixed(1) + ' KB',
      uid: file.uid
    }
    fileList.value.push(fileItem)

    // Sync to form
    form.resumeAttachments = JSON.stringify(fileList.value)
    
    ElMessage.success('PDF解析成功，已自动填充相关字段！')
    
    // Auto fill form
    if (res.university && !form.university) form.university = res.university
    if (res.selfIntro && !form.selfIntro) form.selfIntro = res.selfIntro
    if (res.skills && res.skills.length > 0) {
      res.skills.forEach(s => {
        if (!skillTags.value.includes(s)) {
          skillTags.value.push(s)
        }
      })
    }
  } catch (error) {
    console.error('Failed to parse PDF', error)
    ElMessage.error('解析失败，请手动填写相关信息')
  } finally {
    parseLoading.value = false
  }
}

const removeFile = (index) => {
  fileList.value.splice(index, 1)
  form.resumeAttachments = JSON.stringify(fileList.value)
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传 3 个附件，请先删除已有附件')
}

const loading = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

// Sidebar navigation
const sidebarMenuItems = [
  { id: 'section-info', label: '个人信息' },
  { id: 'section-intent', label: '求职意向' },
  { id: 'section-skills', label: '个人优势' },
]
const activeSidebarItem = ref('section-info')

const scrollToSection = (sectionId) => {
  activeSidebarItem.value = sectionId
  const el = document.getElementById(sectionId)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// Tags handling
const inputValue = ref('')
const skillTags = ref([])
const inputVisible = ref(false)
const InputRef = ref()

const form = reactive({
  university: '',
  major: '',
  educationLevel: '',
  enrollmentYear: '',
  expectedSalaryMin: null,
  expectedSalaryMax: null,
  expectedLocation: '',
  availableSchedule: '',
  skills: '[]',
  selfIntro: '',
  certifications: '',
  clubs: '',
  clubsDesc: '',
  volunteers: '',
  volunteersDesc: '',
  resumeAttachments: '[]'
})

// === Optimization Logic ===
const showOptimizationDialog = ref(false)

const optimizationItems = computed(() => {
  const items = []
  
  if (!form.university || !form.major) {
    items.push({ label: '教育经历不完整', desc: '请填写学校名称和专业。', points: 15, elementId: 'section-info' })
  }
  if (!form.expectedLocation || !form.expectedSalaryMin) {
    items.push({ label: '求职意向缺失', desc: '明确的工作城市和期望薪资能帮助精确匹配岗位。', points: 15, elementId: 'section-intent' })
  }
  if (!form.selfIntro || form.selfIntro.length < 20) {
    items.push({ label: '个人优势偏短', desc: '详细的自我介绍能让企业更好地了解你（建议不少于20字）。', points: 20, elementId: 'section-skills' })
  }
  if (skillTags.value.length === 0) {
    items.push({ label: '缺少技能标签', desc: '添加相关专业技能、软技能标签能提高简历曝光。', points: 15, elementId: 'section-skills' })
  }
  if (!form.certifications && !form.clubs && !form.volunteers) {
    items.push({ label: '经历较单薄', desc: '你可以尝试添加一些证书、校园社团经历或志愿者经历。', points: 15, elementId: 'section-skills' }) /* Using skills as nearest section */
  }
  
  return items
})

const resumeScore = computed(() => {
  let score = 100
  optimizationItems.value.forEach(item => {
    score -= item.points
  })
  return Math.max(score, 20)
})

const scoreColor = computed(() => {
  if (resumeScore.value < 60) return '#f56c6c'
  if (resumeScore.value < 80) return '#e6a23c'
  return '#00a6a7'
})

const goToSectionAndCloseDialog = (elementId) => {
  showOptimizationDialog.value = false
  setTimeout(() => scrollToSection(elementId), 300)
}
// ==========================

const rules = {
  university: [{ required: true, message: '请输入学校名称', trigger: 'blur' }],
  major: [{ required: true, message: '请输入专业名称', trigger: 'blur' }]
}

const fetchProfile = async () => {
  loading.value = true
  try {
    const rawData = await getProfile()
    if (rawData) {
      // populate form
      Object.keys(form).forEach(key => {
        if (rawData[key] !== undefined && rawData[key] !== null) {
          form[key] = rawData[key]
        }
      })
      // parse skills json array
      try {
        skillTags.value = JSON.parse(rawData.skills || '[]')
      } catch (e) {
        skillTags.value = []
      }
      // parse attachments json
      try {
        fileList.value = JSON.parse(rawData.resumeAttachments || '[]')
      } catch (e) {
        fileList.value = []
      }
    }
  } catch (error) {
    console.error('Failed to load profile', error)
  } finally {
    loading.value = false
  }
}

const handleCloseTag = (tag) => {
  skillTags.value.splice(skillTags.value.indexOf(tag), 1)
}

const showInput = () => {
  inputVisible.value = true
  nextTick(() => {
    InputRef.value.input.focus()
  })
}

const handleInputConfirm = () => {
  if (inputValue.value) {
    if (!skillTags.value.includes(inputValue.value)) {
      skillTags.value.push(inputValue.value)
    } else {
      ElMessage.warning('标签已存在')
    }
  }
  inputVisible.value = false
  inputValue.value = ''
}

const saveProfile = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // serialize skills and attachments
        const payload = { ...form }
        payload.skills = JSON.stringify(skillTags.value)
        payload.resumeAttachments = JSON.stringify(fileList.value)
        if (payload.enrollmentYear && typeof payload.enrollmentYear === 'string') {
          payload.enrollmentYear = parseInt(payload.enrollmentYear)
        }
        
        await updateProfile(payload)
        ElMessage.success('简历保存成功，系统正为您重新计算匹配职位')
      } catch (error) {
        // Handled globally
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.resume-layout {
  min-height: 100%;
  padding: 20px 0 80px 0;
}

.resume-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

/* --- Left Sidebar --- */
.sidebar-left {
  width: 180px;
  background: white;
  border-radius: 8px;
  padding: 20px 0;
  box-shadow: 0 1px 4px rgba(0,0,0,0.02);
  position: sticky;
  top: 70px;
}
.sidebar-title {
  font-weight: bold;
  font-size: 16px;
  color: var(--color-text-primary);
  padding: 0 20px 16px 20px;
}
.anchor-menu {
  list-style: none;
}
.menu-item {
  padding: 12px 20px;
  font-size: 14px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.menu-item:hover {
  background-color: var(--color-bg-secondary);
  color: var(--color-accent);
}
.menu-item.active {
  color: var(--color-accent);
  font-weight: 500;
  background-color: rgba(0,166,167,0.05);
  border-right: 3px solid var(--color-accent);
}
.privacy-item .value {
  font-size: 14px;
  color: var(--color-text-secondary);
}

/* --- Optimization Dialog --- */
.clickable-hint {
  cursor: pointer;
  transition: opacity 0.2s;
  color: var(--color-accent);
}
.clickable-hint:hover {
  opacity: 0.8;
}
.text-success {
  color: #67c23a;
}
.opt-dialog-content {
  padding: 0 10px;
}
.opt-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.current-score {
  font-size: 16px;
  color: var(--color-text-secondary);
}
.current-score span {
  font-size: 36px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 8px;
}
.opt-list-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 16px;
}
.opt-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.opt-item {
  background: var(--color-bg-light);
  border-radius: 8px;
  padding: 16px;
  position: relative;
}
.opt-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.opt-label {
  font-weight: 600;
  color: var(--color-text-primary);
}
.opt-points {
  color: #e6a23c;
  font-weight: 600;
}
.opt-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin: 0 0 12px 0;
  line-height: 1.5;
}
.sub-title {
  font-size: 14px;
  color: var(--color-text-secondary);
  padding: 8px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.dropdown-trigger {
  cursor: pointer;
  width: 100%;
}
.dropdown-trigger:hover {
  background-color: var(--color-bg-light);
  border-radius: 6px;
}
:deep(.custom-add-dropdown) {
  width: 180px;
  border-radius: 8px;
  padding: 8px 0;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
:deep(.custom-add-item) {
  display: flex !important;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px !important;
  font-size: 15px;
  color: #333;
}
:deep(.custom-add-item:hover) {
  background-color: #f7f9fa !important;
  color: var(--color-accent) !important;
}
.item-text {
  flex: 1;
}
.tag-rec {
  margin-left: 8px;
  font-size: 12px;
  color: #00a6a7;
  background-color: #e8fbfa;
  padding: 2px 6px;
  border-radius: 4px;
}

/* --- Center Content --- */
.main-content {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.02);
  padding: 24px 32px;
}

.resume-top-banner {
  background: linear-gradient(135deg, #e0f2f1, #e6f7ff);
  padding: 20px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}
.score-area {
  display: flex;
  align-items: center;
  gap: 16px;
}
.score-text {
  font-size: 16px;
  color: #1a4d4e;
  font-weight: 500;
}
.score-num {
  font-size: 32px;
  color: #00a6a7;
  font-weight: bold;
  margin: 0 4px;
}
.opt-hint {
  font-size: 14px;
  color: #7d9e9e;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.form-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px 0;
  border-bottom: 1px dashed var(--color-border);
}
.section-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  color: var(--color-text-primary);
  margin-bottom: 24px;
}
.title-decor {
  width: 4px;
  height: 16px;
  background-color: var(--color-accent);
  border-radius: 2px;
  margin-right: 8px;
}

.mt-24 {
  margin-top: 10px;
}
.w-100 {
  width: 100%;
}
.salary-range {
  display: flex;
  align-items: center;
  gap: 12px;
}
.separator {
  color: var(--color-text-secondary);
}

.skills-input-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background-color: var(--color-bg-secondary);
  min-height: 52px;
}
.skill-tag {
  background-color: #fff;
  color: var(--color-text-primary);
  border-color: var(--color-border);
}
.skill-input {
  width: 120px;
}
.button-new-tag {
  color: var(--color-accent);
  background: transparent;
  border: 1px dashed var(--color-border);
}
.button-new-tag:hover {
  background-color: rgba(0, 166, 167, 0.05);
  border-color: var(--color-accent);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding-top: 24px;
  border-top: none;
}

/* --- Right Sidebar --- */
.sidebar-right {
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.widget-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.02);
}
.widget-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.widget-header h4 {
  font-size: 15px;
  color: var(--color-text-primary);
}
.add-icon {
  font-size: 16px;
  color: var(--color-text-muted);
  cursor: pointer;
}
.add-icon:hover { color: var(--color-accent); }

.file-count {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 16px;
}

.add-upload {
  display: inline-block;
}

.empty-tip {
  font-size: 13px;
  color: var(--color-text-muted);
  text-align: center;
  padding: 10px 0;
  background-color: var(--color-bg-light);
  border-radius: 6px;
  margin-bottom: 16px;
  border: 1px dashed var(--color-border);
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.attachment-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background-color: var(--color-bg-light);
  border-radius: 6px;
  transition: background-color 0.2s;
}

.attachment-item:hover {
  background-color: var(--color-hover);
}

.file-icon {
  color: var(--color-text-secondary);
  margin-right: 8px;
  font-size: 16px;
}

.file-name {
  flex: 1;
  font-size: 13px;
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-right: 8px;
}

.remove-icon {
  color: var(--color-text-muted);
  cursor: pointer;
  font-size: 14px;
}

.remove-icon:hover {
  color: var(--color-danger);
}

.boss-ad-banner {
  background: linear-gradient(180deg, #e8fbfa, #ffffff);
  border: 1px solid #c9ecf0;
  border-radius: 8px;
  padding: 16px;
  margin-top: 20px;
}
.ad-text {
  font-size: 15px;
  font-weight: bold;
  color: #1f3735;
  margin-bottom: 12px;
}
.ad-features {
  list-style: none;
  font-size: 13px;
  color: #3b5a58;
}
.ad-features li {
  margin-bottom: 8px;
  position: relative;
  padding-left: 12px;
}
.ad-features li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 6px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background-color: #00a6a7;
}

.settings-link {
  font-size: 13px;
  color: var(--color-accent);
  text-decoration: none;
}
.privacy-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 12px;
}
.privacy-item .value {
  color: var(--color-text-primary);
}
</style>
