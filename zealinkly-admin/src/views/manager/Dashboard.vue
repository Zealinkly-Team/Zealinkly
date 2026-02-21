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
    
    <!-- 最近任务 -->
    <el-card class="recent-tasks">
      <template #header>
        <div class="card-header">
          <span>最近任务</span>
          <el-button size="small" @click="$router.push('/manager/tasks')">查看全部</el-button>
        </div>
      </template>
      <el-table :data="recentTasks" style="width: 100%">
        <el-table-column prop="id" label="任务ID" width="80" />
        <el-table-column prop="content" label="任务内容" />
        <el-table-column prop="elderName" label="老人姓名" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button size="small" @click="viewTask(scope.row.id)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, UserFilled, List, Warning } from '@element-plus/icons-vue'

const router = useRouter()
const elderCount = ref(0)
const volunteerCount = ref(0)
const taskCount = ref(0)
const emergencyCount = ref(0)
const recentTasks = ref([])

onMounted(() => {
  // 模拟数据，实际项目中应该从API获取
  elderCount.value = 128
  volunteerCount.value = 86
  taskCount.value = 324
  emergencyCount.value = 2
  
  recentTasks.value = [
    {
      id: 101,
      content: '需要帮忙购买生活用品',
      elderName: '张大爷',
      status: 'PENDING',
      createdAt: '2026-02-12 10:30:00'
    },
    {
      id: 102,
      content: '需要有人陪伴去医院',
      elderName: '李奶奶',
      status: 'CLAIMED',
      createdAt: '2026-02-12 09:15:00'
    },
    {
      id: 103,
      content: '紧急求助：老人摔倒',
      elderName: '王爷爷',
      status: 'IN_PROGRESS',
      createdAt: '2026-02-12 08:45:00'
    }
  ]
})

const getStatusType = (status) => {
  const statusMap = {
    PENDING: 'info',
    CLAIMED: 'warning',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    PENDING: '待接单',
    CLAIMED: '已接单',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return statusMap[status] || status
}

const viewTask = (id) => {
  router.push(`/manager/tasks/${id}`)
}
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

.recent-tasks {
  margin-top: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
</style>