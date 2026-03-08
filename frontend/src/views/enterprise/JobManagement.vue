<template>
  <div class="job-manage-container">
    <div class="page-header">
      <div class="header-left">
        <h2>职位管理</h2>
        <p class="subtitle">管理企业发布的兼职职位，查看投递候选人</p>
      </div>
      <div class="header-right">
        <el-button type="primary" size="large" @click="$router.push('/enterprise/jobs/create')">
          <el-icon><Plus /></el-icon> 发布新职位
        </el-button>
      </div>
    </div>

    <el-card shadow="never" class="table-card">
      <!-- 搜索过滤栏 -->
      <div class="filter-tabs">
        <div 
          class="filter-tab" 
          :class="{ active: statusFilter === '' }"
          @click="statusFilter = ''; handleFilterChange()"
        >全部状态</div>
        <div 
          class="filter-tab" 
          :class="{ active: statusFilter === 'OPEN' }"
          @click="statusFilter = 'OPEN'; handleFilterChange()"
        >招聘中</div>
        <div 
          class="filter-tab" 
          :class="{ active: statusFilter === 'CLOSED' }"
          @click="statusFilter = 'CLOSED'; handleFilterChange()"
        >已结束</div>
      </div>

      <el-table
        v-loading="loading"
        :data="jobs"
        style="width: 100%"
        :header-cell-style="{ background: 'var(--color-bg-secondary)', color: 'var(--color-text-primary)' }"
      >
        <el-table-column label="职位名称" min-width="180">
          <template #default="scope">
            <div class="job-title-cell">
              <strong>{{ scope.row.title }}</strong>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="薪资范围" width="150" align="center">
          <template #default="scope">
            <span class="salary-text">{{ scope.row.salaryMin }} - {{ scope.row.salaryMax }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'OPEN' ? 'success' : 'info'" effect="light">
              {{ scope.row.status === 'OPEN' ? '招聘中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="数据" width="160" align="center">
          <template #default="scope">
            <div class="data-cell">
              <span><el-icon><View /></el-icon> {{ scope.row.viewCount }}</span>
              <el-divider direction="vertical" />
              <span class="highlight"><el-icon><User /></el-icon> {{ scope.row.applyCount }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="发布时间" width="160" align="center">
          <template #default="scope">
            <span class="text-muted">{{ formatDate(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                link 
                @click="$router.push(`/enterprise/jobs/${scope.row.id}/candidates`)"
              >
                查看候选人 ({{ scope.row.applyCount }})
              </el-button>
              
              <el-button type="info" link @click="$router.push(`/enterprise/jobs/${scope.row.id}/edit`)">
                编辑
              </el-button>
              
              <el-button 
                v-if="scope.row.status === 'OPEN'"
                type="warning" 
                link 
                @click="toggleJobStatus(scope.row)"
              >
                停止招聘
              </el-button>
              <el-button 
                v-else
                type="success" 
                link 
                @click="toggleJobStatus(scope.row)"
              >
                重新开放
              </el-button>
              
              <el-button type="danger" link @click="handleDelete(scope.row.id)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          background
          layout="total, sizes, prev, pager, next, jumper"
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
import { useRouter } from 'vue-router'
import { getEnterpriseJobs, updateJob, deleteJob } from '@/api/enterprise'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const jobs = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const statusFilter = ref('')

const fetchJobs = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    const res = await getEnterpriseJobs(params)
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

const toggleJobStatus = async (job) => {
  const newStatus = job.status === 'OPEN' ? 'CLOSED' : 'OPEN'
  const actionText = newStatus === 'OPEN' ? '重新开放' : '停止招聘'
  
  try {
    await updateJob(job.id, {
      ...job,
      status: newStatus
    })
    ElMessage.success(`${actionText}成功`)
    fetchJobs()
  } catch (error) {
    // Handled globally
  }
}

const handleDelete = (jobId) => {
  ElMessageBox.confirm(
    '确定要删除该职位吗？此操作不可恢复，相关的各类申请数据也将失效。',
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await deleteJob(jobId)
      ElMessage.success('删除成功')
      fetchJobs()
    } catch (error) {
      // Handled globally
    }
  }).catch(() => {})
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return dayjs(dateString).format('YYYY-MM-DD')
}

onMounted(() => {
  fetchJobs()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
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

.filter-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.filter-tab {
  padding: 6px 16px;
  border-radius: 4px;
  font-size: 14px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
  user-select: none;
}

.filter-tab:hover {
  color: var(--color-primary);
}

.filter-tab.active {
  background-color: rgba(0, 166, 167, 0.08);
  color: var(--color-primary);
  border-color: rgba(0, 166, 167, 0.2);
  font-weight: 500;
}

.job-title-cell {
  color: var(--color-text-primary);
  font-size: 15px;
}

.salary-text {
  color: var(--color-danger);
  font-weight: bold;
}

.data-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--color-text-secondary);
}

.highlight {
  color: var(--color-accent);
  font-weight: 500;
}

.text-muted {
  color: var(--color-text-muted);
  font-size: 13px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
}
</style>
