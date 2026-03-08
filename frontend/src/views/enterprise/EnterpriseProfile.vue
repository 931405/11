<template>
  <div class="enterprise-profile-container">
    <div class="page-title">
      <h2>企业主页与资质认证</h2>
      <p class="subtitle">完善企业信息并上传营业执照以获取发布职位的权限</p>
    </div>

    <div class="main-content" v-loading="loading">
      <!-- Status Banner -->
      <div v-if="form.certificationStatus" class="cert-status-banner" :class="getCertClass(form.certificationStatus)">
        <el-icon class="status-icon"><component :is="getCertIcon(form.certificationStatus)" /></el-icon>
        <div class="status-content">
          <div class="status-title">认证状态：{{ getCertLabel(form.certificationStatus) }}</div>
          <div v-if="form.certificationStatus === 'REJECTED'" class="status-remark">
            驳回原因: {{ form.auditRemark }}。请修改下方信息后重新提交认证。
          </div>
          <div v-if="form.certificationStatus === 'PENDING'" class="status-remark">
            平台管理员正在加急审核您的企业资质，通常需要1-2个工作日。
          </div>
        </div>
      </div>

      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-position="top"
        class="profile-form"
      >
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header">
              <el-icon><OfficeBuilding /></el-icon>基础信息
            </div>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="企业全称 (需与营业执照一致)" prop="companyName">
                <el-input v-model="form.companyName" placeholder="注册企业全称" :disabled="form.certificationStatus === 'APPROVED'" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="统一社会信用代码" prop="businessLicenseNo">
                <el-input v-model="form.businessLicenseNo" placeholder="请输入18位信用代码" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="所属行业" prop="industry">
                <el-input v-model="form.industry" placeholder="如：IT/互联网、教育培训" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="人员规模" prop="companySize">
                <el-select v-model="form.companySize" class="w-100">
                  <el-option label="0-20人" value="0-20人" />
                  <el-option label="20-99人" value="20-99人" />
                  <el-option label="100-499人" value="100-499人" />
                  <el-option label="500人以上" value="500人以上" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="公司官网">
                <el-input v-model="form.website" placeholder="http:// 开头" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="企业简介" prop="description">
            <el-input 
              v-model="form.description" 
              type="textarea" 
              :rows="4" 
              placeholder="简单介绍您的企业，展示雇主品牌形象" 
            />
          </el-form-item>
        </el-card>

        <el-card shadow="never" class="section-card mt-24">
          <template #header>
            <div class="card-header">
              <el-icon><Service /></el-icon>联系方式
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="招聘负责人姓名" prop="contactPerson">
                <el-input v-model="form.contactPerson" placeholder="请输入姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="负责人电话" prop="contactPhone">
                <el-input v-model="form.contactPhone" placeholder="请输入手机号或座机" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="企业办公地址" prop="address">
                <el-input v-model="form.address" placeholder="请输入详细的办公地址" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <!-- Mock Image Upload for Business License -->
        <el-card shadow="never" class="section-card mt-24">
          <template #header>
            <div class="card-header">
              <el-icon><Picture /></el-icon>资质材料 (营业执照扫描件)
            </div>
          </template>
          <el-form-item prop="licenseUrl">
            <div class="upload-stub">
              <el-input v-model="form.licenseUrl" placeholder="输入图片外链URL (例如对象存储的链接)" />
              <div class="upload-hint">支持 jpg, png 格式，需清晰可见。必须上传方可通过企业资质认证审核。</div>
            </div>
          </el-form-item>
        </el-card>
      </el-form>

      <div class="form-actions mt-24">
        <el-button 
          v-if="form.certificationStatus === 'APPROVED'" 
          type="primary" 
          size="large" 
          :loading="submitLoading" 
          @click="saveProfile(false)"
        >
          保存修改
        </el-button>
        
        <el-button 
          v-else 
          type="primary" 
          size="large" 
          :loading="submitLoading" 
          @click="saveProfile(true)"
        >
          保存并提交认证审核
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getEnterpriseInfo, updateEnterpriseInfo } from '@/api/enterprise'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  companyName: '',
  description: '',
  industry: '',
  companySize: '',
  contactPerson: '',
  contactPhone: '',
  address: '',
  website: '',
  businessLicenseNo: '',
  licenseUrl: '',
  certificationStatus: '',
  auditRemark: ''
})

const rules = {
  companyName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入招聘负责人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  licenseUrl: [{ required: true, message: '请提供营业执照以认证真实身份', trigger: 'blur' }]
}

const fetchProfile = async () => {
  loading.value = true
  try {
    const rawData = await getEnterpriseInfo()
    if (rawData) {
      Object.keys(form).forEach(key => {
        if (rawData[key] !== undefined && rawData[key] !== null) {
          form[key] = rawData[key]
        }
      })
    }
  } catch (error) {
    console.error('Failed to load info', error)
  } finally {
    loading.value = false
  }
}

const saveProfile = async (submitForAudit) => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const payload = { ...form }
        
        // If they chose to submit for audit (they were uncertified or rejected)
        if (submitForAudit) {
          payload.certificationStatus = 'PENDING'
        }
        
        await updateEnterpriseInfo(payload)
        
        if (submitForAudit) {
          ElMessage.success('认证资料提交成功，请等待管理员审核')
          form.certificationStatus = 'PENDING'
        } else {
          ElMessage.success('企业主页保存成功')
        }
        
      } catch (error) {
        // Handled globally
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const getCertLabel = (status) => {
  const map = {
    'APPROVED': '审核通过 (已认证)',
    'PENDING': '正在审核',
    'REJECTED': '审核被驳回'
  }
  return map[status] || '未提交'
}

const getCertClass = (status) => {
  const map = {
    'APPROVED': 'banner-success',
    'PENDING': 'banner-warning',
    'REJECTED': 'banner-danger'
  }
  return map[status] || 'banner-default'
}

const getCertIcon = (status) => {
  const map = {
    'APPROVED': 'CircleCheckFilled',
    'PENDING': 'WarningFilled',
    'REJECTED': 'CircleCloseFilled'
  }
  return map[status] || 'InfoFilled'
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.page-title {
  margin-bottom: 24px;
}

.page-title h2 {
  color: var(--color-text-primary);
  margin-bottom: 8px;
  font-weight: 600;
}

.subtitle {
  color: var(--color-text-secondary);
  font-size: 14px;
}

.cert-status-banner {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.banner-success {
  background-color: rgba(123, 163, 123, 0.1);
  border: 1px solid var(--color-success);
}
.banner-success .status-icon { color: var(--color-success); }

.banner-warning {
  background-color: rgba(196, 163, 90, 0.1);
  border: 1px solid var(--color-warning);
}
.banner-warning .status-icon { color: var(--color-warning); }

.banner-danger {
  background-color: rgba(176, 112, 112, 0.1);
  border: 1px solid var(--color-danger);
}
.banner-danger .status-icon { color: var(--color-danger); }

.status-icon {
  font-size: 24px;
  margin-top: 2px;
}

.status-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}

.status-remark {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.section-card {
  border-radius: 8px;
  border-color: var(--color-border);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.mt-24 {
  margin-top: 24px;
}

.w-100 {
  width: 100%;
}

.upload-stub {
  width: 100%;
}

.upload-hint {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 8px;
}

.form-actions {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.form-actions .el-button {
  width: 240px;
}
</style>
