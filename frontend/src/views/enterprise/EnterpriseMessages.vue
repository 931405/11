<template>
  <div class="chat-layout">
    <div class="chat-container">
      
      <!-- Left Pane: Contact List -->
      <aside class="chat-sidebar">
        <!-- Sidebar Header: Search -->
        <div class="sidebar-header">
          <div class="search-box">
            <el-input v-model="searchKeyword" placeholder="搜索候选人、职位" size="small" class="contact-search">
              <template #prefix><el-icon><Search/></el-icon></template>
            </el-input>
          </div>
        </div>

        <!-- Filter Tabs -->
        <div class="filter-tabs">
          <span class="f-tab active">全部会话</span>
        </div>

        <!-- Contact List -->
        <div class="contact-list" v-loading="loading">
          <el-empty v-if="filteredConversations.length === 0" description="暂无消息" :image-size="60" />
          
          <div 
            v-for="conv in filteredConversations" 
            :key="conv.applicationId" 
            class="contact-item"
            :class="{ 'active': selectedConv && selectedConv.applicationId === conv.applicationId }"
            @click="selectConversation(conv)"
          >
            <el-badge :value="conv.unreadCount" :hidden="conv.unreadCount === 0" class="c-badge">
              <el-avatar :size="40" class="c-avatar" :src="conv.studentAvatar || ''">{{ conv.studentName?.charAt(0) || '学' }}</el-avatar>
            </el-badge>
            <div class="c-info">
              <div class="c-row1">
                <span class="c-name">{{ conv.studentName }}</span>
                <span class="c-time">{{ formatTime(conv.latestMessageTime) }}</span>
              </div>
              <div class="c-row2">
                <span class="c-job">投递: {{ conv.jobTitle }}</span>
              </div>
              <div class="c-row3">
                <span class="c-msg" v-if="conv.latestMsgType === 'IMAGE'">[图片]</span>
                <span class="c-msg" v-else-if="conv.latestMsgType === 'FILE'">[文件]</span>
                <span class="c-msg" v-else>{{ conv.latestMessage || '点击开始沟通' }}</span>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <!-- Right Pane: Chat Window -->
      <main class="chat-main">
        <template v-if="selectedConv">
          <!-- Chat Header -->
          <div class="chat-header">
            <div class="ch-left">
              <span class="ch-name">{{ selectedConv.studentName }}</span>
              <span class="ch-status">沟通中</span>
            </div>
            <div class="ch-right">
              <el-button size="small" @click="$router.push(`/enterprise/jobs/${selectedConv.jobId}/candidates`)">查看全部候选人</el-button>
            </div>
          </div>

          <!-- Chat Messages Area -->
          <div class="chat-body" ref="chatBody">
            <div v-for="msg in currentMessages" :key="msg.id" 
                 class="message-wrapper"
                 :class="msg.senderRole === userStore.userInfo.role ? 'msg-self' : 'msg-other'">
              
              <el-avatar v-if="msg.senderRole === userStore.userInfo.role" 
                        :size="36" class="msg-avatar" :src="userStore.userInfo.avatar || ''">
                {{ userStore.userInfo.realName?.charAt(0) || '我' }}
              </el-avatar>
              
              <el-avatar v-if="msg.senderRole !== userStore.userInfo.role" 
                        :size="36" class="msg-avatar" :src="selectedConv.studentAvatar || ''">
                {{ selectedConv.studentName?.charAt(0) || '学' }}
              </el-avatar>

              <div class="msg-content">
                <div class="msg-bubble" :class="{ 'image-msg': msg.msgType === 'IMAGE' }">
                  <template v-if="msg.msgType === 'IMAGE'">
                    <img :src="msg.content" class="chat-image" @click="previewImage(msg.content)" alt="发送的图片" />
                  </template>
                  <template v-else-if="msg.msgType === 'FILE'">
                    <div class="file-card">
                      <el-icon class="file-icon"><Document /></el-icon>
                      <div class="file-info">
                        <div class="file-name">{{ parseFileName(msg.content) }}</div>
                        <div class="file-size">{{ parseFileSize(msg.content) }}</div>
                      </div>
                      <el-button link type="primary" @click="downloadFile(msg.content)">下载</el-button>
                    </div>
                  </template>
                  <template v-else>
                    {{ msg.content }}
                  </template>
                </div>
                <div class="msg-time">{{ formatTime(msg.createdAt) }}</div>
              </div>
            </div>
          </div>

          <!-- Chat Footer: Input Area -->
          <div class="chat-footer">
            <div class="cf-toolbar">
              <el-tooltip content="发送图片" placement="top">
                <el-icon class="tb-icon" @click="triggerImageUpload"><Picture /></el-icon>
              </el-tooltip>
              <el-tooltip content="发送附件" placement="top">
                <el-icon class="tb-icon" @click="triggerFileUpload"><Paperclip /></el-icon>
              </el-tooltip>
              
              <input type="file" ref="imageInput" accept="image/*" style="display: none" @change="handleImageSelected" />
              <input type="file" ref="fileInput" style="display: none" @change="handleFileSelected" />
            </div>
            <div class="cf-input">
              <el-input 
                v-model="replyText" 
                type="textarea" 
                :rows="3" 
                placeholder="回复候选人..." 
                resize="none"
                @keyup.enter.exact.prevent="sendReply"
              />
            </div>
            <div class="cf-actions">
              <span class="cf-tip">按 Enter 发送，Shift+Enter 换行</span>
              <el-button type="primary" color="#00a6a7" :disabled="!replyText.trim()" @click="sendReply">发 送</el-button>
            </div>
          </div>
        </template>
        
        <template v-else>
          <!-- Empty State -->
          <div class="chat-empty">
            <el-icon class="empty-icon"><ChatDotRound /></el-icon>
            <p>点击左侧候选人开始沟通</p>
          </div>
        </template>
      </main>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { getConversations, getMessages, sendMessage, markAsRead } from '@/api/chat'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const appStore = useAppStore()
