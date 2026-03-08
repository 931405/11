<template>
  <div class="match-settings">
    <el-card shadow="never" class="settings-card">
      <template #header>
        <div class="card-header">
          <el-icon><Setting /></el-icon>
          <span>匹配算法参数配置</span>
        </div>
      </template>

      <div class="settings-content" v-loading="loading">
        <el-alert
          title="参数调整说明"
          type="info"
          description="在此页面调节各个维度的权重分配。所有权重总和不必须为100%，系统在计算最终匹配度时会基于各个权重计算综合得分。"
          show-icon
          :closable="false"
          class="info-alert"
        />

        <el-form label-position="top" class="settings-form">
          <el-form-item label="技能匹配度权重">
            <div class="slider-container">
              <el-slider v-model="settings.skill_weight" :max="1" :step="0.05" show-input />
            </div>
            <div class="setting-desc">控制学生技能标签与职位要求的匹配权重</div>
          </el-form-item>

          <el-form-item label="薪资期望匹配度权重">
            <div class="slider-container">
              <el-slider v-model="settings.salary_weight" :max="1" :step="0.05" show-input />
            </div>
            <div class="setting-desc">控制学生期望薪资与职位薪资范围的计算权重</div>
          </el-form-item>

          <el-form-item label="工作地点匹配度权重">
            <div class="slider-container">
              <el-slider v-model="settings.location_weight" :max="1" :step="0.05" show-input />
            </div>
            <div class="setting-desc">控制学生期望工作地区与职位地点的匹配度权重</div>
          </el-form-item>

          <el-form-item label="时间排期匹配度权重">
            <div class="slider-container">
              <el-slider v-model="settings.schedule_weight" :max="1" :step="0.05" show-input />
            </div>
            <div class="setting-desc">控制学生空闲时间与职位工作时间的匹配度权重</div>
          </el-form-item>

          <div class="form-actions">
            <el-button @click="resetSettings">重置为默认</el-button>
            <el-button type="primary" :loading="saving" @click="saveSettings">保存配置</el-button>
          </div>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMatchSettings, saveMatchSettings as saveSettingsApi } from '@/api/admin'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const saving = ref(false)

const settings = ref({
  skill_weight: 0.40,
  salary_weight: 0.25,
  location_weight: 0.20,
  schedule_weight: 0.15
})

const defaultSettings = {
  skill_weight: 0.40,
  salary_weight: 0.25,
  location_weight: 0.20,
  schedule_weight: 0.15
}

const loadSettings = async () => {
  loading.value = true
  try {
    const res = await getMatchSettings()
    if (res && res.skill_weight !== undefined) {
      settings.value = {
        skill_weight: parseFloat(res.skill_weight),
        salary_weight: parseFloat(res.salary_weight),
        location_weight: parseFloat(res.location_weight),
        schedule_weight: parseFloat(res.schedule_weight)
      }
    }
  } catch (error) {
    console.error('Failed to load settings', error)
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

const resetSettings = () => {
  settings.value = { ...defaultSettings }
}

const saveSettings = async () => {
  saving.value = true
  try {
    const dataToSave = {
      skill_weight: settings.value.skill_weight.toString(),
      salary_weight: settings.value.salary_weight.toString(),
      location_weight: settings.value.location_weight.toString(),
      schedule_weight: settings.value.schedule_weight.toString()
    }
    await saveSettingsApi(dataToSave)
    ElMessage.success('配置保存成功，匹配算法将使用新权重')
  } catch (error) {
    console.error('Failed to save settings', error)
    ElMessage.error('配置保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.match-settings {
  max-width: 800px;
  margin: 0 auto;
}

.settings-card {
  border-radius: 8px;
  border: 1px solid var(--color-border);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.settings-content {
  padding: 10px 0;
}

.info-alert {
  margin-bottom: 24px;
}

.settings-form {
  margin-top: 20px;
}

.slider-container {
  width: 100%;
  padding-right: 20px;
}

.setting-desc {
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.4;
  margin-top: -8px;
  margin-bottom: 16px;
}

.form-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
