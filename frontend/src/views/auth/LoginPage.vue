<template>
  <div class="login-container">
    <div class="login-box">
      <div class="logo-wrapper">
        <el-icon :size="40" color="var(--color-accent)"><Briefcase /></el-icon>
        <h2>兼职匹配平台</h2>
      </div>
      
      <el-form :model="form" :rules="rules" ref="loginFormRef" size="large">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="用户名" 
            :prefix-icon="'User'"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="密码" 
            :prefix-icon="'Lock'"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
        
        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: '123'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(form)
        userStore.setLoginInfo(res)
        ElMessage.success('登录成功')
        
        if (userStore.isStudent()) router.push('/student/dashboard')
        else if (userStore.isEnterprise()) router.push('/enterprise/dashboard')
        else if (userStore.isAdmin()) router.push('/admin/dashboard')
        else router.push('/')
        
      } catch (error) {
        // Error is handled by request interceptor
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--color-bg-secondary);
}

.login-box {
  width: 400px;
  background: var(--color-bg-card);
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.logo-wrapper {
  text-align: center;
  margin-bottom: 30px;
}

.logo-wrapper h2 {
  margin-top: 10px;
  color: var(--color-text-primary);
  font-weight: 500;
}

.login-btn {
  width: 100%;
  background-color: var(--color-accent);
  border-color: var(--color-accent);
}

.login-btn:hover {
  background-color: var(--color-accent-hover);
  border-color: var(--color-accent-hover);
}

.login-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: var(--color-text-secondary);
}
</style>
