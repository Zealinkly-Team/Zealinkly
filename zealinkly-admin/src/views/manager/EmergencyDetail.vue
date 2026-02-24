<template>
  <div class="emergency-detail-container">
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <span>紧急报警详情</span>
          <div>
            <el-button size="small" @click="$router.back()">返回</el-button>
          </div>
        </div>
      </template>
      
      <div v-loading="loading" class="detail-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form label-width="120px" :model="emergency" class="detail-form">
              <el-form-item label="报警ID">
                <span>{{ emergency.id }}</span>
              </el-form-item>
              <el-form-item label="老人姓名">
                <span>{{ emergency.elderName }}</span>
              </el-form-item>
              <el-form-item label="老人电话">
                <span>{{ emergency.elderPhone }}</span>
              </el-form-item>
              <el-form-item label="老人地址">
                <span>{{ emergency.elderAddress }}</span>
              </el-form-item>
              <el-form-item label="报警类型">
                <el-tag type="danger">{{ getTypeText(emergency.type) }}</el-tag>
              </el-form-item>
              <el-form-item label="状态">
                <el-tag :type="getStatusType(emergency.status)">
                  {{ getStatusText(emergency.status) }}
                </el-tag>
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :span="12">
            <el-form label-width="120px" :model="emergency" class="detail-form">
              <el-form-item label="位置">
                <span>
                  {{ emergency.latitude || '-' }}, {{ emergency.longitude || '-' }}
                </span>
              </el-form-item>
              <el-form-item label="报警时间">
                <span>{{ emergency.createdAt }}</span>
              </el-form-item>
              <el-form-item label="开始处理时间">
                <span>{{ emergency.processingAt || '-' }}</span>
              </el-form-item>
              <el-form-item label="处理完成时间">
                <span>{{ emergency.processedAt || '-' }}</span>
              </el-form-item>
              <el-form-item label="处理人">
                <span>{{ emergency.processorName || '-' }}</span>
              </el-form-item>
              <el-form-item label="处理人电话">
                <span>{{ emergency.processorPhone || '-' }}</span>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
        
        <!-- 报警操作 -->
        <el-card class="action-card" v-if="canTakeAction(emergency.status)">
          <template #header>
            <span>报警处理</span>
          </template>
          <div class="action-buttons">
            <el-button type="primary" @click="handleEmergencyAction" :loading="actionLoading">
              {{ getActionText(emergency.status) }}
            </el-button>
          </div>
        </el-card>
        
        <!-- 报警备注 -->
        <el-card class="note-card" v-if="emergency.notes">
          <template #header>
            <span>报警备注</span>
          </template>
          <div class="note-content">
            <p>{{ emergency.notes }}</p>
          </div>
        </el-card>
        
        <!-- 处理记录 -->
        <el-card class="record-card" v-if="emergency.processingNotes">
          <template #header>
            <span>处理记录</span>
          </template>
          <div class="record-content">
            <p>{{ emergency.processingNotes }}</p>
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
import { emergencyAPI, api, API_BASE_URL } from '@/utils/api'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const actionLoading = ref(false)

const emergencyId = computed(() => route.params.id)

const emergency = reactive({
  id: '',
  elderName: '',
  elderPhone: '',
  elderAddress: '',
  type: '',
  status: '',
  latitude: '',
  longitude: '',
  notes: '',
  processingNotes: '',
  createdAt: '',
  processingAt: '',
  processedAt: '',
  processorName: '',
  processorPhone: ''
})

