<template>
  <div class="chat-layout">
    <div class="chat-container">
      
      <!-- Left Pane: Contact List -->
      <aside class="chat-sidebar">
        <!-- Sidebar Header: Search -->
        <div class="sidebar-header">
          <div class="search-box">
            <el-input v-model="searchKeyword" placeholder="搜索联系人、公司" size="small" class="contact-search">
              <template #prefix><el-icon><Search/></el-icon></template>
            </el-input>
          </div>
        </div>

        <!-- Filter Tabs -->
        <div class="filter-tabs">
          <span v-for="ft in filterTabs" :key="ft" class="f-tab" :class="{ active: activeFilter === ft }" @click="activeFilter = ft">{{ ft }}</span>
        </div>

        <!-- Contact List (Mocked using real application data) -->
        <div class="contact-list" v-loading="loading">
          <el-empty v-if="applications.length === 0" description="暂无联系人" :image-size="60" />
          
          <div 
            v-for="(app, index) in filteredApplications" 
            :key="app.id" 
            class="contact-item"
            :class="{ 'active': selectedApp && selectedApp.id === app.id }"
            @click="selectApplication(app)"
          >
            <el-avatar :size="40" class="c-avatar">{{ app.companyName?.charAt(0) || '企' }}</el-avatar>
            <div class="c-info">
              <div class="c-row1">
                <span class="c-name">HR · {{ app.companyName }}</span>
                <span class="c-time">{{ formatTime(app.createdAt) }}</span>
              </div>
              <div class="c-row2">
                <span class="c-job">{{ app.jobTitle || '投递职位: ' + app.jobId }}</span>
              </div>
              <div class="c-row3">
                <span class="c-msg">{{ app.enterpriseRemark || '投递成功，等待企业查阅' }}</span>
                <div v-show="app.unreadCount > 0" class="unread-dot"></div>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- Right Pane: Chat Window -->
      <main class="chat-main">
        <template v-if="selectedApp">
          <!-- Chat Header -->
          <div class="chat-header">
            <div class="ch-left">
              <span class="ch-name">HR · {{ selectedApp.companyName }}</span>
              <span class="ch-status">在线</span>
            </div>
            <div class="ch-right">
              <el-button size="small" @click="$router.push(`/student/jobs/${selectedApp.jobId}`)">查看职位</el-button>
            </div>
          </div>

          <!-- Chat Body (Messages + Timeline) -->
          <div class="chat-body" ref="chatBodyRef">
            <div class="sys-msg">您已成功投递该职位，请耐心等待HR回复</div>
            
            <!-- Job Info Card -->
            <div class="job-info-card" v-if="selectedApp.jobTitle">
              <div class="jic-title">{{ selectedApp.jobTitle }}</div>
              <div class="jic-salary" v-if="selectedApp.salaryMin">{{ selectedApp.salaryMin }}-{{ selectedApp.salaryMax }} 元/天</div>
              <div class="jic-meta">{{ selectedApp.workLocation }}</div>
            </div>
            
            <!-- Real Chat Messages -->
            <div v-if="chatMessages.length > 0" class="messages-container">
              <div 
                v-for="msg in chatMessages" 
                :key="msg.id" 
                class="msg-item"
                :class="{ 'my-msg': msg.senderRole === 'STUDENT', 'hr-msg': msg.senderRole !== 'STUDENT' }"
              >
                <!-- Avatar is always first in DOM, row-reverse will push it to the right for .my-msg -->
                <el-avatar v-if="msg.senderRole === 'STUDENT'" :size="36" class="msg-avatar">
                  我
                </el-avatar>
                <el-avatar v-if="msg.senderRole !== 'STUDENT'" :size="36" class="msg-avatar">
                  {{ selectedApp.companyName?.charAt(0) || '企' }}
                </el-avatar>

                <div class="msg-bubble">
                  <div v-if="msg.msgType === 'RESUME'" class="resume-card">
                    <el-icon><Document/></el-icon>
                    <span>我的简历</span>
                  </div>
                  <div v-else-if="msg.msgType === 'IMAGE'" class="image-msg">
                    <img :src="msg.content" alt="图片" class="chat-image" @click="previewImage(msg.content)" />
                  </div>
                  <div v-else-if="msg.msgType === 'FILE'" class="file-card">
                    <el-icon :size="20"><Folder/></el-icon>
                    <div class="file-info">
                      <span class="file-name">{{ parseFileName(msg.content) }}</span>
                      <span class="file-size">{{ parseFileSize(msg.content) }}</span>
                    </div>
                  </div>
                  <template v-else>{{ msg.content }}</template>
                </div>
              </div>
            </div>

            <!-- Application Timeline (Horizontal) -->
            <div class="timeline-section">
              <h4 class="timeline-header">求职进度</h4>
              <el-steps :active="selectedApp.timeline?.length || 1" align-center finish-status="success">
                <el-step 
                  v-for="(event, idx) in selectedApp.timeline" 
                  :key="idx" 
                  :title="event.label" 
                  :description="formatTime(event.time)"
                ></el-step>
              </el-steps>
            </div>
          </div>

          <!-- Chat Input Area -->
          <div class="chat-footer">
            <div class="chat-toolbar">
              <span class="toolbar-icon" @click="triggerImageUpload" title="发送图片">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>
              </span>
              <span class="toolbar-icon" @click="triggerFileUpload" title="发送文件">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
              </span>
              <input 
                type="file" 
                ref="imageInputRef" 
                accept="image/*" 
                style="display:none" 
                @change="handleImageSelected"
              />
              <input 
                type="file" 
                ref="fileInputRef" 
                style="display:none" 
                @change="handleFileSelected"
              />
              <div class="t-spacer"></div>
              <span class="t-resume" @click="openResumeDialog" style="cursor:pointer">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
                发送简历
              </span>
            </div>
            <div class="chat-input-wrapper">
              <textarea 
                v-model="chatInput" 
                placeholder="输入消息..." 
                class="chat-textarea" 
                @keydown.enter.exact.prevent="handleSendMessage"
              ></textarea>
            </div>
            <div class="chat-send">
              <span class="send-hint">按下 Enter 发送</span>
              <el-button 
                type="primary" 
                color="#00a6a7" 
                :disabled="!chatInput.trim()" 
                @click="handleSendMessage"
              >发送</el-button>
            </div>
          </div>
        </template>
        
        <template v-else>
          <!-- Empty State -->
          <div class="chat-empty">
            <svg width="120" height="120" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M8 10H16M8 14H12M21 11.5C21 16.1944 16.9706 20 12 20C10.7431 20 9.54636 19.7432 8.4687 19.2783C8.1691 19.1491 7.82479 19.1416 7.51978 19.2587L4.31802 20.4868C3.76569 20.6987 3.25056 20.1581 3.49392 19.6105L4.85871 16.5387C5.02102 16.1735 5.01183 15.7538 4.83446 15.3957C3.6669 13.0645 3 10.3392 3 7.5C3 3.35786 7.02944 0 12 0C16.9706 0 21 3.35786 21 7.5Z" fill="#e8f4f4"/>
              <path d="M12 21C17.5228 21 22 16.5228 22 11C22 5.47715 17.5228 1 12 1C6.47715 1 2 5.47715 2 11C2 12.7876 2.46959 14.466 3.28435 15.9392C3.41808 16.181 3.43004 16.4717 3.31551 16.7238L2.09638 19.4072C1.78788 20.0863 2.4549 20.7845 3.15582 20.5186L6.09689 19.4031C6.38883 19.2924 6.71181 19.3032 6.99468 19.432C8.50854 20.1217 10.2039 20.5 12 20.5" stroke="#00a6a7" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <p>选择一个联系人开始沟通</p>
          </div>
        </template>
      </main>

    </div>
    
    <!-- Select Resume Dialog -->
    <el-dialog
      v-model="resumeDialogVisible"
      title="选择要发送的简历"
      width="400px"
      :close-on-click-modal="false"
    >
      <div v-if="resumeList.length === 0" class="no-resumes-msg">
        您尚未上传任何 PDF 简历，请先去「个人中心」-「编辑简历」中上传。
      </div>
      <div v-else class="resume-selection-list">
        <div 
          v-for="(item, idx) in resumeList" 
          :key="idx" 
          class="resume-select-item"
        >
          <div class="resume-info">
            <svg class="pdf-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#F56C6C" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
            <span class="resume-name" :title="item.name">{{ item.name }}</span>
            <span class="resume-size">{{ item.size }}</span>
          </div>
          <el-button 
            type="primary" 
            size="small" 
            :loading="sendingResume"
            @click="confirmSendResume(item)"
          >发送</el-button>
        </div>
      </div>
    </el-dialog>
    
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getApplications, getProfile } from '@/api/student'
import { getMessages, sendMessage, markAsRead } from '@/api/chat'
import { useAppStore } from '@/stores/app'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const appStore = useAppStore()
const loading = ref(false)
const applications = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(50)