const loading = ref(false)
const searchKeyword = ref('')
const replyText = ref('')
const chatBody = ref(null)

const conversations = ref([])
const selectedConv = ref(null)
const currentMessages = ref([])

const imageInput = ref(null)
const fileInput = ref(null)

// --- Polling for real-time chat ---
let chatPollTimer = null

onMounted(() => {
  fetchConversations()
})

const fetchConversations = async () => {
  loading.value = true
  try {
    const res = await getConversations()
    conversations.value = res || []
    
    // Auto-select first if none selected
    if (!selectedConv.value && conversations.value.length > 0) {
      selectConversation(conversations.value[0])
    }
  } catch (error) {
    console.error('Failed to fetch conversations:', error)
    ElMessage.error('无法加载消息列表')
  } finally {
    loading.value = false
  }
}

const filteredConversations = computed(() => {
  if (!searchKeyword.value) return conversations.value
  const kw = searchKeyword.value.toLowerCase()
  return conversations.value.filter(c => 
    (c.studentName && c.studentName.toLowerCase().includes(kw)) ||
    (c.jobTitle && c.jobTitle.toLowerCase().includes(kw))
  )
})

const selectConversation = async (conv) => {
  selectedConv.value = conv
  if (chatPollTimer) clearInterval(chatPollTimer)
  
  await fetchMessages(conv.applicationId)
  
  // Mark as read locally and remotely
  if (conv.unreadCount > 0) {
    conv.unreadCount = 0
    markAsRead(conv.applicationId).then(() => {
      appStore.fetchUnreadCount()
    }).catch(err => console.error(err))
  }
  
  // Start polling for this conversation
  chatPollTimer = setInterval(() => {
    fetchMessages(conv.applicationId, true)
  }, 5000)
}

const fetchMessages = async (applicationId, isSilent = false) => {
  try {
    const res = await getMessages(applicationId)
    const prevLen = currentMessages.value.length
    currentMessages.value = res || []
    
    if (currentMessages.value.length > prevLen && !isSilent) {
      scrollToBottom()
    } else if (currentMessages.value.length > prevLen && isSilent) {
        // If we received new messages while polling, scroll to bottom
        scrollToBottom()
        // mark as read
        markAsRead(applicationId).then(() => {
          appStore.fetchUnreadCount()
        }).catch(err => console.error(err))
    }
  } catch (error) {
    console.error('Failed to fetch messages:', error)
  }
}