const getEmergencyDetail = async () => {
  console.log('=== 触发获取紧急报警详情 ===')
  console.log('route.params:', route.params)
  console.log('emergencyId.value:', emergencyId.value)
  console.log('emergencyId.value 类型:', typeof emergencyId.value)
  
  if (!emergencyId.value) {
    ElMessage.error('报警ID不存在')
    router.push('/manager/emergency')
    return
  }
  
  try {
    loading.value = true
    console.log('=== 获取紧急报警详情开始 ===')
    console.log('报警ID:', emergencyId.value)
    
    // 调用后端API获取报警详情，确保id是数字类型
    const id = parseInt(emergencyId.value)
    if (isNaN(id)) {
      ElMessage.error('无效的报警ID')
      router.push('/manager/emergency')
      return
    }
    console.log('调用API路径:', `/emergency/${id}/detail`)
    console.log('完整请求路径:', `${API_BASE_URL}/emergency/${id}/detail`)
    const response = await api.get(`/emergency/${id}/detail`)
    console.log('API响应:', response)
    
    if (response.code === 200) {
      const data = response.data
      console.log('响应数据:', data)
      
      // 更新emergency对象
      emergency.id = data.task?.id || emergencyId.value
      emergency.elderName = data.elderInfo?.realName || '未知'
      emergency.elderPhone = data.elderInfo?.phone || '未知'
      emergency.elderAddress = data.elderInfo?.address || '未知'
      emergency.type = 'EMERGENCY' // 默认为紧急求助类型
      emergency.status = data.task?.status || 'UNKNOWN'
      emergency.latitude = data.location?.lat || ''
      emergency.longitude = data.location?.lng || ''
      emergency.notes = ''
      emergency.processingNotes = data.task?.aiResponse || ''
      emergency.createdAt = data.task?.createdAt || ''
      emergency.processingAt = ''
      emergency.processedAt = ''
      emergency.processorName = data.task?.adminName || ''
      emergency.processorPhone = ''
      
      console.log('=== 处理完成 ===')
      console.log('处理后的紧急报警详情:', emergency)
    } else {
      ElMessage.error(`获取紧急报警详情失败: ${response.message || '未知错误'}`)
      // 使用默认数据确保页面正常显示
      emergency.id = emergencyId.value
      emergency.elderName = '未知'
      emergency.elderPhone = '未知'
      emergency.elderAddress = '未知'
      emergency.type = 'EMERGENCY'
      emergency.status = 'UNKNOWN'
      emergency.latitude = ''
      emergency.longitude = ''
      emergency.notes = ''
      emergency.processingNotes = ''
      emergency.createdAt = ''
      emergency.processingAt = ''
      emergency.processedAt = ''
      emergency.processorName = ''
      emergency.processorPhone = ''
    }
  } catch (error) {
    console.error('=== 获取紧急报警详情失败 ===')
    console.error('错误对象:', error)
    console.error('错误消息:', error.message)
    console.error('错误响应:', error.response)
    console.error('错误响应数据:', error.response?.data)
    console.error('错误配置:', error.config)
    console.error('错误配置URL:', error.config?.url)
    
    // 显示友好的提示信息
    ElMessage.info('报警记录不存在或已被删除')
    
    // 使用默认数据确保页面正常显示
    emergency.id = emergencyId.value
    emergency.elderName = '未知'
    emergency.elderPhone = '未知'
    emergency.elderAddress = '未知'
    emergency.type = 'EMERGENCY'
    emergency.status = 'UNKNOWN'
    emergency.latitude = ''
    emergency.longitude = ''
    emergency.notes = ''
    emergency.processingNotes = ''
    emergency.createdAt = ''
    emergency.processingAt = ''
    emergency.processedAt = ''
    emergency.processorName = ''
    emergency.processorPhone = ''
  } finally {
    loading.value = false
    console.log('=== 获取紧急报警详情结束 ===')
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
    IN_PROGRESS: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    PENDING: '待处理',
    IN_PROGRESS: '处理中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return statusMap[status] || status
}

const canTakeAction = (status) => {
  // 在状态为PENDING或IN_PROGRESS时显示操作按钮
  return status === 'PENDING' || status === 'IN_PROGRESS'
}

const getActionText = (status) => {
  if (status === 'PENDING') {
    return '开始处理'
  } else if (status === 'IN_PROGRESS') {
    return '完成处理'
  }
  return '操作'
}

const handleEmergencyAction = async () => {
  try {
    actionLoading.value = true
    
    // 确保id是数字类型
    const id = parseInt(emergencyId.value)
    if (isNaN(id)) {
      ElMessage.error('无效的报警ID')
      router.push('/manager/emergency')
      return
    }
    
    if (emergency.status === 'PENDING') {
      // 开始处理报警
      await api.patch(`/emergency/${id}/start`)
      ElMessage.success('开始处理成功')
      // 重新获取详情以更新状态
      getEmergencyDetail()
      // 触发仪表盘更新
      window.dispatchEvent(new CustomEvent('refreshDashboard'))
    } else if (emergency.status === 'IN_PROGRESS') {
      // 完成处理报警
      // 弹出对话框让管理员输入处理备注
      ElMessageBox.prompt('请输入处理备注', '完成处理', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入处理情况...'
      }).then(async ({ value }) => {
        await api.patch(`/emergency/${id}/complete`, { note: value })
        ElMessage.success('处理成功')
        // 重新获取详情以更新状态
        getEmergencyDetail()
        // 触发仪表盘更新
        window.dispatchEvent(new CustomEvent('refreshDashboard'))
      }).catch(() => {
        // 取消操作
      })
    }
  } catch (error) {
    // 显示友好的错误信息
    let errorMessage = '处理紧急报警失败'
    if (error.response) {
      errorMessage = `${errorMessage}: ${error.response.data?.message || `服务器错误 (${error.response.status})`}`
    } else if (error.request) {
      errorMessage = `${errorMessage}: 服务器无响应，请检查网络连接`
    } else {
      errorMessage = `${errorMessage}: ${error.message}`
    }
    ElMessage.error(errorMessage)
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  getEmergencyDetail()
})
</script>

<style scoped>
.emergency-detail-container {
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

.note-card,
.record-card {
  margin-top: 30px;
}

.note-content,
.record-content {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  white-space: pre-wrap;
}

.note-content p,
.record-content p {
  margin: 0;
  line-height: 1.6;
}
</style>