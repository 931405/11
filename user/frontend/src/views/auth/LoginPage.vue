<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="header">
        <h1>兼职通</h1>
        <p>请输入账号和密码登录系统</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>

        <el-button type="primary" class="submit-btn" :loading="loading" @click="handleLogin">登录</el-button>

        <div class="footer">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = await login(form)
    userStore.setLoginInfo(data)
    ElMessage.success('登录成功')
    router.push('/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(circle at top right, rgba(0, 166, 167, 0.16), transparent 32%),
    linear-gradient(180deg, #f7fbfb 0%, #eef3f4 100%);
}

.auth-card {
  width: 100%;
  max-width: 460px;
  padding: 32px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 24px 60px rgba(27, 44, 51, 0.12);
}

.header {
  text-align: center;
  margin-bottom: 24px;
}

.header h1 {
  font-size: 30px;
  margin-bottom: 8px;
}

.header p {
  color: var(--color-text-secondary);
}

.submit-btn {
  width: 100%;
}

.footer {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  gap: 8px;
  color: var(--color-text-secondary);
}
</style>
