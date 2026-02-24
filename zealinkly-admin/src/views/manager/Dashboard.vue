<template>
  <div class="dashboard-container">
    <h3>系统仪表盘</h3>
    
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-content">
          <el-icon class="stat-icon"><User /></el-icon>
          <div>
            <h4>老人总数</h4>
            <p class="stat-number">{{ elderCount }}</p>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <el-icon class="stat-icon"><UserFilled /></el-icon>
          <div>
            <h4>志愿者总数</h4>
            <p class="stat-number">{{ volunteerCount }}</p>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <el-icon class="stat-icon"><List /></el-icon>
          <div>
            <h4>任务总数</h4>
            <p class="stat-number">{{ taskCount }}</p>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card danger">
        <div class="stat-content">
          <el-icon class="stat-icon"><Warning /></el-icon>
          <div>
            <h4>待处理报警</h4>
            <p class="stat-number">{{ emergencyCount }}</p>
          </div>
        </div>
      </el-card>
    </div>
    

  </div>
</template>

<script setup>
import { ref, onMounted, onActivated, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, UserFilled, List, Warning } from '@element-plus/icons-vue'
import { api } from '@/utils/api'

const router = useRouter()
const elderCount = ref(0)
const volunteerCount = ref(0)
const taskCount = ref(0)
const emergencyCount = ref(0)

const getEmergencyCount = async () => {
  try {
    console.log('=== 获取待处理报警数开始 ===')
    const response = await api.get('/emergency/pending')
    console.log('=== 待处理报警数响应 ===')
    console.log('响应完整内容:', response)
    console.log('响应数据:', response.data)
    console.log('响应状态码:', response.code)
    
    if (response.code === 200 && Array.isArray(response.data)) {
      emergencyCount.value = response.data.length
      console.log('待处理报警数更新为:', emergencyCount.value)
    } else {
      console.error('获取待处理报警数失败: 响应数据格式不正确', response.data)
      emergencyCount.value = 0
    }
  } catch (error) {
    console.error('=== 获取待处理报警数失败 ===')
    console.error('错误对象:', error)
    console.error('错误消息:', error.message)
    console.error('错误响应:', error.response)
    emergencyCount.value = 0
  }
}

const getDashboardData = async () => {
  // 模拟数据，实际项目中应该从API获取
  elderCount.value = 128
  volunteerCount.value = 86
  taskCount.value = 324
  
  // 获取实时的待处理报警数
  await getEmergencyCount()
  console.log('=== 仪表盘数据加载完成 ===')
  console.log('待处理报警数:', emergencyCount.value)
}

onMounted(() => {
  getDashboardData()
  // 添加事件监听器
  window.addEventListener('refreshDashboard', getDashboardData)
})

// 当组件被激活时（从其他页面返回时），刷新数据
onActivated(() => {
  console.log('=== 组件被激活，刷新仪表盘数据 ===')
  getDashboardData()
})

// 组件卸载时移除事件监听器
onUnmounted(() => {
  window.removeEventListener('refreshDashboard', getDashboardData)
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px 0;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-card.danger {
  border-left: 4px solid #f56c6c;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 0;
}

.stat-icon {
  font-size: 48px;
  color: #409eff;
}

.stat-card.danger .stat-icon {
  color: #f56c6c;
}

.stat-content h4 {
  margin: 0 0 10px 0;
  color: #606266;
  font-size: 14px;
}

.stat-number {
  margin: 0;
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>