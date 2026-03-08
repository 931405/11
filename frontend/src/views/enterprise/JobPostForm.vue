<template>
  <div class="job-form-container">
    <el-page-header @back="router.back()" class="page-header">
      <template #content>
        <span class="header-title">{{ isEdit ? '编辑职位' : '发布新职位' }}</span>
      </template>
    </el-page-header>

    <div class="form-wrapper" v-loading="initLoading">
      <el-card shadow="never" class="form-card">
        <el-form 
          ref="formRef" 
          :model="form" 
          :rules="rules" 
          label-position="top"
          label-width="120px"
        >
          <div class="section-title">基本信息</div>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="职位名称" prop="title">
                <el-input v-model="form.title" placeholder="如：系统前端开发实习生" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="职位分类" prop="categoryId">
                <el-select v-model="form.categoryId" placeholder="请选择分类" class="w-100">
                  <el-option 
                    v-for="cat in categories" 
                    :key="cat.id" 
                    :label="cat.name" 
                    :value="cat.id" 
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="薪资范围（元/时）" prop="salaryMin">
                <div class="salary-range">
                  <el-input-number v-model="form.salaryMin" :min="0" :step="5" placeholder="最低" controls-position="right" class="range-input" />
                  <span class="separator">-</span>
                  <el-input-number v-model="form.salaryMax" :min="form.salaryMin || 0" :step="5" placeholder="最高" controls-position="right" class="range-input" />
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="招聘人数" prop="headcount">
                <el-input-number v-model="form.headcount" :min="1" :max="999" controls-position="right" class="w-100" />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="section-title mt-4">工作细节</div>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="工作地点" prop="workLocation">
                <el-input v-model="form.workLocation" placeholder="匹配算法将基于此推算，如：海淀区/朝阳区/清华大学校内" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="工作时间安排" prop="workSchedule">
                <el-input v-model="form.workSchedule" placeholder="如：周一至周五下午，或周末全天" />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="section-title mt-4">职位要求</div>
          <el-form-item label="所需技能标签（算法精准匹配依据，输入后按回车添加）">
            <div class="skills-input-container">
              <el-tag
                v-for="tag in skillTags"
                :key="tag"
                class="skill-tag"
                closable
                @close="handleCloseTag(tag)"
                effect="plain"
              >
                {{ tag }}
              </el-tag>
              <el-input
                v-if="inputVisible"
                ref="InputRef"
                v-model="inputValue"
                class="skill-input"
                size="small"
                @keyup.enter="handleInputConfirm"
                @blur="handleInputConfirm"
              />
              <el-button v-else class="button-new-tag" size="small" @click="showInput">
                + 新标签
              </el-button>
            </div>
          </el-form-item>

          <el-form-item label="职位描述" prop="description">
            <el-input 
              v-model="form.description" 
              type="textarea" 
              :rows="5" 
              placeholder="请详细描述工作内容、职责等" 
            />
          </el-form-item>

          <el-form-item label="任职要求" prop="requirements">
            <el-input 
              v-model="form.requirements" 
              type="textarea" 
              :rows="5" 
              placeholder="请输入对应聘者的学历、能力、经验等要求" 
            />
          </el-form-item>
          
          <el-form-item label="是否立即开放招聘">
            <el-switch
              v-model="form.status"
              active-value="OPEN"
              inactive-value="CLOSED"
              active-text="开放"
              inactive-text="关闭"
            />
          </el-form-item>

          <div class="form-actions">
            <el-button @click="router.back()">取消</el-button>
            <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
              {{ isEdit ? '保存修改' : '确认发布' }}
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { createJob, updateJob } from '@/api/enterprise'
import { getJobDetail, getCategories } from '@/api/job'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const isEdit = route.name === 'EnterpriseJobEdit'
const jobId = route.params.id

const formRef = ref(null)
const initLoading = ref(false)
const submitLoading = ref(false)
const categories = ref([])

// Tags handling
const inputValue = ref('')
const skillTags = ref([])
const inputVisible = ref(false)
const InputRef = ref()

const form = reactive({
  title: '',
  jobType: '综合',
  categoryId: '',
  description: '',
  requirements: '',
  salaryMin: 0,
  salaryMax: 0,
  workLocation: '',
  workSchedule: '',
  headcount: 1,
  status: 'OPEN'
})

const rules = {
  title: [{ required: true, message: '请输入职位名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择职位分类', trigger: 'change' }],
  jobType: [{ required: true, message: '请选择实习类型', trigger: 'change' }],
  salaryMin: [{ required: true, message: '请输入最低时薪', trigger: 'blur' }],
  workLocation: [{ required: true, message: '请输入工作地点', trigger: 'blur' }],
  description: [{ required: true, message: '请输入职位描述', trigger: 'blur' }]
}

const loadCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch (error) {
    console.error('Failed to load categories', error)
  }
}

const loadJobData = async () => {
  if (!isEdit) return
  
  initLoading.value = true
  try {
    const data = await getJobDetail(jobId)
    if (data) {
      Object.keys(form).forEach(key => {
        if (data[key] !== undefined) form[key] = data[key]
      })
      try {
        skillTags.value = JSON.parse(data.skillsRequired || '[]')
      } catch (e) {
        skillTags.value = []
      }
    }
  } catch (error) {
    console.error('Failed to load job', error)
    ElMessage.error('无法加载职位数据')
    router.back()
  } finally {
    initLoading.value = false
  }
}

const handleCloseTag = (tag) => {
  skillTags.value.splice(skillTags.value.indexOf(tag), 1)
}

const showInput = () => {
  inputVisible.value = true
  nextTick(() => {
    InputRef.value.input.focus()
  })
}

const handleInputConfirm = () => {
  if (inputValue.value && !skillTags.value.includes(inputValue.value)) {
    skillTags.value.push(inputValue.value)
  }
  inputVisible.value = false
  inputValue.value = ''
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const payload = { ...form }
        payload.skillsRequired = JSON.stringify(skillTags.value)
        
        // Ensure numbers are numbers
        payload.salaryMin = Number(payload.salaryMin) || 0
        payload.salaryMax = Number(payload.salaryMax) || 0
        payload.headcount = Number(payload.headcount) || 1
        
        if (isEdit) {
          await updateJob(jobId, payload)
          ElMessage.success('职位修改成功')
        } else {
          await createJob(payload)
          ElMessage.success('职位发布成功')
        }
        router.push('/enterprise/jobs')
      } catch (error) {
        // Validation handles (e.g., enterprise certification error) are handled by global interceptor,
        // but it might throw up here. The interceptor already shows the ElMessage, so we do nothing.
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(async () => {
  await loadCategories()
  await loadJobData()
})
</script>

<style scoped>
.job-form-container {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
  background: var(--color-bg-card);
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.header-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.form-card {
  border-radius: 8px;
  border-color: var(--color-border);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-accent);
  margin-bottom: 20px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-border);
}

.mt-4 {
  margin-top: 32px;
}

.w-100 {
  width: 100%;
}

.salary-range {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.range-input {
  flex: 1;
}

.separator {
  color: var(--color-text-secondary);
}

.skills-input-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  background-color: var(--color-bg-card);
  min-height: 48px;
  align-items: center;
  width: 100%;
}

.skill-tag {
  background-color: var(--color-bg-secondary);
  color: var(--color-text-primary);
  border-color: var(--color-border);
}

.skill-input {
  width: 120px;
}

.button-new-tag {
  color: var(--color-accent);
  background: transparent;
  border: 1px dashed var(--color-border);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border);
}
</style>
