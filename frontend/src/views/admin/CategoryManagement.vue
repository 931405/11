<template>
  <div class="category-manage-container">
    <div class="page-title">
      <h2>职位分类管理</h2>
      <p class="subtitle">维护系统的兼职岗位类型字典，支持层级展示</p>
    </div>

    <el-card shadow="never" class="table-card">
      <div class="toolbar">
        <el-button type="primary" @click="openDialog(null, null)">
          <el-icon><Plus /></el-icon> 新增分类
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="categories"
        style="width: 100%"
        row-key="id"
        border
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :header-cell-style="{ background: 'var(--color-bg-secondary)', color: 'var(--color-text-primary)' }"
      >
        <el-table-column label="分类名称" prop="name" min-width="180">
          <template #default="scope">
            <strong>{{ scope.row.name }}</strong>
          </template>
        </el-table-column>
        
        <el-table-column label="排序权重" prop="sortOrder" width="120" align="center" />
        
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button 
                v-if="!scope.row.parentId"
                type="primary" 
                link 
                size="small"
                @click="openDialog(null, scope.row.id)"
              >
                添加子类
              </el-button>
              
              <el-button 
                type="info" 
                link 
                size="small"
                @click="openDialog(scope.row, null)"
              >
                编辑
              </el-button>
              
              <el-button 
                type="danger" 
                link 
                size="small"
                @click="handleDelete(scope.row)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Modal Form -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="400px">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        
        <el-form-item label="上级分类" v-if="!isEdit && form.parentId">
          <!-- Cannot change parent when editing for simplicity, display only if adding child -->
          <el-select v-model="form.parentId" disabled class="w-100">
            <el-option :value="form.parentId" :label="getParentName(form.parentId)" />
          </el-select>
        </el-form-item>

        <el-form-item label="排序权重" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="1" :max="999" class="w-100" />
          <div class="helper-text">数字越小越靠前</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            确认保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCategories } from '@/api/job'
import { createCategory, updateCategory, deleteCategory } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const categories = ref([])

const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const formRef = ref(null)

const form = reactive({
  name: '',
  parentId: null,
  sortOrder: 1,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const fetchCategories = async () => {
  loading.value = true
  try {
    categories.value = await getCategories()
  } catch (error) {
    console.error('Failed to load categories', error)
  } finally {
    loading.value = false
  }
}

// Helper to display parent name in form
const getParentName = (parentId) => {
  const parent = categories.value.find(c => c.id === parentId)
  return parent ? parent.name : '未知'
}

const openDialog = (row = null, parentId = null) => {
  if (formRef.value) formRef.value.clearValidate()
  
  if (row) {
    // Edit existing
    isEdit.value = true
    currentId.value = row.id
    form.name = row.name
    form.parentId = row.parentId
    form.sortOrder = row.sortOrder
    form.status = row.status
  } else {
    // Add new (either root or child)
    isEdit.value = false
    currentId.value = null
    form.name = ''
    form.parentId = parentId
    form.sortOrder = 1
    form.status = 1
  }
  
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateCategory(currentId.value, { 
            name: form.name, 
            sortOrder: form.sortOrder,
            status: form.status
          })
          ElMessage.success('修改成功')
        } else {
          await createCategory({
            name: form.name,
            parentId: form.parentId,
            sortOrder: form.sortOrder
          })
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        fetchCategories()
      } catch (error) {
        // Error handled globally
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = (row) => {
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该分类下包含子分类，无法直接删除')
    return
  }

  ElMessageBox.confirm(
    `确定要删除分类 "${row.name}" 吗？这可能导致与此相关的职位失去分类索引！`,
    '高危操作警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error',
    }
  ).then(async () => {
    try {
      await deleteCategory(row.id)
      ElMessage.success('删除成功')
      fetchCategories()
    } catch (error) {
      // Handled globally
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchCategories()
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

.toolbar {
  margin-bottom: 16px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.w-100 {
  width: 100%;
}

.helper-text {
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.4;
  margin-top: 4px;
}
</style>
