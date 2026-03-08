<template>
  <div class="system-logs">
    <el-card shadow="never" class="logs-card">
      <template #header>
        <div class="card-header">
          <el-icon><DataLine /></el-icon>
          <span>系统运行日志</span>
        </div>
      </template>

      <div class="toolbar">
        <el-input
          v-model="searchAction"
          placeholder="按操作行为搜索"
          clearable
          @keyup.enter="handleSearch"
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="logList"
        style="width: 100%"
        border
        stripe
        size="large"
      >
        <el-table-column prop="id" label="日志 ID" width="80" align="center" />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="action" label="操作行为" min-width="150" />
        <el-table-column prop="method" label="后端方法" min-width="250" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP 地址" width="140" />
        <el-table-column prop="timeCost" label="耗时(ms)" width="100" align="right">
          <template #default="scope">
            <span :class="scope.row.timeCost > 1000 ? 'text-warning' : ''">
              {{ scope.row.timeCost }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="操作时间" width="180" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="viewDetails(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="日志详情" width="600px">
      <div v-if="selectedLog" class="log-detail-content">
        <div class="detail-item">
          <span class="detail-label">操作人:</span>
          <span class="detail-value">{{ selectedLog.username || '匿名' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">操作行为:</span>
          <span class="detail-value">{{ selectedLog.action }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">后端方法:</span>
          <span class="detail-value">{{ selectedLog.method }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">IP 地址:</span>
          <span class="detail-value">{{ selectedLog.ip }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">操作时间:</span>
          <span class="detail-value">{{ selectedLog.createdAt }}</span>
        </div>
        
        <el-divider />
        
        <div class="detail-section">
          <h4>请求参数</h4>
          <pre class="code-block">{{ formatJson(selectedLog.params) }}</pre>
        </div>

        <div v-if="selectedLog.status === 0 && selectedLog.errorMsg" class="detail-section error-section">
          <h4>错误信息</h4>
          <pre class="code-block error-block">{{ selectedLog.errorMsg }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSystemLogs } from '@/api/admin'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const logList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchAction = ref('')

const dialogVisible = ref(false)
const selectedLog = ref(null)

const loadLogs = async () => {
  loading.value = true
  try {
    const res = await getSystemLogs({
      page: currentPage.value,
      size: pageSize.value,
      action: searchAction.value || undefined
    })
    logList.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to load logs', error)
    ElMessage.error('加载日志列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadLogs()
}

const handleReset = () => {
  searchAction.value = ''
  currentPage.value = 1
  loadLogs()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadLogs()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadLogs()
}

const viewDetails = (row) => {
  selectedLog.value = row
  dialogVisible.value = true
}

const formatJson = (str) => {
  if (!str) return '无'
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch (e) {
    return str
  }
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.system-logs {
  display: flex;
  flex-direction: column;
}

.logs-card {
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

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.text-warning {
  color: var(--color-warning);
}

.log-detail-content {
  color: var(--color-text-primary);
  font-size: 14px;
}

.detail-item {
  display: flex;
  margin-bottom: 12px;
}

.detail-label {
  width: 100px;
  color: var(--color-text-muted);
}

.detail-value {
  flex: 1;
  word-break: break-all;
}

.detail-section {
  margin-top: 16px;
}

.detail-section h4 {
  margin-top: 0;
  margin-bottom: 8px;
  color: var(--color-text-primary);
  font-weight: 600;
}

.code-block {
  background-color: var(--color-bg-secondary);
  padding: 12px;
  border-radius: 6px;
  font-family: monospace;
  font-size: 13px;
  color: var(--color-text-secondary);
  white-space: pre-wrap;
  word-break: break-all;
  border: 1px solid var(--color-border);
}

.error-block {
  color: var(--color-danger);
  background-color: rgba(var(--color-danger-rgb), 0.05);
  border-color: rgba(var(--color-danger-rgb), 0.2);
}
</style>