const sendReply = async () => {
  if (!replyText.value.trim() || !selectedConv.value) return
  
  const text = replyText.value
  replyText.value = '' // Clear immediately for better UX
  
  try {
    await sendMessage(
      selectedConv.value.applicationId, 
      text,
      'TEXT'
    )
    
    // Refresh messages and conversations
    await fetchMessages(selectedConv.value.applicationId)
    scrollToBottom()
    
    // Refresh conversation list quietly to update latest message string
    const res = await getConversations()
    conversations.value = res || []
  } catch (error) {
    console.error('Failed to send msg:', error)
    ElMessage.error('发送失败')
    replyText.value = text // Restore on fail
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatBody.value) {
      chatBody.value.scrollTop = chatBody.value.scrollHeight
    }
  })
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  
  // Format based on relative time
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime()
  const msgDate = new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime()
  
  if (msgDate === today) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else {
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
  }
}

// Media Sending Handlers
const triggerImageUpload = () => {
  imageInput.value?.click()
}

const triggerFileUpload = () => {
  fileInput.value?.click()
}

const handleImageSelected = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  event.target.value = '' // reset
  
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 2MB')
    return
  }
  
  const reader = new FileReader()
  reader.onload = async (e) => {
    try {
      await sendMessage(
        selectedConv.value.applicationId,
        e.target.result,
        'IMAGE'
      )
      await fetchMessages(selectedConv.value.applicationId)
      scrollToBottom()
    } catch (err) {
      ElMessage.error('图片发送失败')
    }
  }
  reader.readAsDataURL(file)
}

const handleFileSelected = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  event.target.value = '' // reset
  
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过 5MB')
    return
  }
  
  const reader = new FileReader()
  reader.onload = async (e) => {
    const payload = JSON.stringify({
      name: file.name,
      size: file.size,
      data: e.target.result
    })
    try {
      await sendMessage(
        selectedConv.value.applicationId,
        payload,
        'FILE'
      )
      await fetchMessages(selectedConv.value.applicationId)
      scrollToBottom()
    } catch (err) {
      ElMessage.error('文件发送失败')
    }
  }
  reader.readAsDataURL(file)
}

const parseFileName = (content) => {
  try { return JSON.parse(content).name } catch (e) { return '未知文件' }
}

const parseFileSize = (content) => {
  try {
    const size = JSON.parse(content).size
    if (size < 1024) return size + ' B'
    if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
    return (size / (1024 * 1024)).toFixed(1) + ' MB'
  } catch (e) { return '0 B' }
}

const downloadFile = (content) => {
  try {
    const fileData = JSON.parse(content)
    const link = document.createElement('a')
    link.href = fileData.data
    link.download = fileData.name
    link.click()
  } catch (e) {
    ElMessage.error('下载失败')
  }
}

const previewImage = (content) => {
    const w = window.open('about:blank')
    const image = new Image()
    image.src = content
    setTimeout(() => {
        w.document.write(image.outerHTML)
    }, 0)
}
</script>

<style scoped>
.chat-layout {
  height: calc(100vh - 100px);
  padding: 20px;
  background-color: var(--color-bg-secondary);
}

.chat-container {
  display: flex;
  height: 100%;
  background-color: var(--color-bg-card);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  overflow: hidden;
  max-width: 1200px;
  margin: 0 auto;
}

/* Sidebar */
.chat-sidebar {
  width: 320px;
  border-right: 1px solid var(--color-border-light);
  display: flex;
  flex-direction: column;
  background-color: #fcfcfc;
}

.sidebar-header {
  padding: 16px;
}

.contact-search :deep(.el-input__wrapper) {
  border-radius: 20px;
  background-color: var(--color-bg-secondary);
}

.filter-tabs {
  display: flex;
  padding: 0 16px 12px;
  border-bottom: 1px solid var(--color-border-light);
}

.f-tab {
  font-size: 14px;
  color: var(--color-text-regular);
  margin-right: 20px;
  cursor: pointer;
  position: relative;
}

.f-tab.active {
  color: var(--color-accent);
  font-weight: 500;
}

