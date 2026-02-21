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
import { emergencyAPI, api } from '@/utils/api'

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
  if (!emergencyId.value) {
    ElMessage.error('报警ID不存在')
    router.push('/manager/emergency')
    return
  }
  
  try {
    loading.value = true
    const response = await emergencyAPI.getDetail(emergencyId.value)
    if (response.code === 200) {
      const data = response.data
      // 处理后端返回的数据结构
      emergency.id = data.task.id
      emergency.elderName = data.elderInfo.realName
      emergency.elderPhone = data.elderInfo.phone
      emergency.elderAddress = data.elderInfo.address
      emergency.type = data.task.taskType || 'EMERGENCY'
      emergency.status = data.task.status
      emergency.latitude = data.location.lat
      emergency.longitude = data.location.lng
      emergency.notes = data.task.aiResponse || ''
      emergency.processingNotes = data.task.aiResponse || ''
      emergency.createdAt = data.task.createdAt
      emergency.processingAt = data.task.processingAt || ''
      emergency.processedAt = data.task.completedAt || ''
      emergency.processorName = data.task.adminName || ''
      emergency.processorPhone = ''
    }
  } catch (error) {
    // 错误已在API拦截器中处理
    router.push('/manager/emergency')
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

const canTakeAction = (status) => {
  // 只在状态为PENDING时显示操作按钮，因为后端会直接将状态转换为COMPLETED
  return status === 'PENDING'
}

const getActionText = (status) => {
  // 只显示"开始处理"按钮，因为后端会直接将状态转换为COMPLETED
  return '开始处理'
}

const handleEmergencyAction = async () => {
  try {
    actionLoading.value = true
    
    // 处理紧急报警
    await emergencyAPI.handle(emergencyId.value, { note: '' })
    
    // 显示成功消息
    ElMessage.success('处理成功')
    
    // 重新获取详情，更新状态
    getEmergencyDetail()
  } catch (error) {
    // 错误已在API拦截器中处理
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