<template>
  <div class="feedback-container">
    <div class="page-title">
      <h2>用户意见反馈</h2>
      <p class="subtitle">查看并回复用户提交的使用问题与建议</p>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="filter-bar">
        <el-radio-group v-model="statusFilter" @change="handleFilterChange">
          <el-radio-button label="">全部记录</el-radio-button>
          <el-radio-button label="PENDING">未回复</el-radio-button>
          <el-radio-button label="REPLIED">已回复</el-radio-button>
        </el-radio-group>
      </div>

      <el-table
        v-loading="loading"
        :data="feedbacks"
        style="width: 100%"
        :header-cell-style="{ background: 'var(--color-bg-secondary)', color: 'var(--color-text-primary)' }"
      >
        <el-table-column label="提交时间" width="160">
          <template #default="scope">
            <span class="text-muted">{{ formatDate(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="反馈内容" min-width="250" show-overflow-tooltip>
          <template #default="scope">
            <strong>{{ scope.row.content }}</strong>
          </template>
        </el-table-column>

        <el-table-column label="联系方式" prop="contactInfo" width="160" show-overflow-tooltip />

        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'REPLIED' ? 'success' : 'warning'" effect="light">
              {{ scope.row.status === 'REPLIED' ? '已回复' : '未回复' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="scope">
            <el-button 
              type="primary" 
              link 
              @click="openReplyDialog(scope.row)"
            >
              {{ scope.row.status === 'REPLIED' ? '查看回复' : '立即回复' }}
            </el-button>
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

    <!-- Reply Dialog -->
    <el-dialog v-model="dialogVisible" :title="isReadOnly ? '查看反馈详情' : '回复用户反馈'" width="500px">
      <div v-if="currentFeedback">
        <el-descriptions :column="1" border direction="vertical" class="mb-20">
          <el-descriptions-item label="用户反馈内容">{{ currentFeedback.content }}</el-descriptions-item>
          <el-descriptions-item label="用户联系方式">{{ currentFeedback.contactInfo || '未提供' }}</el-descriptions-item>
        </el-descriptions>

        <el-form label-position="top">
          <el-form-item label="管理员回复">
            <el-input 
              v-model="replyText" 
              type="textarea" 
              :rows="4" 
              placeholder="请输入您对该反馈的回复说明..." 
              :disabled="isReadOnly"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ isReadOnly ? '关闭' : '取消' }}</el-button>
          <el-button 
            v-if="!isReadOnly"
            type="primary" 
            :loading="submitLoading" 
            @click="submitReply"
          >
            确认回复
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getFeedbackList, replyFeedback } from '@/api/admin'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)
const feedbacks = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const statusFilter = ref('')

const dialogVisible = ref(false)
const submitLoading = ref(false)
const currentFeedback = ref(null)
const replyText = ref('')
const isReadOnly = ref(false)

const fetchFeedbacks = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    const res = await getFeedbackList(params)
    feedbacks.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to load feedbacks', error)
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  page.value = 1
  fetchFeedbacks()
}

const handleSizeChange = (val) => {
  size.value = val
  fetchFeedbacks()
}

const handleCurrentChange = (val) => {
  page.value = val
  fetchFeedbacks()
}

const openReplyDialog = (row) => {
  currentFeedback.value = row
  if (row.status === 'REPLIED') {
    isReadOnly.value = true
    replyText.value = row.replyContent
  } else {
    isReadOnly.value = false
    replyText.value = ''
  }
  dialogVisible.value = true
}

const submitReply = async () => {
  if (!replyText.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  submitLoading.value = true
  try {
    await replyFeedback(currentFeedback.value.id, replyText.value)
    ElMessage.success('回复成功')
    dialogVisible.value = false
    fetchFeedbacks()
  } catch (error) {
    // Handled globally
  } finally {
    submitLoading.value = false
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return dayjs(dateString).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  fetchFeedbacks()
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

.table-card {
  border-radius: 8px;
  border-color: var(--color-border);
}

.filter-bar {
  margin-bottom: 20px;
}

.text-muted {
  color: var(--color-text-muted);
  font-size: 13px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
}

.mb-20 {
  margin-bottom: 20px;
}
</style>
