<template>
  <div class="page">
    <div class="panel">
      <div class="header">
        <div>
          <h1>岗位管理</h1>
          <p>可新增、编辑、删除岗位，并查看岗位候选人。</p>
        </div>
        <div class="actions">
          <el-button @click="router.push('/home')">返回首页</el-button>
          <el-button type="primary" @click="openDialog()">新增岗位</el-button>
        </div>
      </div>

      <el-table :data="jobs" v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="岗位名称" min-width="220" />
        <el-table-column prop="jobType" label="岗位类型" width="140" />
        <el-table-column prop="workLocation" label="工作地点" min-width="160" />
        <el-table-column prop="status" label="状态" width="120" align="center" />
        <el-table-column prop="applyCount" label="申请数" width="100" align="center" />
        <el-table-column label="操作" width="260" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="primary" link @click="router.push(`/enterprise/jobs/${row.id}/candidates`)">候选人</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑岗位' : '新增岗位'" width="720px">
      <el-form :model="form" label-position="top">
        <div class="grid">
          <el-form-item label="岗位名称">
            <el-input v-model="form.title" placeholder="请输入岗位名称" />
          </el-form-item>
          <el-form-item label="岗位类型">
            <el-input v-model="form.jobType" placeholder="例如：兼职、实习、远程" />
          </el-form-item>
          <el-form-item label="岗位分类">
            <el-select v-model="form.categoryId" placeholder="请选择分类">
              <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="工作地点">
            <el-input v-model="form.workLocation" placeholder="请输入工作地点" />
          </el-form-item>
          <el-form-item label="最低时薪">
            <el-input-number v-model="form.salaryMin" :min="0" :step="5" />
          </el-form-item>
          <el-form-item label="最高时薪">
            <el-input-number v-model="form.salaryMax" :min="0" :step="5" />
          </el-form-item>
          <el-form-item label="招聘人数">
            <el-input-number v-model="form.headcount" :min="1" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status">
              <el-option label="开放中" value="OPEN" />
              <el-option label="已关闭" value="CLOSED" />
              <el-option label="草稿" value="DRAFT" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="技能要求（逗号分隔）">
          <el-input v-model="form.skillsText" placeholder="例如：Java, Vue, 沟通能力" />
        </el-form-item>
        <el-form-item label="岗位描述">
          <el-input v-model="form.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="岗位要求">
          <el-input v-model="form.requirements" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { createJob, deleteJob, getEnterpriseJobs, updateJob } from '@/api/enterprise'
import { getCategories } from '@/api/job'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const jobs = ref([])
const categories = ref([])

const emptyForm = () => ({
  title: '',
  jobType: '',
  categoryId: null,
  workLocation: '',
  salaryMin: null,
  salaryMax: null,
  headcount: 1,
  status: 'OPEN',
  skillsText: '',
  description: '',
  requirements: ''
})

const form = reactive(emptyForm())

const fillForm = (row) => {
  Object.assign(form, emptyForm())
  if (!row) return
  form.title = row.title || ''
  form.jobType = row.jobType || ''
  form.categoryId = row.categoryId || null
  form.workLocation = row.workLocation || ''
  form.salaryMin = row.salaryMin || null
  form.salaryMax = row.salaryMax || null
  form.headcount = row.headcount || 1
  form.status = row.status || 'OPEN'
  form.description = row.description || ''
  form.requirements = row.requirements || ''
  try {
    form.skillsText = JSON.parse(row.skillsRequired || '[]').join(', ')
  } catch (error) {
    form.skillsText = ''
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const [jobData, categoryData] = await Promise.all([
      getEnterpriseJobs({ page: 1, size: 50 }),
      getCategories()
    ])
    jobs.value = jobData.content || []
    categories.value = categoryData || []
  } finally {
    loading.value = false
  }
}

const openDialog = (row = null) => {
  editingId.value = row?.id || null
  fillForm(row)
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.title.trim()) {
    ElMessage.warning('请输入岗位名称')
    return
  }

  saving.value = true
  try {
    const payload = {
      title: form.title,
      jobType: form.jobType,
      categoryId: form.categoryId,
      workLocation: form.workLocation,
      salaryMin: form.salaryMin,
      salaryMax: form.salaryMax,
      headcount: form.headcount,
      status: form.status,
      description: form.description,
      requirements: form.requirements,
      skillsRequired: JSON.stringify(
        form.skillsText
          .split(',')
          .map((item) => item.trim())
          .filter(Boolean)
      )
    }
    if (editingId.value) {
      await updateJob(editingId.value, payload)
      ElMessage.success('岗位更新成功')
    } else {
      await createJob(payload)
      ElMessage.success('岗位创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除岗位“${row.title}”吗？`, '提示', { type: 'warning' })
  await deleteJob(row.id)
  ElMessage.success('岗位已删除')
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page {
  padding: 32px 20px;
}

.panel {
  max-width: 1180px;
  margin: 0 auto;
  background: #fff;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 10px 40px rgba(27, 44, 51, 0.08);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.header p {
  color: var(--color-text-secondary);
}

.actions {
  display: flex;
  gap: 12px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 900px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
  }

  .actions {
    width: 100%;
    flex-direction: column;
  }

  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