const searchKeyword = ref('')
const selectedApp = ref(null)
const filterTabs = ['全部', '新招呼', '未读']
const activeFilter = ref('全部')

// Chat state
const chatMessages = ref([])
const chatInput = ref('')
const chatBodyRef = ref(null)
const chatLoading = ref(false)
const imageInputRef = ref(null)
const fileInputRef = ref(null)
let pollTimer = null

// Resume selection state
const resumeDialogVisible = ref(false)
const resumeList = ref([])
const sendingResume = ref(false)

const filteredApplications = computed(() => {
  let result = applications.value

  // Apply text search filter
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(app => {
      const companyMatch = app.companyName?.toLowerCase().includes(keyword)
      const jobMatch = app.jobTitle?.toLowerCase().includes(keyword)
      return companyMatch || jobMatch
    })
  }

  // Apply tab filter
  if (activeFilter.value === '新招呼') {
    // "New Greeting" - we'll define this as applications that have NO messages or only 1 message from system/student 
    // Since we don't have message counts per application globally here without fetching, 
    // a simplified approach is filtering by APPLIED status or having 1 unread message.
    // Given the data model, let's assume '新招呼' means just recently APPLIED jobs with no HR interaction.
    result = result.filter(app => app.status === 'APPLIED')
  } else if (activeFilter.value === '未读') {
    // "Unread" - applications with unreadCount > 0
    result = result.filter(app => app.unreadCount > 0)
  }

  return result
})

