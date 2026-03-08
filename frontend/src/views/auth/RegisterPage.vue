<template>
  <div class="register-container">
    <div class="register-box">
      <div class="logo-wrapper">
        <h2>注册账号</h2>
      </div>
      
      <el-tabs v-model="activeRole" class="role-tabs">
        <el-tab-pane label="我是学生" name="STUDENT"></el-tab-pane>
        <el-tab-pane label="我是企业" name="ENTERPRISE"></el-tab-pane>
      </el-tabs>
      
      <el-form :model="form" :rules="rules" ref="registerFormRef" label-position="top">
        <!-- 基础信息 -->
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        
        <div class="row">
          <el-form-item label="真实姓名/联系人" prop="realName" class="half">
            <el-input v-model="form.realName" placeholder="请输入姓名" />
          </el-form-item>
          
          <el-form-item label="手机号码" prop="phone" class="half">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
        </div>

        <el-form-item label="邮箱地址" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>

        <!-- 学生特有字段 -->
        <template v-if="activeRole === 'STUDENT'">
          <div class="row">
            <el-form-item label="学校名称" prop="university" class="half">
              <el-input v-model="form.university" placeholder="请输入学校" />
            </el-form-item>
            <el-form-item label="专业名称" prop="major" class="half">
              <el-input v-model="form.major" placeholder="请输入专业" />
            </el-form-item>
          </div>
          <div class="row">
            <el-form-item label="学历层次" prop="educationLevel" class="half">
              <el-select v-model="form.educationLevel" placeholder="选择学历">
                <el-option label="本科" value="BACHELOR" />
                <el-option label="硕士" value="MASTER" />
                <el-option label="博士" value="PHD" />
              </el-select>
            </el-form-item>
            <el-form-item label="入学年份" prop="enrollmentYear" class="half">
              <el-date-picker
                v-model="form.enrollmentYear"
                type="year"
                placeholder="选择年份"
                value-format="YYYY"
              />
            </el-form-item>
          </div>
        </template>

        <!-- 企业特有字段 -->
        <template v-if="activeRole === 'ENTERPRISE'">
          <div class="row">
            <el-form-item label="企业名称" prop="companyName" class="half">
              <el-input v-model="form.companyName" placeholder="注册企业全称" />
            </el-form-item>
            <el-form-item label="所属行业" prop="industry" class="half">
              <el-input v-model="form.industry" placeholder="如：IT互联网" />
            </el-form-item>
          </div>
          <div class="row">
            <el-form-item label="联系人" prop="contactPerson" class="half">
              <el-input v-model="form.contactPerson" placeholder="招聘负责人" />
            </el-form-item>
            <el-form-item label="联系电话" prop="contactPhone" class="half">
              <el-input v-model="form.contactPhone" placeholder="负责人电话" />
            </el-form-item>
          </div>
        </template>
        
        <el-form-item>
          <el-button type="primary" class="register-btn" size="large" :loading="loading" @click="handleRegister">
            立 即 注 册
          </el-button>
        </el-form-item>
        
        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login">返回登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)
const activeRole = ref('STUDENT')

const form = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  university: '',
  major: '',
  educationLevel: '',
  enrollmentYear: '',
  companyName: '',
  industry: '',
  contactPerson: '',
  contactPhone: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  companyName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }]
}

// Clear form specific fields when role changes
watch(activeRole, () => {
  registerFormRef.value?.clearValidate()
})

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const payload = { ...form, role: activeRole.value }
        if (payload.enrollmentYear) {
          payload.enrollmentYear = parseInt(payload.enrollmentYear)
        }
        
        await register(payload)
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (error) {
        // Error Handled Globally
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--color-bg-secondary);
  padding: 40px 0;
}

.register-box {
  width: 500px;
  background: var(--color-bg-card);
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.logo-wrapper {
  text-align: center;
  margin-bottom: 20px;
}

.role-tabs {
  margin-bottom: 24px;
}

.row {
  display: flex;
  gap: 16px;
}

.half {
  flex: 1;
}

.register-btn {
  width: 100%;
  margin-top: 10px;
  background-color: var(--color-accent);
  border-color: var(--color-accent);
}

.register-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: var(--color-text-secondary);
}
</style>
