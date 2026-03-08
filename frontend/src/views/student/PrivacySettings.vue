<template>
  <div class="settings-page">
    <div class="settings-card">
      <div class="settings-header">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--color-accent)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
        <h2>隐私保护</h2>
      </div>
      <p class="settings-subtitle">管理您的隐私偏好，控制信息的可见范围</p>

      <!-- Profile Visibility -->
      <div class="section">
        <h3 class="section-title">简历可见性</h3>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">在线简历公开</span>
            <span class="label-desc">开启后，招聘方可以搜索和查看您的简历</span>
          </div>
          <el-switch v-model="privacy.resumeVisible" active-color="#00a6a7" />
        </div>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">显示真实姓名</span>
            <span class="label-desc">关闭后将以昵称展示给招聘方</span>
          </div>
          <el-switch v-model="privacy.showRealName" active-color="#00a6a7" />
        </div>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">显示联系方式</span>
            <span class="label-desc">关闭后招聘方无法直接看到您的手机号和邮箱</span>
          </div>
          <el-switch v-model="privacy.showContact" active-color="#00a6a7" />
        </div>
      </div>

      <!-- Activity Visibility -->
      <div class="section">
        <h3 class="section-title">活动记录</h3>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">在线状态</span>
            <span class="label-desc">关闭后招聘方将无法看到您的在线状态</span>
          </div>
          <el-switch v-model="privacy.showOnline" active-color="#00a6a7" />
        </div>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">最近活跃时间</span>
            <span class="label-desc">关闭后不展示最后活跃时间</span>
          </div>
          <el-switch v-model="privacy.showLastActive" active-color="#00a6a7" />
        </div>
      </div>

      <!-- Blacklist -->
      <div class="section">
        <h3 class="section-title">屏蔽管理</h3>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">屏蔽企业列表</span>
            <span class="label-desc">已屏蔽的企业无法查看您的简历</span>
          </div>
          <el-button size="small" plain @click="openBlacklistDialog">管理</el-button>
        </div>
      </div>

      <div class="save-bar">
        <el-button type="primary" color="#00a6a7" @click="handleSave" :loading="saving">保存设置</el-button>
      </div>
    </div>

    <!-- Blacklist Management Dialog -->
    <el-dialog v-model="blacklistDialogVisible" title="已屏蔽企业列表" width="500px">
      <el-table :data="blacklist" v-loading="loadingBlacklist" empty-text="当前没有屏蔽的企业">
        <el-table-column prop="companyName" label="企业名称" />
        <el-table-column prop="industry" label="所属行业" />
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button link type="primary" @click="handleUnblock(scope.row)">解除屏蔽</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProfile, updateProfile, getBlacklist, removeBlacklist } from '@/api/student'

const saving = ref(false)
const blacklistDialogVisible = ref(false)
const blacklist = ref([])
const loadingBlacklist = ref(false)

const privacy = reactive({
  resumeVisible: true,
  showRealName: true,
  showContact: false,
  showOnline: true,
  showLastActive: true
})

onMounted(async () => {
  try {
    const res = await getProfile()
    const p = res.data || res
    privacy.resumeVisible = p.privacyResumeOpen !== false
    privacy.showRealName = p.privacyShowName !== false
    privacy.showContact = p.privacyShowContact === true
    privacy.showOnline = p.privacyShowOnline !== false
    privacy.showLastActive = p.privacyShowLastActive !== false
  } catch (e) { /* use defaults */ }
})

const handleSave = async () => {
  saving.value = true
  try {
    await updateProfile({
      privacyResumeOpen: privacy.resumeVisible,
      privacyShowName: privacy.showRealName,
      privacyShowContact: privacy.showContact,
      privacyShowOnline: privacy.showOnline,
      privacyShowLastActive: privacy.showLastActive
    })
    ElMessage.success('隐私设置已保存')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const openBlacklistDialog = async () => {
  blacklistDialogVisible.value = true
  await fetchBlacklist()
}

const fetchBlacklist = async () => {
  loadingBlacklist.value = true
  try {
    const res = await getBlacklist()
    blacklist.value = res.data || res
  } catch (e) {
    ElMessage.error('获取黑名单失败')
  } finally {
    loadingBlacklist.value = false
  }
}

const handleUnblock = (row) => {
  ElMessageBox.confirm(`确定要解除对 ${row.companyName} 的屏蔽吗？`, '解除提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await removeBlacklist(row.enterpriseId)
      ElMessage.success('已解除屏蔽')
      await fetchBlacklist()
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  }).catch(() => {})
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