const fetchApplications = async () => {
  loading.value = true
  try {
    const res = await getApplications({ page: page.value, size: size.value })
    applications.value = res.content || []
    total.value = res.totalElements || 0
    if (applications.value.length > 0 && !selectedApp.value) {
      selectApplication(applications.value[0])
    }
  } catch (error) {
    console.error('Failed to load applications', error)
  } finally {
    loading.value = false
  }
}

const selectApplication = async (app) => {
  selectedApp.value = app
  
  if (app.unreadCount > 0) {
    app.unreadCount = 0
  }
  
  await fetchChatMessages()
}

const fetchChatMessages = async () => {
  if (!selectedApp.value) return
  try {
    chatMessages.value = await getMessages(selectedApp.value.id)
    await markAsRead(selectedApp.value.id)
    appStore.fetchUnreadCount()
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('Failed to load chat messages', error)
  }
}

const handleSendMessage = async () => {
  const content = chatInput.value.trim()
  if (!content || !selectedApp.value) return

  try {
    const newMsg = await sendMessage(selectedApp.value.id, content)
    chatMessages.value.push(newMsg)
    chatInput.value = ''
    await nextTick()
    scrollToBottom()
  } catch (error) {
    ElMessage.error('发送失败，请重试')
  }
}

const openResumeDialog = async () => {
  if (!selectedApp.value) return
  
  try {
    const profile = await getProfile()
    if (profile && profile.resumeAttachments) {
      resumeList.value = JSON.parse(profile.resumeAttachments)
    } else {
      resumeList.value = []
    }
  } catch (e) {
    console.error('Failed to parse resume attachments', e)
    resumeList.value = []
  }
  
  if (resumeList.value.length === 0) {
    ElMessage.warning('您尚未上传PDF简历，请先去个人中心上传')
  } else if (resumeList.value.length === 1) {
    // If only 1 resume, just send it directly
    confirmSendResume(resumeList.value[0])
  } else {
    // If multiple resumes, show dialog for selection
    resumeDialogVisible.value = true
  }
}

const confirmSendResume = async (resumeItem) => {
  if (!selectedApp.value) return
  sendingResume.value = true
  try {
    const content = JSON.stringify({ name: resumeItem.name, size: resumeItem.size, url: resumeItem.url })
    const newMsg = await sendMessage(selectedApp.value.id, content, 'FILE')
    chatMessages.value.push(newMsg)
    ElMessage.success('简历已成功发送')
    resumeDialogVisible.value = false
    await nextTick()
    scrollToBottom()
  } catch (error) {
    ElMessage.error('简历发送失败')
  } finally {
    sendingResume.value = false
  }
}

const triggerImageUpload = () => {
  imageInputRef.value?.click()
}

const triggerFileUpload = () => {
  fileInputRef.value?.click()
}

const handleImageSelected = async (e) => {
  const file = e.target.files?.[0]
  if (!file || !selectedApp.value) return
  
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return
  }

  const reader = new FileReader()
  reader.onload = async () => {
    try {
      const newMsg = await sendMessage(selectedApp.value.id, reader.result, 'IMAGE')
      chatMessages.value.push(newMsg)
      await nextTick()
      scrollToBottom()
    } catch (error) {
      ElMessage.error('图片发送失败')
    }
  }
  reader.readAsDataURL(file)
  e.target.value = ''
}

