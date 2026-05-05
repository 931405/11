<template>
  <div class="settings-page">
    <div class="settings-card">
      <div class="settings-header">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="var(--color-accent)" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
        <h2>意见反馈</h2>
      </div>
      <p class="settings-subtitle">向我们提出您的问题、建议或意见，我们会尽快回复</p>

      <!-- Submit feedback form -->
      <div class="section">
        <h3 class="section-title">提交反馈</h3>
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
      </div>

      <!-- My feedback history -->
      <div class="section">
        <h3 class="section-title">我的反馈记录</h3>
        <div v-if="myFeedbacks.length === 0" class="empty-tip">
          <el-empty description="暂无反馈记录" :image-size="80" />
        </div>
        <div v-else class="feedback-list">
          <div v-for="item in myFeedbacks" :key="item.id" class="feedback-item">
            <div class="feedback-header">
              <span class="feedback-title">{{ item.title }}</span>
              <el-tag :type="item.status === 'RESOLVED' || item.status === 'REPLIED' ? 'success' : 'warning'" effect="light" size="small">
                {{ item.status === 'RESOLVED' || item.status === 'REPLIED' ? '已回复' : '待处理' }}
              </el-tag>
            </div>
            <div class="feedback-content">{{ item.content }}</div>
            <div class="feedback-meta">
              <span class="feedback-time">{{ formatDate(item.createdAt) }}</span>
              <span v-if="item.contactInfo" class="feedback-contact">联系方式：{{ item.contactInfo }}</span>
            </div>
            <div v-if="item.adminReply" class="feedback-reply">
              <div class="reply-label">管理员回复：</div>
              <div class="reply-content">{{ item.adminReply }}</div>
            </div>
          </div>
        </div>
        <div class="pagination-wrapper" v-if="total > size">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :page-sizes="[5, 10]"
            background
            layout="total, prev, pager, next"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
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

const myFeedbacks = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(5)

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
  try {
    const res = await getMyFeedbacks({ page: page.value, size: size.value })
    myFeedbacks.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to load feedbacks', error)
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
.settings-page {
  padding: 32px 0;
  max-width: 800px;
  margin: 0 auto;
}

.settings-card {
  background: var(--color-bg-card);
  border-radius: 12px;
  padding: 32px;
  border: 1px solid var(--color-border);
}

.settings-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.settings-header h2 {
  color: var(--color-text-primary);
  font-size: 20px;
  font-weight: 600;
}

.settings-subtitle {
  color: var(--color-text-secondary);
  font-size: 14px;
  margin-bottom: 24px;
}

.section {
  margin-bottom: 32px;
}

.section-title {
  color: var(--color-text-primary);
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-border);
}

.empty-tip {
  padding: 20px 0;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feedback-item {
  background: var(--color-bg-secondary);
  border-radius: 8px;
  padding: 16px;
  border: 1px solid var(--color-border);
}

.feedback-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.feedback-title {
  font-weight: 600;
  color: var(--color-text-primary);
  font-size: 14px;
}

.feedback-content {
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 8px;
}

.feedback-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.feedback-reply {
  margin-top: 12px;
  padding: 12px;
  background: var(--color-bg-card);
  border-radius: 6px;
  border: 1px solid var(--color-accent);
}

.reply-label {
  font-size: 12px;
  color: var(--color-accent);
  font-weight: 600;
  margin-bottom: 4px;
}

.reply-content {
  font-size: 13px;
  color: var(--color-text-primary);
  line-height: 1.5;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}
</style>
