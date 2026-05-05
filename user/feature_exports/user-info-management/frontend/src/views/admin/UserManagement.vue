<template>
  <div class="user-manage-container">
    <div class="page-title">
      <h2>用户管理</h2>
      <p class="subtitle">管理平台所有用户账号，限制异常用户登录</p>
    </div>

    <el-card shadow="never" class="table-card">
      <!-- 过滤栏 -->
      <div class="filter-bar">
        <el-radio-group v-model="roleFilter" @change="handleFilterChange">
          <el-radio-button label="">全部用户</el-radio-button>
          <el-radio-button label="STUDENT">学生</el-radio-button>
          <el-radio-button label="ENTERPRISE">企业账号</el-radio-button>
        </el-radio-group>
      </div>

      <el-table
        v-loading="loading"
        :data="users"
        style="width: 100%"
        :header-cell-style="{ background: 'var(--color-bg-secondary)', color: 'var(--color-text-primary)' }"
      >
        <el-table-column label="注册时间" width="160">
          <template #default="scope">
            <span class="text-muted">{{ formatDate(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="用户名" prop="username" min-width="120">
          <template #default="scope">
            <strong>{{ scope.row.username }}</strong>
          </template>
        </el-table-column>
        
        <el-table-column label="角色角色" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)" effect="plain">
              {{ getRoleLabel(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="账号状态" width="120" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              active-color="var(--color-success)"
              inactive-color="var(--color-danger)"
              @change="toggleUserStatus(scope.row)"
              :disabled="scope.row.role === 'ADMIN'"
            />
            <span :class="scope.row.status === 1 ? 'text-success' : 'text-danger'" class="ml-2">
              {{ scope.row.status === 1 ? '正常' : '封禁' }}
            </span>
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
import { getUserList, updateUserStatus } from '@/api/admin'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const roleFilter = ref('')

const fetchUsers = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (roleFilter.value) {
      params.role = roleFilter.value
    }
    const res = await getUserList(params)
    users.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to load users', error)
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  page.value = 1
  fetchUsers()
}

const handleSizeChange = (val) => {
  size.value = val
  fetchUsers()
}

const handleCurrentChange = (val) => {
  page.value = val
  fetchUsers()
}

const toggleUserStatus = async (user) => {
  try {
    // Make the API call
    await updateUserStatus(user.id, user.status)
    ElMessage.success(`用户 [${user.username}] 状态已${user.status === 1 ? '启用' : '禁用'}`)
  } catch (error) {
    // Revert switch on failure
    user.status = user.status === 1 ? 0 : 1
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return dayjs(dateString).format('YYYY-MM-DD HH:mm')
}

const getRoleType = (role) => {
  const map = {
    'STUDENT': 'success',
    'ENTERPRISE': 'warning',
    'ADMIN': 'danger'
  }
  return map[role] || 'info'
}

const getRoleLabel = (role) => {
  const map = {
    'STUDENT': '学生',
    'ENTERPRISE': '企业',
    'ADMIN': '管理员'
  }
  return map[role] || role
}

onMounted(() => {
  fetchUsers()
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

.ml-2 {
  margin-left: 8px;
  font-size: 13px;
}

.text-success { color: var(--color-success); }
.text-danger { color: var(--color-danger); }

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
}
</style>