const handleFileSelected = async (e) => {
  const file = e.target.files?.[0]
  if (!file || !selectedApp.value) return

  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过 10MB')
    return
  }

  try {
    const fileName = file.name
    const fileSize = (file.size / 1024).toFixed(1) + ' KB'
    const content = JSON.stringify({ name: fileName, size: fileSize })
    const newMsg = await sendMessage(selectedApp.value.id, content, 'FILE')
    chatMessages.value.push(newMsg)
    ElMessage.success('文件已发送')
    await nextTick()
    scrollToBottom()
  } catch (error) {
    ElMessage.error('文件发送失败')
  }
  e.target.value = ''
}

const parseFileName = (content) => {
  try { return JSON.parse(content).name || '文件' } catch { return '文件' }
}

const parseFileSize = (content) => {
  try { return JSON.parse(content).size || '' } catch { return '' }
}

const previewImage = (src) => {
  window.open(src, '_blank')
}

const scrollToBottom = () => {
  if (chatBodyRef.value) {
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
  }
}

const formatTime = (dateString) => {
  if (!dateString) return ''
  const date = dayjs(dateString)
  const today = dayjs()
  if (date.isSame(today, 'day')) {
    return date.format('HH:mm')
  } else if (date.isSame(today.subtract(1, 'day'), 'day')) {
    return '昨天'
  } else {
    return date.format('MM-DD')
  }
}

// Poll for new messages every 5 seconds
const startPolling = () => {
  pollTimer = setInterval(() => {
    if (selectedApp.value) {
      fetchChatMessages()
    }
  }, 5000)
}

