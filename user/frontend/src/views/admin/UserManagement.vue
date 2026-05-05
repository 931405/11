<template>
  <div class="page">
    <div class="panel">
      <div class="header">
        <div>
          <h1>用户管理</h1>
          <p>支持按角色筛选用户，并启用或禁用账号。</p>
        </div>
        <div class="actions">
          <el-select v-model="role" placeholder="按角色筛选" style="width: 180px" @change="handleSearch">
            <el-option label="全部角色" value="" />
            <el-option label="学生" value="STUDENT" />
            <el-option label="企业" value="ENTERPRISE" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
          <el-button @click="router.push('/admin/dashboard')">返回看板</el-button>
        </div>
      </div>

      <el-table :data="users" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="username" label="用户名" min-width="160" />
        <el-table-column prop="realName" label="姓名" min-width="140" />
        <el-table-column prop="role" label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag>{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="150" />
        <el-table-column prop="email" label="邮箱" min-width="220" />
        <el-table-column label="状态" width="180" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              :disabled="row.role === 'ADMIN'"
              @change="(value) => handleToggle(row, value)"
            />
            <span class="status-text">{{ row.status === 1 ? '正常' : '禁用' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserList, updateUserStatus } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const users = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const role = ref('')

const roleText = (value) => {
  if (value === 'STUDENT') return '学生'
  if (value === 'ENTERPRISE') return '企业'
  if (value === 'ADMIN') return '管理员'
  return value || '未知'
}

const loadUsers = async () => {
  loading.value = true
  try {
    const data = await getUserList({
      role: role.value || undefined,
      page: page.value,
      size: size.value
    })
    users.value = data.content || []
    total.value = data.totalElements || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadUsers()
}

const handleToggle = async (row, enabled) => {
  const oldValue = row.status
  row.status = enabled ? 1 : 0
  try {
    await updateUserStatus(row.id, row.status)
    ElMessage.success(`用户 ${row.username} 状态已更新`)
  } catch (error) {
    row.status = oldValue
  }
}

onMounted(() => {
  loadUsers()
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

.status-text {
  margin-left: 8px;
  color: var(--color-text-secondary);
  font-size: 13px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
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
