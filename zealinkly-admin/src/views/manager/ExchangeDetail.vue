<template>
  <div class="exchange-detail-container">
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <span>兑换详情</span>
          <div>
            <el-button size="small" @click="$router.back()">返回</el-button>
          </div>
        </div>
      </template>
      
      <div v-loading="loading" class="detail-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form label-width="120px" :model="exchange" class="detail-form">
              <el-form-item label="兑换ID">
                <span>{{ exchange.id }}</span>
              </el-form-item>
              <el-form-item label="老人姓名">
                <span>{{ exchange.elderName }}</span>
              </el-form-item>
              <el-form-item label="老人电话">
                <span>{{ exchange.elderPhone }}</span>
              </el-form-item>
              <el-form-item label="老人地址">
                <span>{{ exchange.elderAddress }}</span>
              </el-form-item>
              <el-form-item label="商品名称">
                <span>{{ exchange.productName }}</span>
              </el-form-item>
              <el-form-item label="兑换数量">
                <span>{{ exchange.quantity }}</span>
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :span="12">
            <el-form label-width="120px" :model="exchange" class="detail-form">
              <el-form-item label="商品价格">
                <span>{{ exchange.productPrice }} 积分</span>
              </el-form-item>
              <el-form-item label="总积分">
                <span>{{ exchange.totalPoints }} 积分</span>
              </el-form-item>
              <el-form-item label="状态">
                <el-tag :type="getStatusType(exchange.status)">
                  {{ getStatusText(exchange.status) }}
                </el-tag>
              </el-form-item>
              <el-form-item label="申请时间">
                <span>{{ exchange.createdAt }}</span>
              </el-form-item>
              <el-form-item label="处理时间">
                <span>{{ exchange.processedAt || '-' }}</span>
              </el-form-item>
              <el-form-item label="处理人">
                <span>{{ exchange.processorName || '-' }}</span>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
        
        <!-- 兑换操作 -->
        <el-card class="action-card" v-if="exchange.status === 'PENDING'">
          <template #header>
            <span>兑换处理</span>
          </template>
          <div class="action-buttons">
            <el-button type="primary" @click="approveExchange" :loading="actionLoading">
              通过申请
            </el-button>
            <el-button type="danger" @click="rejectExchange" :loading="actionLoading">
              拒绝申请
            </el-button>
          </div>
        </el-card>
        
        <!-- 兑换备注 -->
        <el-card class="note-card" v-if="exchange.notes">
          <template #header>
            <span>兑换备注</span>
          </template>
          <div class="note-content">
            <p>{{ exchange.notes }}</p>
          </div>
        </el-card>
        
        <!-- 处理备注 -->
        <el-card class="note-card" v-if="exchange.processingNotes">
          <template #header>
            <span>处理备注</span>
          </template>
          <div class="note-content">
            <p>{{ exchange.processingNotes }}</p>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const actionLoading = ref(false)

const exchangeId = computed(() => route.params.id)

const exchange = reactive({
  id: '',
  elderName: '',
  elderPhone: '',
  elderAddress: '',
  productName: '',
  quantity: 0,
  productPrice: 0,
  totalPoints: 0,
  status: '',
  notes: '',
  processingNotes: '',
  createdAt: '',
  processedAt: '',
  processorName: ''
})

const getExchangeDetail = async () => {
  if (!exchangeId.value) {
    ElMessage.error('兑换ID不存在')
    router.push('/manager/exchanges')
    return
  }
  
  try {
    loading.value = true
    const response = await fetch(`/api/admin/exchanges/${exchangeId.value}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
      Object.assign(exchange, data.data)
    } else {
      ElMessage.error(data.message || '获取兑换信息失败')
      router.push('/manager/exchanges')
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
    router.push('/manager/exchanges')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const statusMap = {
    PENDING: 'warning',
    APPROVED: 'primary',
    REJECTED: 'danger',
    COMPLETED: 'success'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    COMPLETED: '已完成'
  }
  return statusMap[status] || status
}

const approveExchange = async () => {
  try {
    actionLoading.value = true
    await ElMessageBox.confirm('确定要通过这个兑换申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    const response = await fetch(`/api/admin/exchanges/${exchangeId.value}/approve`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('兑换申请已通过')
      getExchangeDetail()
    } else {
      ElMessage.error(data.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试')
    }
  } finally {
    actionLoading.value = false
  }
}

const rejectExchange = async () => {
  try {
    actionLoading.value = true
    await ElMessageBox.confirm('确定要拒绝这个兑换申请吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await fetch(`/api/admin/exchanges/${exchangeId.value}/reject`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('兑换申请已拒绝')
      getExchangeDetail()
    } else {
      ElMessage.error(data.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试')
    }
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  getExchangeDetail()
})
</script>

<style scoped>
.exchange-detail-container {
  padding: 20px 0;
}

.detail-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-content {
  padding: 20px 0;
}

.detail-form {
  margin-bottom: 20px;
}

.detail-form .el-form-item {
  margin-bottom: 15px;
}

.action-card {
  margin-top: 30px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.note-card {
  margin-top: 30px;
}

.note-content {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  white-space: pre-wrap;
}

.note-content p {
  margin: 0;
  line-height: 1.6;
}
</style>