onMounted(() => {
  fetchApplications()
  startPolling()
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.chat-layout {
  height: calc(100vh - 80px); /* Height minus header and padding */
  padding: 20px 0;
  display: flex;
  justify-content: center;
}

.chat-container {
  width: 100%;
  max-width: 1200px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
  display: flex;
  overflow: hidden;
  height: 100%;
}

/* --- Left Pane: Sidebar --- */
.chat-sidebar {
  width: 320px;
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  background-color: #fafafa;
}

.sidebar-header {
  padding: 16px 20px;
  background-color: #fff;
}
:deep(.contact-search .el-input__wrapper) {
  border-radius: 20px;
  background-color: var(--color-bg-primary);
  box-shadow: none !important;
}

.filter-tabs {
  display: flex;
  gap: 20px;
  padding: 10px 20px;
  background-color: #fff;
  border-bottom: 1px solid var(--color-border);
}
.f-tab {
  font-size: 13px;
  color: var(--color-text-secondary);
  cursor: pointer;
  position: relative;
  padding-bottom: 6px;
}
.f-tab.active {
  color: var(--color-accent);
  font-weight: 500;
}
.f-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 16px;
  height: 2px;
  background-color: var(--color-accent);
  border-radius: 2px;
}

.contact-list {
  flex: 1;
  overflow-y: auto;
}
.contact-item {
  display: flex;
  padding: 16px 20px;
  gap: 12px;
  cursor: pointer;
  border-bottom: 1px solid var(--color-border);
  transition: background-color 0.2s;
}
.contact-item:hover {
  background-color: #f0f0f0;
}
.contact-item.active {
  background-color: #e5f5f5;
}

.c-avatar {
  background-color: var(--color-bg-secondary);
  color: var(--color-accent);
  flex-shrink: 0;
}
.c-info {
  flex: 1;
  min-width: 0; /* for text overflow */
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.c-row1, .c-row2, .c-row3 {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.c-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.c-time {
  font-size: 12px;
  color: var(--color-text-muted);
}
.c-job {
  font-size: 12px;
  color: var(--color-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.c-msg {
  font-size: 12px;
  color: var(--color-text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}
.unread-dot {
  width: 8px;
  height: 8px;
  background-color: var(--color-danger);
  border-radius: 50%;
  margin-left: 8px;
  flex-shrink: 0;
}

/* --- Right Pane: Chat Window --- */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
}

.chat-header {
  height: 60px;
  padding: 0 24px;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.ch-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.ch-name {
  font-size: 16px;
  font-weight: bold;
  color: var(--color-text-primary);
}
.ch-status {
  font-size: 12px;
  color: var(--color-text-muted);
  display: flex;
  align-items: center;
  gap: 4px;
}
.ch-status::before {
  content: '';
  display: block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--color-success); /* simulated online */
}

/* --- Chat Body --- */
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background-color: #f7f9fa;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.sys-msg {
  text-align: center;
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 8px;
}
.messages-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 4px;
}
.msg-item {
  display: flex;
  gap: 12px;
  max-width: 80%;
}
.my-msg {
  align-self: flex-end;
  flex-direction: row-reverse;
}
.hr-msg {
  align-self: flex-start;
}
.msg-avatar {
  flex-shrink: 0;
  background-color: var(--color-accent);
  color: #fff;
  font-size: 14px;
}
.my-msg .msg-avatar {
  margin-left: 12px;
}
.hr-msg .msg-avatar {
  background-color: var(--color-warning);
  margin-right: 12px;
}
.msg-bubble {
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
}
.my-msg .msg-bubble {
  background-color: var(--color-accent);
  color: #fff;
  border-top-right-radius: 0;
}
.hr-msg .msg-bubble {
  background-color: #fff;
  color: var(--color-text-primary);
  border-top-left-radius: 0;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.resume-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255,255,255,0.15);
  border-radius: 4px;
  font-weight: 500;
}
.image-msg {
  max-width: 240px;
}
.chat-image {
  max-width: 100%;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}
.chat-image:hover {
  transform: scale(1.02);
}

/* --- Resume Dialog --- */
.no-resumes-msg {
  text-align: center;
  color: var(--el-text-color-secondary);
  padding: 20px 0;
}
.resume-selection-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.resume-select-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  transition: all 0.3s ease;
}
.resume-select-item:hover {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
}
.resume-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}
.pdf-icon {
  color: #F56C6C;
  font-size: 20px;
}
.resume-name {
  color: var(--el-text-color-primary);
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}
.resume-size {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

/* --- Timeline --- */
.timeline-section {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: rgba(255,255,255,0.15);
}
.file-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: rgba(255,255,255,0.15);
  border-radius: 6px;
  min-width: 180px;
}
.file-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.file-name {
  font-size: 13px;
  font-weight: 500;
  word-break: break-all;
}
.file-size {
  font-size: 11px;
  opacity: 0.7;
}
.msg-content {
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
}
.student-msg .msg-content {
  background-color: var(--color-accent);
  color: #fff;
  border-top-right-radius: 0;
}
.hr-msg .msg-content {
  background-color: #fff;
  color: var(--color-text-primary);
  border-top-left-radius: 0;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

/* --- Chat Footer --- */
.chat-footer {
  height: 180px;
  border-top: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  background-color: #fff;
}
.chat-toolbar {
  display: flex;
  padding: 12px 24px;
  gap: 20px;
  color: var(--color-text-secondary);
  font-size: 20px;
}
.chat-toolbar .toolbar-icon {
  cursor: pointer;
  display: flex;
  align-items: center;
}
.chat-toolbar .toolbar-icon:hover {
  color: var(--color-accent);
}
.t-spacer {
  flex: 1;
}
.t-resume {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: var(--color-text-primary);
}
.t-resume:hover {
  color: var(--color-accent);
}
.chat-input-wrapper {
  flex: 1;
  padding: 0 24px;
}
.chat-textarea {
  width: 100%;
  height: 100%;
  border: none;
  resize: none;
  outline: none;
  font-size: 14px;
  color: var(--color-text-primary);
  font-family: inherit;
  background: transparent;
}
.chat-textarea:disabled {
  color: var(--color-text-muted);
  cursor: not-allowed;
}
.chat-send {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 12px 24px;
  gap: 16px;
}
.send-hint {
  font-size: 12px;
  color: var(--color-text-muted);
}

/* --- Empty state for right pane --- */
.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f7f9fa;
  color: var(--color-text-muted);
  font-size: 14px;
  gap: 16px;
}

/* --- Job Info Card inside chat --- */
.job-info-card {
  background: linear-gradient(135deg, #f0fafa, #e8f4f4);
  border-radius: 8px;
  padding: 12px 16px;
  margin: 8px 24px;
  border-left: 3px solid #00a6a7;
}
.jic-title {
  font-weight: 600;
  font-size: 15px;
  color: var(--color-text-primary);
}
.jic-salary {
  color: #ff6633;
  font-size: 14px;
  font-weight: bold;
  margin-top: 4px;
}
.jic-meta {
  color: var(--color-text-muted);
  font-size: 12px;
  margin-top: 2px;
}

/* --- Timeline Section --- */
.timeline-section {
  padding: 16px 24px;
}
.timeline-header {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 12px;
}
.tl-label {
  font-weight: 600;
  font-size: 14px;
}
.tl-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-top: 2px;
}
</style>
