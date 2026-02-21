<template>
  <div class="manager-container">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h3>管理后台</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical-demo"
        @select="handleMenuSelect"
      >
        <el-menu-item index="dashboard">
          <el-icon><House /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-sub-menu index="elders">
          <template #title>
            <el-icon><User /></el-icon>
            <span>老人管理</span>
          </template>
          <el-menu-item index="elders/list">老人列表</el-menu-item>
          <el-menu-item index="elders/create">新增老人</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="volunteers">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span>志愿者管理</span>
          </template>
          <el-menu-item index="volunteers/list">志愿者列表</el-menu-item>
          <el-menu-item index="volunteers/create">新增志愿者</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="tasks">
          <template #title>
            <el-icon><List /></el-icon>
            <span>任务管理</span>
          </template>
          <el-menu-item index="tasks/list">任务列表</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="emergency">
          <el-icon><Warning /></el-icon>
          <span>紧急报警</span>
        </el-menu-item>
        <el-sub-menu index="products">
          <template #title>
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </template>
          <el-menu-item index="products/list">商品列表</el-menu-item>
          <el-menu-item index="products/create">新增商品</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="exchanges">
          <template #title>
            <el-icon><Money /></el-icon>
            <span>兑换管理</span>
          </template>
          <el-menu-item index="exchanges/list">兑换记录</el-menu-item>
          <el-menu-item index="exchanges/create">商品兑换</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="appeals">
          <template #title>
            <el-icon><ChatLineSquare /></el-icon>
            <span>申诉管理</span>
          </template>
          <el-menu-item index="appeals/list">申诉列表</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="notifications">
          <el-icon><Bell /></el-icon>
          <span>通知管理</span>
        </el-menu-item>
        <el-menu-item index="points">
          <el-icon><Star /></el-icon>
          <span>积分管理</span>
        </el-menu-item>
      </el-menu>
    </div>
    
    <!-- 主内容区域 -->
    <div class="main-content">
      <div class="content-header">
        <div class="user-info">
          <span>{{ username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </div>
      <div class="content-body">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { House, User, UserFilled, List, Warning, Goods, Money, ChatLineSquare, Bell, Star } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const username = ref('')

const activeMenu = computed(() => {
  const path = route.path
  if (path.includes('/dashboard')) return 'dashboard'
  if (path.includes('/elders')) return 'elders'
  if (path.includes('/volunteers')) return 'volunteers'
  if (path.includes('/tasks')) return 'tasks'
  if (path.includes('/emergency')) return 'emergency'
  if (path.includes('/products')) return 'products'
  if (path.includes('/exchanges')) return 'exchanges'
  if (path.includes('/appeals')) return 'appeals'
  if (path.includes('/notifications')) return 'notifications'
  if (path.includes('/points')) return 'points'
  return 'dashboard'
})

const pageTitle = computed(() => {
  const path = route.path
  if (path.includes('/dashboard')) return '仪表盘'
  if (path.includes('/elders')) return '老人管理'
  if (path.includes('/volunteers')) return '志愿者管理'
  if (path.includes('/tasks')) return '任务管理'
  if (path.includes('/emergency')) return '紧急报警管理'
  if (path.includes('/products')) return '商品管理'
  if (path.includes('/exchanges')) return '兑换管理'
  if (path.includes('/appeals')) return '申诉管理'
  if (path.includes('/notifications')) return '通知管理'
  if (path.includes('/points')) return '积分管理'
  return '管理后台'
})

onMounted(() => {
  username.value = localStorage.getItem('username') || '管理员'
})

const handleMenuSelect = (key) => {
  switch (key) {
    case 'dashboard':
      router.push('/manager/dashboard')
      break
    case 'elders/list':
      router.push('/manager/elders')
      break
    case 'elders/create':
      router.push('/manager/elders/create')
      break
    case 'volunteers/list':
      router.push('/manager/volunteers')
      break
    case 'volunteers/create':
      router.push('/manager/volunteers/create')
      break
    case 'tasks/list':
      router.push('/manager/tasks')
      break
    case 'emergency':
      router.push('/manager/emergency')
      break
    case 'products/list':
      router.push('/manager/products')
      break
    case 'products/create':
      router.push('/manager/products/create')
      break
    case 'exchanges/list':
      router.push('/manager/exchanges')
      break
    case 'exchanges/create':
      router.push('/manager/exchanges/create')
      break
    case 'appeals/list':
      router.push('/manager/appeals')
      break
    case 'notifications':
      router.push('/manager/notifications')
      break
    case 'points':
      router.push('/manager/points')
      break
  }
}

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
.manager-container {
  display: flex;
  min-height: 100vh;
  background-color: #ffffff;
}

.sidebar {
  width: 250px;
  background-color: #2c3e50;
  color: rgb(255, 255, 255);
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  overflow-y: auto;
  z-index: 0;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #34495e;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 18px;
}

.el-menu-vertical-demo {
  background-color: rgba(44, 62, 80, 0.2);
  border-right: none;
}

.sidebar .el-menu-item,
.sidebar .el-sub-menu__title {
  color: rgb(255, 255, 255) !important;
  height: 60px;
  line-height: 60px;
  opacity: 1;
  background-color: rgba(44, 62, 80);
}

.sidebar .el-menu-item .el-icon,
.sidebar .el-sub-menu__title .el-icon {
  color: white !important;
}

/* 直接为侧边栏中的span元素设置颜色 */
.sidebar .el-menu-item span,
.sidebar .el-sub-menu__title span {
  color: white !important;
}

.el-menu-item:hover,
.el-sub-menu__title:hover {
  background-color: rgba(52, 73, 94, 0.4);
  opacity: 1 !important;
}

.el-menu-item.is-active {
  background-color: #3498db;
  color: white !important;
  opacity: 1 !important;
}

.main-content {
  flex: 1;
  margin-left: 250px;
  padding: 20px;
  z-index: 2;
  position: relative;
}

.content-header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 30px;
}



.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.content-body {
  background-color: rgb(255, 255, 255);
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
</style>