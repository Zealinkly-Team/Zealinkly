<template>
  <div class="emergency-list-container">
    <div class="page-header">
      <h3>紧急报警管理</h3>
      <el-button type="primary" @click="refreshEmergencies">刷新列表</el-button>
    </div>
    
    <!-- 报警列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>报警列表</span>
          <span class="emergency-count" v-if="emergencies.length > 0">
            共 {{ emergencies.length }} 条记录
          </span>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="emergencies"
        style="width: 100%"
        empty-text="暂无紧急报警记录"
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
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="viewEmergency(scope.row.id)">查看</el-button>
            <el-button 
              v-if="scope.row.status === 'PENDING'" 
              size="small" 
              type="primary" 
              @click="startEmergency(scope.row.id)"
            >
              开始处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '@/utils/api'

const router = useRouter()
const loading = ref(false)
const emergencies = ref([])

// 模拟测试数据
const mockEmergencies = [
  {
    id: 1,
    content: '老人摔倒检测',
    elderName: '张三',
    elderPhone: '13800138000',
    status: 'PENDING',
    createdAt: new Date().toISOString()
  },
  {
    id: 2,
    content: '心率异常检测',
    elderName: '李四',
    elderPhone: '13900139000',
    status: 'PROCESSING',
    createdAt: new Date().toISOString()
  }
]

const getEmergencies = async () => {
  loading.value = true
  try {
    console.log('=== 获取紧急报警列表开始 ===')
    
    // 尝试调用 API
    console.log('尝试调用 API: /emergency/pending')
    const response = await api.get('/emergency/pending')
    
    console.log('=== 紧急报警列表响应 ===')
    console.log('响应完整内容:', response)
    console.log('响应数据:', response.data)
    console.log('响应状态码:', response.code)
    
    if (response.code === 200) {
      // 检查响应数据是否为数组
      console.log('响应数据类型检查:', {
        dataType: typeof response.data,
        isArray: Array.isArray(response.data),
        dataLength: Array.isArray(response.data) ? response.data.length : 0
      })
      
      if (Array.isArray(response.data)) {
        // 处理响应数据
        emergencies.value = (response.data || []).map((emergency, index) => {
          console.log(`处理第 ${index} 个紧急报警:`)
          console.log('原始紧急报警数据:', emergency)
          
          const processedEmergency = {
            id: emergency.id || '',
            content: emergency.content || emergency.message || emergency.detail || '',
            elderName: emergency.elderName || emergency.name || emergency.elder || '',
            elderPhone: emergency.elderPhone || emergency.phone || emergency.elderPhoneNumber || '',
            status: emergency.status || 'PENDING',
            createdAt: emergency.createdAt || emergency.time || emergency.timestamp || ''
          }
          
          console.log('处理后的紧急报警数据:', processedEmergency)
          return processedEmergency
        })
        
        console.log('=== 处理完成 ===')
        console.log('处理后的紧急报警数量:', emergencies.value.length)
        console.log('处理后的紧急报警列表:', emergencies.value)
        
        // 如果没有数据，显示空列表
        if (emergencies.value.length === 0) {
          ElMessage.info('当前没有紧急报警记录')
          console.log('没有紧急报警记录')
        }
      } else {
        console.error('响应数据格式不正确: 响应数据不是数组', response.data)
        ElMessage.error('获取紧急报警列表失败: 响应数据格式不正确')
        // 显示空列表
        emergencies.value = []
      }
    } else {
      console.error('获取紧急报警列表失败:', response.message)
      ElMessage.error(`获取紧急报警列表失败: ${response.message || '未知错误'}`)
      // 显示空列表
      emergencies.value = []
    }
  } catch (error) {
    console.error('=== 获取紧急报警列表失败 ===')
    console.error('错误对象:', error)
    console.error('错误消息:', error.message)
    console.error('错误响应:', error.response)
    console.error('错误配置:', error.config)
    
    // 显示友好的错误信息
    let errorMessage = '获取紧急报警列表失败'
    if (error.response) {
      errorMessage = `${errorMessage}: ${error.response.data?.message || `服务器错误 (${error.response.status})`}`
    } else if (error.request) {
      errorMessage = `${errorMessage}: 服务器无响应，请检查网络连接`
    } else {
      errorMessage = `${errorMessage}: ${error.message}`
    }
    ElMessage.error(errorMessage)
    
    // 显示空列表
    emergencies.value = []
    console.log('显示空列表')
  } finally {
    loading.value = false
    console.log('=== 获取紧急报警列表结束 ===')
  }
}

const refreshEmergencies = () => {
  getEmergencies()
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

const viewEmergency = (id) => {
  // 确保id是数字类型
  const numericId = parseInt(id)
  if (isNaN(numericId)) {
    ElMessage.error('无效的报警ID')
    return
  }
  router.push(`/manager/emergency/${numericId}`)
}

const startEmergency = async (id) => {
  try {
    // 确保id是数字类型
    const numericId = parseInt(id)
    if (isNaN(numericId)) {
      ElMessage.error('无效的报警ID')
      return
    }
    await api.patch(`/emergency/${numericId}/start`)
    ElMessage.success('开始处理成功')
    getEmergencies()
    // 触发仪表盘更新
    window.dispatchEvent(new CustomEvent('refreshDashboard'))
  } catch (error) {
    ElMessage.error('开始处理失败：' + (error.response?.data?.message || error.message))
  }
}

onMounted(() => {
  getEmergencies()
})

// 当组件被激活时（从详情页面返回时），刷新列表
onActivated(() => {
  console.log('=== 组件被激活，刷新紧急报警列表 ===')
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.emergency-count {
  font-size: 14px;
  color: #606266;
}

.list-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
</style>