.f-tab.active::after {
  content: '';
  position: absolute;
  bottom: -13px;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: var(--color-accent);
}

.contact-list {
  flex: 1;
  overflow-y: auto;
}

.contact-item {
  display: flex;
  padding: 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f0f0f0;
}

.contact-item:hover {
  background-color: var(--color-bg-primary);
}

.contact-item.active {
  background-color: #e6f6f6;
  border-left: 3px solid var(--color-accent);
}

.c-avatar {
  background-color: var(--color-accent);
  margin-right: 12px;
  flex-shrink: 0;
}

.c-info {
  flex: 1;
  min-width: 0; 
}

.c-row1 {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.c-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.c-time {
  font-size: 12px;
  color: var(--color-text-placeholder);
}

.c-row2 {
  font-size: 13px;
  color: var(--color-text-regular);
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.c-row3 {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.c-msg {
  font-size: 13px;
  color: var(--color-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

/* Chat Main */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg-card);
}

.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--color-text-placeholder);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: #e0e0e0;
}

.chat-header {
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid var(--color-border-light);
}

.ch-name {
  font-size: 18px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin-right: 12px;
}

.ch-status {
  font-size: 12px;
  color: var(--color-success);
  background-color: rgba(103, 194, 58, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
}

.chat-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background-color: rgba(247, 248, 250, 0.5);
}

.message-wrapper {
  display: flex;
  margin-bottom: 24px;
}

.msg-self {
  flex-direction: row-reverse;
}

.msg-avatar {
  background-color: #c0c4cc;
  flex-shrink: 0;
}

.msg-self .msg-avatar {
  background-color: var(--color-accent);
  margin-left: 12px;
}

.msg-other .msg-avatar {
  margin-right: 12px;
}

.msg-content {
  max-width: 60%;
  display: flex;
  flex-direction: column;
}

.msg-self .msg-content {
  align-items: flex-end;
}

.msg-other .msg-content {
  align-items: flex-start;
}

.msg-bubble {
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.msg-self .msg-bubble {
  background-color: var(--color-accent);
  color: #fff;
  border-top-right-radius: 2px;
}

.msg-other .msg-bubble {
  background-color: #fff;
  border: 1px solid var(--color-border-light);
  color: var(--color-text-primary);
  border-top-left-radius: 2px;
}

.msg-bubble.image-msg {
  padding: 0;
  background: transparent;
  border: none;
  box-shadow: none;
}

.chat-image {
  max-width: 250px;
  max-height: 250px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid var(--color-border-light);
}

.file-card {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  min-width: 200px;
}

.msg-self .file-card {
  background: rgba(255,255,255,0.1);
  border-color: rgba(255,255,255,0.2);
}

.file-icon {
  font-size: 32px;
  color: #909399;
  margin-right: 12px;
}

.msg-self .file-icon {
  color: #fff;
}

.file-info {
  flex: 1;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.file-size {
  font-size: 12px;
  color: #909399;
}

.msg-self .file-size {
  color: rgba(255,255,255,0.7);
}


.msg-time {
  font-size: 12px;
  color: var(--color-text-placeholder);
  margin-top: 6px;
}

/* Chat Footer */
.chat-footer {
  height: 180px;
  border-top: 1px solid var(--color-border-light);
  display: flex;
  flex-direction: column;
  background-color: #fff;
}

.cf-toolbar {
  padding: 10px 20px;
  display: flex;
  gap: 16px;
}

.tb-icon {
  font-size: 20px;
  color: var(--color-text-regular);
  cursor: pointer;
  transition: color 0.2s;
}

.tb-icon:hover {
  color: var(--color-accent);
}

.cf-input {
  flex: 1;
  padding: 0 20px;
}

.cf-input :deep(.el-textarea__inner) {
  border: none;
  box-shadow: none;
  padding: 0;
  font-size: 14px;
}

.cf-input :deep(.el-textarea__inner:focus) {
  box-shadow: none;
}

.cf-actions {
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cf-tip {
  font-size: 12px;
  color: var(--color-text-placeholder);
}

:deep(.c-badge) {
  margin-right: 12px;
}

:deep(.c-badge .el-badge__content) {
  transform: translateY(-50%) translateX(50%);
}
</style>
