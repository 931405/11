<template>
  <div class="job-audit-container">
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>职位内容审核</span>
          <div class="header-actions">
            <el-select v-model="filterStatus" placeholder="筛选状态" clearable @change="handleFilterChange" style="width: 150px">
              <el-option label="招聘中" value="OPEN" />
              <el-option label="已关闭" value="CLOSED" />
              <el-option label="已封禁" value="BANNED" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="jobs" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="职位名称" min-width="150">
          <template #default="scope">
            <strong>{{ scope.row.title }}</strong>
            <div class="text-muted" v-if="scope.row.workLocation">{{ scope.row.workLocation }}</div>
          </template>
        </el-table-column>
        <el-table-column label="企业ID" width="100">
          <template #default="scope">
            {{ scope.row.enterpriseId }}
          </template>
        </el-table-column>
        <el-table-column label="薪资范围" width="150">
          <template #default="scope">
            <span class="salary-text">{{ scope.row.salaryMin }} - {{ scope.row.salaryMax }} 元/天</span>
          </template>
        </el-table-column>
        <el-table-column label="数据表现" width="120">
          <template #default="scope">
            <div>浏览: {{ scope.row.viewCount }}</div>
            <div>投递: {{ scope.row.applyCount }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="150">
          <template #default="scope">
            <span class="text-muted">{{ formatTime(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <template v-if="scope.row.status !== 'BANNED'">
              <el-button type="danger" size="small" @click="handleBanJob(scope.row)">
                违规封禁
              </el-button>
            </template>
            <template v-else>
              <el-button type="success" size="small" @click="handleRestoreJob(scope.row)">
                解封恢复
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          background
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 封禁确认弹窗 -->
    <el-dialog v-model="banDialogVisible" title="职位违规封禁确认" width="400px">
      <el-alert title="注意：封禁后该职位将从学生端下架，但企业记录仍保留以此作为申诉依据。" type="warning" show-icon style="margin-bottom: 20px" />
      <el-form label-position="top">
        <el-form-item label="封禁原因（内部备注）">
          <el-input v-model="banRemark" type="textarea" :rows="3" placeholder="例如：涉嫌虚假招聘、违禁词汇等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="banDialogVisible = false">取消</el-button>
          <el-button type="danger" :loading="submitLoading" @click="submitBanJob">确认封禁</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getJobList, updateJobStatus } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)
const jobs = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const filterStatus = ref('')

const banDialogVisible = ref(false)
const currentBanJobId = ref(null)
const banRemark = ref('')
const submitLoading = ref(false)

const fetchJobs = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filterStatus.value) {
      params.status = filterStatus.value
    }
    const res = await getJobList(params)
    jobs.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to load jobs', error)
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  page.value = 1
  fetchJobs()
}

const handleSizeChange = (val) => {
  size.value = val
  fetchJobs()
}

const handleCurrentChange = (val) => {
  page.value = val
  fetchJobs()
}

const handleBanJob = (job) => {
  currentBanJobId.value = job.id
  banRemark.value = ''
  banDialogVisible.value = true
}

const submitBanJob = async () => {
  submitLoading.value = true
  try {
    // 假设后端接受 status 参数。若需要备注，可以在后续接口扩展，这里复用状态更改API。
    await updateJobStatus(currentBanJobId.value, 'BANNED')
    ElMessage.success('已成功封禁该职位')
    banDialogVisible.value = false
    fetchJobs()
  } catch (error) {
    //
  } finally {
    submitLoading.value = false
  }
}

const handleRestoreJob = (job) => {
  ElMessageBox.confirm(
    `是否要解除职位「${job.title}」的封禁状态，并恢复为招聘中？`,
    '解封确认',
    {
      confirmButtonText: '确定解封',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await updateJobStatus(job.id, 'OPEN')
      ElMessage.success('已解除封禁，职位可正常显示')
      fetchJobs()
    } catch (e) {
      //
    }
  }).catch(() => {})
}

const getStatusType = (status) => {
  const map = {
    'OPEN': 'success',
    'CLOSED': 'info',
    'BANNED': 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = {
    'OPEN': '招聘中',
    'CLOSED': '已关闭',
    'BANNED': '已封禁'
  }
  return map[status] || status
}

const formatTime = (time) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  fetchJobs()
})
</script>

<style scoped>
.job-audit-container {
  padding: 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
.text-muted {
  color: var(--color-text-muted);
  font-size: 13px;
  margin-top: 4px;
}
.salary-text {
  color: var(--color-danger);
  font-weight: 500;
}
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
