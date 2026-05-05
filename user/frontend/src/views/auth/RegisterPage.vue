<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="header">
        <h1>注册账号</h1>
        <p>支持学生、企业和管理员三种身份</p>
      </div>

      <el-tabs v-model="role" class="role-tabs">
        <el-tab-pane label="我是学生" name="STUDENT" />
        <el-tab-pane label="我是企业" name="ENTERPRISE" />
        <el-tab-pane label="我是管理员" name="ADMIN" />
      </el-tabs>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入不少于 6 位的密码" />
        </el-form-item>

        <div class="grid">
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
        </div>

        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>

        <template v-if="role === 'STUDENT'">
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
        </template>

        <template v-if="role === 'ENTERPRISE'">
          <div class="grid">
            <el-form-item label="企业名称">
              <el-input v-model="form.companyName" placeholder="请输入企业名称" />
            </el-form-item>
            <el-form-item label="所属行业">
              <el-input v-model="form.industry" placeholder="例如：互联网、教育、零售" />
            </el-form-item>
            <el-form-item label="联系人">
              <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </div>
        </template>

        <el-button type="primary" class="submit-btn" :loading="loading" @click="handleRegister">注册</el-button>

        <div class="footer">
          <span>已经有账号？</span>
          <router-link to="/login">返回登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const role = ref('STUDENT')

const form = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  university: '',
  major: '',
  educationLevel: '',
  enrollmentYear: null,
  companyName: '',
  industry: '',
  contactPerson: '',
  contactPhone: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

const handleRegister = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (role.value === 'ENTERPRISE' && !form.companyName.trim()) {
    ElMessage.warning('请输入企业名称')
    return
  }

  loading.value = true
  try {
    await register({
      ...form,
      role: role.value
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
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
  max-width: 720px;
  padding: 32px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 24px 60px rgba(27, 44, 51, 0.12);
}

.header {
  text-align: center;
  margin-bottom: 20px;
}

.header h1 {
  font-size: 30px;
  margin-bottom: 8px;
}

.header p {
  color: var(--color-text-secondary);
}

.role-tabs {
  margin-bottom: 12px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
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

@media (max-width: 900px) {
  .auth-card {
    padding: 24px;
  }

  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
