<template>
  <div class="feedback-container">
    <div class="page-title">
      <h2>意见反馈</h2>
      <p class="subtitle">向我们提出您的问题、建议或意见，我们会尽快回复</p>
    </div>

    <!-- Submit feedback -->
    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="card-header">提交反馈</div>
      </template>
      <el-form label-position="top" :model="feedbackForm">
        <el-form-item label="反馈类型">
          <el-select v-model="feedbackForm.type" placeholder="请选择反馈类型" style="width: 100%">
            <el-option label="功能建议" value="SUGGESTION" />
            <el-option label="问题反馈" value="FEEDBACK" />
            <el-option label="Bug报告" value="BUG" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="反馈标题">
          <el-input v-model="feedbackForm.title" placeholder="简要描述您的反馈" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="反馈内容">
          <el-input v-model="feedbackForm.content" type="textarea" :rows="5" placeholder="详细描述您遇到的问题或建议..." maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item label="联系方式（选填）">
          <el-input v-model="feedbackForm.contactInfo" placeholder="方便我们联系您，如手机号、邮箱等" maxlength="200" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" color="#00a6a7" :loading="submitLoading" @click="handleSubmit">提交反馈</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- My feedback history -->
    <el-card shadow="never" class="table-card" style="margin-top: 24px">
      <template #header>
        <div class="card-header">我的反馈记录</div>
      </template>
      <div v-if="myFeedbacks.length === 0" class="empty-tip">
        <el-empty description="暂无反馈记录" :image-size="80" />
      </div>
      <el-table v-else
        v-loading="loading"
        :data="myFeedbacks"
        style="width: 100%"
        :header-cell-style="{ background: 'var(--color-bg-secondary)', color: 'var(--color-text-primary)' }"
      >
        <el-table-column label="提交时间" width="160">
          <template #default="scope">
            <span class="text-muted">{{ formatDate(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="标题" prop="title" min-width="150" show-overflow-tooltip />
        <el-table-column label="反馈内容" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.content }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'RESOLVED' || scope.row.status === 'REPLIED' ? 'success' : 'warning'" effect="light" size="small">
              {{ scope.row.status === 'RESOLVED' || scope.row.status === 'REPLIED' ? '已回复' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="管理员回复" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.adminReply" class="reply-text">{{ scope.row.adminReply }}</span>
            <span v-else class="text-muted">暂无回复</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20]"
          background
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { submitFeedback, getMyFeedbacks } from '@/api/feedback'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const feedbackForm = ref({
  title: '',
  content: '',
  contactInfo: '',
  type: 'FEEDBACK'
})
const submitLoading = ref(false)
const loading = ref(false)

const myFeedbacks = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const handleSubmit = async () => {
  if (!feedbackForm.value.title.trim()) {
    ElMessage.warning('请输入反馈标题')
    return
  }
  if (!feedbackForm.value.content.trim()) {
    ElMessage.warning('请输入反馈内容')
    return
  }

  submitLoading.value = true
  try {
    await submitFeedback(feedbackForm.value)
    ElMessage.success('反馈提交成功，我们会尽快处理')
    feedbackForm.value = { title: '', content: '', contactInfo: '', type: 'FEEDBACK' }
    fetchMyFeedbacks()
  } catch (error) {
    // handled globally
  } finally {
    submitLoading.value = false
  }
}

const fetchMyFeedbacks = async () => {
  loading.value = true
  try {
    const res = await getMyFeedbacks({ page: page.value, size: size.value })
    myFeedbacks.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to load feedbacks', error)
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  size.value = val
  fetchMyFeedbacks()
}

const handleCurrentChange = (val) => {
  page.value = val
  fetchMyFeedbacks()
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return dayjs(dateString).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  fetchMyFeedbacks()
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

.form-card, .table-card {
  border-radius: 8px;
  border-color: var(--color-border);
}

.card-header {
  font-weight: 600;
  color: var(--color-text-primary);
}

.empty-tip {
  padding: 20px 0;
}

.text-muted {
  color: var(--color-text-muted);
  font-size: 13px;
}

.reply-text {
  color: var(--color-accent);
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
}
</style>
