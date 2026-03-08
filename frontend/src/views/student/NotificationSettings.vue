<template>
  <div class="settings-page">
    <div class="settings-card">
      <div class="settings-header">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--color-accent)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
        <h2>消息通知</h2>
      </div>
      <p class="settings-subtitle">管理您的通知偏好，选择接收哪些类型的消息</p>

      <!-- Job Notifications -->
      <div class="section">
        <h3 class="section-title">求职通知</h3>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">新职位推荐</span>
            <span class="label-desc">当有与您匹配的新职位发布时通知您</span>
          </div>
          <el-switch v-model="notify.newJobRecommendation" active-color="#00a6a7" />
        </div>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">投递状态更新</span>
            <span class="label-desc">当您的投递被查看、录取或拒绝时通知您</span>
          </div>
          <el-switch v-model="notify.applicationStatus" active-color="#00a6a7" />
        </div>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">面试邀请</span>
            <span class="label-desc">当招聘方向您发出面试邀请时通知您</span>
          </div>
          <el-switch v-model="notify.interviewInvitation" active-color="#00a6a7" />
        </div>
      </div>

      <!-- Chat Notifications -->
      <div class="section">
        <h3 class="section-title">聊天消息</h3>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">新消息提醒</span>
            <span class="label-desc">当收到招聘方的新消息时通知您</span>
          </div>
          <el-switch v-model="notify.newMessage" active-color="#00a6a7" />
        </div>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">消息声音提醒</span>
            <span class="label-desc">收到新消息时播放提示音</span>
          </div>
          <el-switch v-model="notify.messageSound" active-color="#00a6a7" />
        </div>
      </div>

      <!-- System Notifications -->
      <div class="section">
        <h3 class="section-title">系统通知</h3>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">系统公告</span>
            <span class="label-desc">平台政策更新、维护通知等</span>
          </div>
          <el-switch v-model="notify.systemAnnouncement" active-color="#00a6a7" />
        </div>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">活动推广</span>
            <span class="label-desc">平台优惠活动、招聘会等信息</span>
          </div>
          <el-switch v-model="notify.promotion" active-color="#00a6a7" />
        </div>
      </div>

      <div class="save-bar">
        <el-button type="primary" color="#00a6a7" @click="handleSave" :loading="saving">保存设置</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile } from '@/api/student'

const saving = ref(false)

const notify = reactive({
  newJobRecommendation: true,
  applicationStatus: true,
  interviewInvitation: true,
  newMessage: true,
  messageSound: true,
  systemAnnouncement: true,
  promotion: false
})

onMounted(async () => {
  try {
    const res = await getProfile()
    const p = res.data || res
    notify.newJobRecommendation = p.notifyNewJob !== false
    notify.applicationStatus = p.notifyApplicationStatus !== false
    notify.interviewInvitation = p.notifyInterview !== false
    notify.newMessage = p.notifyNewMessage !== false
    notify.messageSound = p.notifyMessageSound !== false
    notify.systemAnnouncement = p.notifySystemAnnouncement !== false
    notify.promotion = p.notifyPromotion === true
  } catch (e) { /* use defaults */ }
})

const handleSave = async () => {
  saving.value = true
  try {
    await updateProfile({
      notifyNewJob: notify.newJobRecommendation,
      notifyApplicationStatus: notify.applicationStatus,
      notifyInterview: notify.interviewInvitation,
      notifyNewMessage: notify.newMessage,
      notifyMessageSound: notify.messageSound,
      notifySystemAnnouncement: notify.systemAnnouncement,
      notifyPromotion: notify.promotion
    })
    ElMessage.success('通知设置已保存')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.settings-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 0;
}
.settings-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.settings-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}
.settings-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}
.settings-subtitle {
  color: var(--color-text-muted);
  font-size: 13px;
  margin: 0 0 28px 0;
  padding-left: 36px;
}
.section {
  margin-bottom: 28px;
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-border);
}
.setting-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;
}
.setting-row:last-child {
  border-bottom: none;
}
.setting-label {
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.label-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
}
.label-desc {
  font-size: 12px;
  color: var(--color-text-muted);
}
.save-bar {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}
</style>
