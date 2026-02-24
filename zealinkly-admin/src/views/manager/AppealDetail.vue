<template>
  <div class="appeal-detail-container">
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <span>申诉详情</span>
          <div>
            <el-button size="small" @click="$router.back()">返回</el-button>
          </div>
        </div>
      </template>
      
      <div v-loading="loading" class="detail-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form label-width="120px" :model="appeal" class="detail-form">
              <el-form-item label="申诉ID">
                <span>{{ appeal.id }}</span>
              </el-form-item>
              <el-form-item label="申诉人姓名">
                <span>{{ appeal.appellantName }}</span>
              </el-form-item>
              <el-form-item label="申诉人类型">
                <span>{{ appeal.appellantType === 'ELDER' ? '老人' : '志愿者' }}</span>
              </el-form-item>
              <el-form-item label="申诉人电话">
                <span>{{ appeal.appellantPhone }}</span>
              </el-form-item>
              <el-form-item label="申诉类型">
                <span>{{ getTypeText(appeal.type) }}</span>
              </el-form-item>
              <el-form-item label="申诉标题">
                <span>{{ appeal.title }}</span>
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :span="12">
            <el-form label-width="120px" :model="appeal" class="detail-form">
              <el-form-item label="状态">
                <el-tag :type="getStatusType(appeal.status)">
                  {{ getStatusText(appeal.status) }}
                </el-tag>
              </el-form-item>
              <el-form-item label="申诉时间">
                <span>{{ appeal.createdAt }}</span>
              </el-form-item>
              <el-form-item label="开始处理时间">
                <span>{{ appeal.processingAt || '-' }}</span>
              </el-form-item>
              <el-form-item label="处理完成时间">
                <span>{{ appeal.resolvedAt || '-' }}</span>
              </el-form-item>
              <el-form-item label="处理人">
                <span>{{ appeal.processorName || '-' }}</span>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
        
        <!-- 申诉内容 -->
        <el-card class="content-card">
          <template #header>
            <span>申诉内容</span>
          </template>
          <div class="content-text">
            <p>{{ appeal.content }}</p>
          </div>
        </el-card>
        
        <!-- 处理操作 -->
        <el-card class="action-card" v-if="appeal.status === 'PENDING'">
          <template #header>
            <span>申诉处理</span>
          </template>
          <div class="action-buttons">
            <el-button type="primary" @click="processAppeal">开始处理</el-button>
          </div>
        </el-card>
        
        <!-- 处理结果 -->
        <el-card class="result-card" v-if="appeal.processingResult">
          <template #header>
            <span>处理结果</span>
          </template>
          <div class="result-content">
            <p>{{ appeal.processingResult }}</p>
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

const appealId = computed(() => route.params.id)

const appeal = reactive({
  id: '',
  appellantName: '',
  appellantType: '',
  appellantPhone: '',
  type: '',
  title: '',
  content: '',
  status: '',
  processingResult: '',
  createdAt: '',
  processingAt: '',
  resolvedAt: '',
  processorName: ''
})

const getAppealDetail = async () => {
  if (!appealId.value) {
    ElMessage.error('申诉ID不存在')
    router.push('/manager/appeals')
    return
  }
  
  try {
    loading.value = true
    const response = await fetch(`/api/admin/appeals/${appealId.value}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
      console.log('申诉详情数据:', data.data)
      // 确保所有必要字段都存在，即使后端没有返回
      const appealData = {
        id: data.data.id || '',
        appellantName: data.data.appellantName || '',
        appellantType: data.data.appellantType || '',
        appellantPhone: data.data.appellantPhone || '',
        type: data.data.type || '',
        title: data.data.title || '',
        content: data.data.content || '',
        status: data.data.status || '',
        processingResult: data.data.processingResult || '',
        createdAt: data.data.createdAt || '',
        processingAt: data.data.processingAt || '',
        resolvedAt: data.data.resolvedAt || '',
        processorName: data.data.processorName || ''
      }
      Object.assign(appeal, appealData)
    } else {
      ElMessage.error(data.message || '获取申诉信息失败')
      router.push('/manager/appeals')
    }
  } catch (error) {
    console.error('获取申诉详情失败:', error)
    ElMessage.error('网络错误，请稍后重试')
    router.push('/manager/appeals')
  } finally {
    loading.value = false
  }
}

const getTypeText = (type) => {
  const typeMap = {
    TASK: '任务申诉',
    POINT: '积分申诉',
    EXCHANGE: '兑换申诉',
    OTHER: '其他申诉'
  }
  return typeMap[type] || type
}

const getStatusType = (status) => {
  const statusMap = {
    PENDING: 'warning',
    PROCESSING: 'primary',
    RESOLVED: 'success',
    REJECTED: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    RESOLVED: '已解决',
    REJECTED: '已驳回'
  }
  return statusMap[status] || status
}

const processAppeal = () => {
  router.push(`/manager/appeals/${appealId.value}/process`)
}

onMounted(() => {
  getAppealDetail()
})
</script>

<style scoped>
.appeal-detail-container {
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

.content-card,
.action-card,
.result-card {
  margin-top: 30px;
}

.content-text,
.result-content {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  white-space: pre-wrap;
}

.content-text p,
.result-content p {
  margin: 0;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  gap: 10px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}
</style>