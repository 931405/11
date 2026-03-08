<template>
  <div class="enterprise-audit-container">
    <div class="page-title">
      <h2>企业资质审核</h2>
      <p class="subtitle">审核企业提交的认证资料，决定是否开放其发布职位的权限</p>
    </div>

    <!-- Alert for uncertified enterprises -->
    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="enterprises"
        style="width: 100%"
        :header-cell-style="{ background: 'var(--color-bg-secondary)', color: 'var(--color-text-primary)' }"
      >
        <el-table-column label="提交时间" width="160">
          <template #default="scope">
            <span class="text-muted">{{ formatDate(scope.row.updatedAt || scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="企业全称" prop="companyName" min-width="180">
          <template #default="scope">
            <strong>{{ scope.row.companyName }}</strong>
          </template>
        </el-table-column>
        
        <el-table-column label="统一社会信用代码" prop="businessLicenseNo" width="180" />

        <el-table-column label="联系人/电话" min-width="150">
          <template #default="scope">
            <div>{{ scope.row.contactPerson }}</div>
            <div class="text-muted">{{ scope.row.contactPhone }}</div>
          </template>
        </el-table-column>

        <el-table-column label="当前状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.certificationStatus)" effect="light">
              {{ getStatusLabel(scope.row.certificationStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button 
              type="primary" 
              link 
              @click="openAuditDialog(scope.row)"
            >
              审查详情 & 审核
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

    <!-- 审核弹窗 -->
    <el-dialog v-model="dialogVisible" title="企业资质详情审查" width="600px">
      <div v-if="currentAudit" class="audit-details">
        <el-descriptions title="企业基本信息" :column="2" border>
          <el-descriptions-item label="企业名称" :span="2">{{ currentAudit.companyName }}</el-descriptions-item>
          <el-descriptions-item label="信用代码" :span="2">{{ currentAudit.businessLicenseNo }}</el-descriptions-item>
          <el-descriptions-item label="所属行业">{{ currentAudit.industry || '-' }}</el-descriptions-item>
          <el-descriptions-item label="人员规模">{{ currentAudit.companySize || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentAudit.contactPerson }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentAudit.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="办公地址" :span="2">{{ currentAudit.address || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="license-section mt-24">
          <h4>营业执照扫描件</h4>
          <div class="license-img-wrapper">
            <el-image 
              style="width: 100%; max-height: 300px"
              :src="currentAudit.licenseUrl"
              :preview-src-list="[currentAudit.licenseUrl]"
              fit="contain"
            >
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon> <span>未上传或加载失败</span>
                </div>
              </template>
            </el-image>
          </div>
        </div>

        <el-form label-position="top" class="mt-24" v-if="currentAudit.certificationStatus === 'PENDING'">
          <el-form-item label="审核结果">
            <el-radio-group v-model="auditForm.status">
              <el-radio label="APPROVED">通过认真与开放权限</el-radio>
              <el-radio label="REJECTED">驳回并要求重新修改</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="审核备注说明 (驳回时必填)" :required="auditForm.status === 'REJECTED'">
            <el-input 
              v-model="auditForm.remark" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入通过或驳回的详细说明..." 
            />
          </el-form-item>
        </el-form>
        
        <!-- Only show past remarks if not pending -->
        <div v-else class="mt-24 past-remark">
           <el-alert
             :title="`过去审核记录: ${getStatusLabel(currentAudit.certificationStatus)}`"
             :type="currentAudit.certificationStatus === 'APPROVED' ? 'success' : 'error'"
             :description="currentAudit.auditRemark || '无备注记录'"
             :closable="false"
             show-icon
           />
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button 
            v-if="currentAudit?.certificationStatus === 'PENDING'"
            type="primary" 
            :loading="submitLoading" 
            @click="submitAudit"
          >
            确认提交审核
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPendingAudits, auditEnterprise } from '@/api/admin'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)
const enterprises = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const dialogVisible = ref(false)
const currentAudit = ref(null)
const submitLoading = ref(false)

const auditForm = ref({
  status: 'APPROVED',
  remark: ''
})

const fetchAudits = async () => {
  loading.value = true
  try {
    const res = await getPendingAudits({ page: page.value, size: size.value })
    enterprises.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to load audits', error)
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  size.value = val
  fetchAudits()
}

const handleCurrentChange = (val) => {
  page.value = val
  fetchAudits()
}

const openAuditDialog = (row) => {
  currentAudit.value = row
  auditForm.value.status = 'APPROVED'
  auditForm.value.remark = ''
  dialogVisible.value = true
}

const submitAudit = async () => {
  if (auditForm.value.status === 'REJECTED' && !auditForm.value.remark) {
    ElMessage.warning('驳回时必须填写备注说明')
    return
  }

  submitLoading.value = true
  try {
    await auditEnterprise(currentAudit.value.id, auditForm.value.status, auditForm.value.remark)
    ElMessage.success('审核操作成功')
    dialogVisible.value = false
    fetchAudits()
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

const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = {
    'PENDING': '待审核',
    'APPROVED': '已认证',
    'REJECTED': '已驳回'
  }
  return map[status] || '未定'
}

onMounted(() => {
  fetchAudits()
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

.text-muted {
  color: var(--color-text-muted);
  font-size: 13px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
}

.mt-24 {
  margin-top: 24px;
}

.license-img-wrapper {
  border: 1px dashed var(--color-border);
  border-radius: 4px;
  padding: 8px;
  background: var(--color-bg-secondary);
}

.image-slot {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: var(--color-text-muted);
  background: var(--color-bg-primary);
}

.image-slot .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}
</style>
