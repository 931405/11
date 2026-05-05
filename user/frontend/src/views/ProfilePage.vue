<template>
  <div class="page">
    <div class="card">
      <div class="header">
        <div>
          <p class="eyebrow">基础资料管理</p>
          <h1>维护账号基础信息</h1>
          <p class="desc">这里用于维护登录账号、联系方式以及角色基础资料。学生的详细简历编辑在“简历完善”页面。</p>
        </div>
        <div class="actions">
          <el-button @click="router.push('/home')">返回首页</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">保存资料</el-button>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" v-loading="loading">
        <div class="section">
          <div class="section-title">
            <h2>账号信息</h2>
            <el-tag type="success">{{ roleText }}</el-tag>
          </div>

          <div class="grid">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </div>

          <el-form-item label="头像地址" prop="avatar">
            <el-input v-model="form.avatar" placeholder="可选，填写头像图片链接" />
          </el-form-item>
        </div>

        <div v-if="form.role === 'STUDENT'" class="section">
          <div class="section-title">
            <h2>学生基础资料</h2>
            <el-button type="primary" link @click="router.push('/student/profile-edit')">进入详细简历编辑</el-button>
          </div>

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

          <el-form-item label="个人介绍">
            <el-input v-model="form.selfIntro" type="textarea" :rows="4" placeholder="请输入个人介绍" />
          </el-form-item>
        </div>

        <div v-if="form.role === 'ENTERPRISE'" class="section">
          <div class="section-title">
            <h2>企业资料</h2>
            <el-tag>{{ certificationText }}</el-tag>
          </div>

          <div class="grid">
            <el-form-item label="企业名称">
              <el-input v-model="form.companyName" placeholder="请输入企业名称" />
            </el-form-item>
            <el-form-item label="所属行业">
              <el-input v-model="form.industry" placeholder="请输入所属行业" />
            </el-form-item>
            <el-form-item label="企业规模">
              <el-select v-model="form.companySize" placeholder="请选择企业规模">
                <el-option label="50 人以下" value="SMALL" />
                <el-option label="50 - 200 人" value="MEDIUM" />
                <el-option label="200 - 1000 人" value="LARGE" />
                <el-option label="1000 人以上" value="ENTERPRISE" />
              </el-select>
            </el-form-item>
            <el-form-item label="联系人">
              <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="公司地址">
              <el-input v-model="form.companyAddress" placeholder="请输入公司地址" />
            </el-form-item>
          </div>

          <el-form-item label="企业介绍">
            <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入企业介绍" />
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyProfile, updateMyProfile } from '@/api/profile'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  userId: null,
  username: '',
  realName: '',
  phone: '',
  email: '',
  avatar: '',
  role: '',
  university: '',
  major: '',
  educationLevel: '',
  enrollmentYear: null,
  selfIntro: '',
  expectedLocation: '',
  availableSchedule: '',
  companyName: '',
  industry: '',
  companySize: '',
  contactPerson: '',
  contactPhone: '',
  companyAddress: '',
  description: '',
  certificationStatus: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

const roleText = computed(() => {
  if (form.role === 'STUDENT') return '学生'
  if (form.role === 'ENTERPRISE') return '企业'
  if (form.role === 'ADMIN') return '管理员'
  return '未知'
})

const certificationText = computed(() => {
  if (form.certificationStatus === 'APPROVED') return '认证已通过'
  if (form.certificationStatus === 'REJECTED') return '认证未通过'
  if (form.certificationStatus === 'PENDING') return '认证待审核'
  return '未设置'
})

const fillForm = (data = {}) => {
  Object.keys(form).forEach((key) => {
    form[key] = data[key] ?? (typeof form[key] === 'number' ? null : '')
  })
}

const loadProfile = async () => {
  loading.value = true
  try {
    const data = await getMyProfile()
    fillForm(data)
    userStore.updateUserInfo(data)
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const data = await updateMyProfile({ ...form })
    fillForm(data)
    userStore.updateUserInfo(data)
    ElMessage.success('资料保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 36px 20px;
  background:
    radial-gradient(circle at top right, rgba(0, 166, 167, 0.14), transparent 30%),
    linear-gradient(180deg, #f7fbfb 0%, #eef3f4 100%);
}

.card {
  max-width: 1100px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  padding: 32px;
  box-shadow: 0 20px 60px rgba(27, 44, 51, 0.1);
}

.header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.eyebrow {
  color: var(--color-accent);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: 8px;
}

.header h1 {
  font-size: 32px;
  margin-bottom: 8px;
}

.desc {
  color: var(--color-text-secondary);
}

.actions {
  display: flex;
  gap: 12px;
}

.section {
  margin-bottom: 24px;
  padding: 24px;
  border-radius: 20px;
  background: #f7fbfb;
  border: 1px solid rgba(0, 166, 167, 0.1);
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.section-title h2 {
  font-size: 22px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 900px) {
  .card {
    padding: 24px;
  }

  .header {
    flex-direction: column;
  }

  .actions {
    flex-direction: column;
  }

  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
