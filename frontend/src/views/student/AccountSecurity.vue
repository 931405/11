<template>
  <div class="settings-page">
    <div class="settings-card">
      <div class="settings-header">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--color-accent)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
        <h2>账号与安全中心</h2>
      </div>
      <p class="settings-subtitle">管理您的账号信息和登录安全设置</p>

      <!-- Account Info -->
      <div class="section">
        <h3 class="section-title">基本信息</h3>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">用户名</span>
            <span class="label-value">{{ userStore.userInfo.username }}</span>
          </div>
          <el-button size="small" plain @click="openUpdateDialog('username', '用户名')">修改</el-button>
        </div>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">真实姓名</span>
            <span class="label-value">{{ userStore.userInfo.realName || '未设置' }}</span>
          </div>
          <el-button size="small" plain @click="openUpdateDialog('realName', '真实姓名')">修改</el-button>
        </div>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">邮箱</span>
            <span class="label-value">{{ userStore.userInfo.email || '未绑定' }}</span>
          </div>
          <el-button size="small" plain @click="openUpdateDialog('email', '邮箱')">{{ userStore.userInfo.email ? '修改' : '绑定' }}</el-button>
        </div>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">手机号</span>
            <span class="label-value">{{ userStore.userInfo.phone || '未绑定' }}</span>
          </div>
          <el-button size="small" plain @click="openUpdateDialog('phone', '手机号')">{{ userStore.userInfo.phone ? '修改' : '绑定' }}</el-button>
        </div>
      </div>

      <!-- Security -->
      <div class="section">
        <h3 class="section-title">安全设置</h3>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">登录密码</span>
            <span class="label-desc">定期修改密码可以提高账号安全性</span>
          </div>
          <el-button size="small" type="primary" plain @click="showPasswordDialog = true">修改密码</el-button>
        </div>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">账号角色</span>
            <span class="label-value">{{ userStore.userInfo.role === 'STUDENT' ? '求职者' : '招聘方' }}</span>
          </div>
        </div>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">注册时间</span>
            <span class="label-value">{{ userStore.userInfo.createdAt || '——' }}</span>
          </div>
        </div>
      </div>

      <!-- Danger Zone -->
      <div class="section danger-section">
        <h3 class="section-title danger-title">危险操作</h3>
        <div class="setting-row">
          <div class="setting-label">
            <span class="label-text">注销账号</span>
            <span class="label-desc">注销后账号数据将不可恢复</span>
          </div>
          <el-button size="small" type="danger" plain @click="handleConfirmDelete">注销账号</el-button>
        </div>
      </div>
    </div>

    <!-- Change Password Dialog -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="420px">
      <el-form label-width="80px">
        <el-form-item label="当前密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" color="#00a6a7" :loading="changingPassword" @click="handleChangePassword">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- Update Info Dialog -->
    <el-dialog v-model="updateDialog.visible" :title="'修改' + updateDialog.label" width="400px" @closed="onUpdateDialogClosed">
      <el-form label-position="top">
        <el-form-item :label="updateDialog.label">
          <el-input v-model="updateDialog.value" :placeholder="'请输入新的' + updateDialog.label" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="updateDialog.visible = false">取消</el-button>
        <el-button type="primary" color="#00a6a7" :loading="updatingInfo" @click="handleUpdateInfo">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { changePassword, updateUserInfo, deleteAccount } from '@/api/student'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const showPasswordDialog = ref(false)
const changingPassword = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const updateDialog = reactive({
  visible: false,
  field: '',
  label: '',
  value: ''
})
const updatingInfo = ref(false)

const openUpdateDialog = (field, label) => {
  updateDialog.field = field
  updateDialog.label = label
  updateDialog.value = userStore.userInfo[field] || ''
  updateDialog.visible = true
}

const onUpdateDialogClosed = () => {
  updateDialog.value = ''
  updateDialog.field = ''
  updateDialog.label = ''
}

const handleUpdateInfo = async () => {
  if (!updateDialog.value.trim() && updateDialog.field === 'username') {
    ElMessage.warning('用户名不能为空')
    return
  }

  updatingInfo.value = true
  try {
    const data = {}
    data[updateDialog.field] = updateDialog.value.trim()
    await updateUserInfo(data)
    
    // Update local store to reflect changes instantly
    userStore.userInfo[updateDialog.field] = updateDialog.value.trim()
    
    ElMessage.success('修改成功')
    updateDialog.visible = false
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  } finally {
    updatingInfo.value = false
  }
}

const handleChangePassword = async () => {
  if (!passwordForm.oldPassword) {
    ElMessage.warning('请输入当前密码')
    return
  }
  if (!passwordForm.newPassword || passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  changingPassword.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    showPasswordDialog.value = false
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  } finally {
    changingPassword.value = false
  }
}

const handleConfirmDelete = () => {
  ElMessageBox.confirm(
    '注销账号后，您的所有数据将被永久删除且无法恢复。确定要注销吗？',
    '敏感操作确认',
    {
      confirmButtonText: '确定注销',
      cancelButtonText: '取消',
      type: 'error',
      confirmButtonClass: 'el-button--danger'
    }
  ).then(async () => {
    try {
      await deleteAccount()
      ElMessage.success('账号已注销')
      userStore.logout()
      router.push('/login')
    } catch (e) {
      ElMessage.error(e.response?.data?.message || '注销失败')
    }
  }).catch(() => {
    // User canceled
  })
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
.label-value {
  font-size: 13px;
  color: var(--color-text-secondary);
}
.label-desc {
  font-size: 12px;
  color: var(--color-text-muted);
}
.danger-section {
  margin-bottom: 0;
}
.danger-title {
  color: #F56C6C !important;
  border-bottom-color: #fde2e2 !important;
}
</style>
