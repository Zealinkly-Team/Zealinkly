<template>
  <div class="emergency-list-container">
    <div class="page-header">
      <h3>紧急报警管理</h3>
    </div>
    
    <!-- 报警列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>报警列表</span>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="emergencies"
        style="width: 100%"
      >
        <el-table-column prop="id" label="报警ID" width="80" />
        <el-table-column prop="content" label="报警内容" />
        <el-table-column prop="elderName" label="老人姓名" width="120" />
        <el-table-column prop="elderPhone" label="老人电话" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="报警时间" width="180" />
        <el-table-column label="操作" width="80">
          <template #default="scope">
            <el-button size="small" @click="viewEmergency(scope.row.id)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { emergencyAPI } from '@/utils/api'

const router = useRouter()
const loading = ref(false)
const emergencies = ref([])
const total = ref(0)

const getEmergencies = async () => {
  loading.value = true
  try {
    const response = await emergencyAPI.getPendingList()
    if (response.code === 200) {
      emergencies.value = response.data || []
      total.value = emergencies.value.length
    }
  } catch (error) {
    // 错误已在API拦截器中处理
  } finally {
    loading.value = false
  }
}

const getTypeText = (type) => {
  const typeMap = {
    FALL: '跌倒',
    HEART_RATE: '心率异常',
    BLOOD_PRESSURE: '血压异常',
    EMERGENCY: '紧急求助'
  }
  return typeMap[type] || type
}

const getStatusType = (status) => {
  const statusMap = {
    PENDING: 'danger',
    PROCESSING: 'warning',
    PROCESSED: 'primary',
    CLOSED: 'success'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    PROCESSED: '已处理',
    CLOSED: '已关闭'
  }
  return statusMap[status] || status
}

const viewEmergency = (id) => {
  router.push(`/manager/emergency/${id}`)
}

onMounted(() => {
  getEmergencies()
})
</script>

<style scoped>
.emergency-list-container {
  padding: 20px 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.list-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}
</style>