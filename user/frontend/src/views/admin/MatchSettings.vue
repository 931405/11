<template>
  <div class="page">
    <div class="panel">
      <div class="header">
        <div>
          <h1>匹配参数设置</h1>
          <p>用于控制技能、薪资、地点和时间的匹配权重。</p>
        </div>
        <div class="actions">
          <el-button @click="router.push('/admin/dashboard')">返回看板</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">保存参数</el-button>
        </div>
      </div>

      <el-form label-position="top" v-loading="loading">
        <el-form-item label="技能匹配权重">
          <el-slider v-model="form.skill_weight" :max="1" :step="0.05" show-input />
        </el-form-item>
        <el-form-item label="薪资匹配权重">
          <el-slider v-model="form.salary_weight" :max="1" :step="0.05" show-input />
        </el-form-item>
        <el-form-item label="地点匹配权重">
          <el-slider v-model="form.location_weight" :max="1" :step="0.05" show-input />
        </el-form-item>
        <el-form-item label="时间匹配权重">
          <el-slider v-model="form.schedule_weight" :max="1" :step="0.05" show-input />
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMatchSettings, saveMatchSettings } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  skill_weight: 0.4,
  salary_weight: 0.25,
  location_weight: 0.2,
  schedule_weight: 0.15
})

const loadSettings = async () => {
  loading.value = true
  try {
    const data = await getMatchSettings()
    form.skill_weight = Number(data.skill_weight ?? 0.4)
    form.salary_weight = Number(data.salary_weight ?? 0.25)
    form.location_weight = Number(data.location_weight ?? 0.2)
    form.schedule_weight = Number(data.schedule_weight ?? 0.15)
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await saveMatchSettings({
      skill_weight: String(form.skill_weight),
      salary_weight: String(form.salary_weight),
      location_weight: String(form.location_weight),
      schedule_weight: String(form.schedule_weight)
    })
    ElMessage.success('匹配参数已保存')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.page {
  padding: 32px 20px;
}

.panel {
  max-width: 900px;
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

@media (max-width: 900px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
  }

  .actions {
    width: 100%;
    flex-direction: column;
  }
}
</style>
