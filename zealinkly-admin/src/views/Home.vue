<template>
  <div class="home-container">
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <span>欢迎回来，{{ username }}</span>
          <el-button type="danger" @click="handleLogout">退出登录</el-button>
        </div>
      </template>
      <div class="welcome-content">
        <h1>智链邻里 - 管理员系统</h1>
        <p>这是管理员后台管理系统，您可以在这里管理老人、志愿者、任务等各项内容。</p>
        <div class="quick-links">
          <el-button type="primary" @click="$router.push('/manager/dashboard')">进入管理后台</el-button>
          <el-button @click="$router.push('/manager/emergency')">查看紧急报警</el-button>
          <el-button @click="$router.push('/manager/tasks')">管理任务</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const username = ref('')

onMounted(() => {
  username.value = localStorage.getItem('username') || '管理员'
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userType')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  
  ElMessage.success('退出登录成功')
  router.push('/login')
}
</script>

<style scoped>
.home-container {
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-content {
  text-align: center;
  padding: 40px 0;
}

.welcome-content h1 {
  font-size: 28px;
  margin-bottom: 20px;
  color: #303133;
}

.welcome-content p {
  font-size: 16px;
  margin-bottom: 40px;
  color: #606266;
}

.quick-links {
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style>