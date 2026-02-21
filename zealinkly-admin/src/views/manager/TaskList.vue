<template>
  <div class="task-list-container">
    <div class="page-header">
      <h3>任务管理</h3>
    </div>
    
    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="任务内容">
          <el-input v-model="searchForm.content" placeholder="请输入任务内容" />
        </el-form-item>
        <el-form-item label="老人姓名">
          <el-input v-model="searchForm.elderName" placeholder="请输入老人姓名" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态">
            <el-option label="待接单" :value="'PENDING'" />
            <el-option label="已接单" :value="'CLAIMED'" />
            <el-option label="进行中" :value="'IN_PROGRESS'" />
            <el-option label="已完成" :value="'COMPLETED'" />
            <el-option label="已取消" :value="'CANCELLED'" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 任务列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>任务列表</span>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="tasks"
        style="width: 100%"
      >
        <el-table-column prop="id" label="任务ID" width="80" />
        <el-table-column prop="content" label="任务内容" />
        <el-table-column prop="elderName" label="老人姓名" width="120" />
        <el-table-column prop="volunteerName" label="志愿者姓名" width="120">
          <template #default="scope">
            <span>{{ scope.row.volunteerName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="getPriorityType(scope.row.priority)">{{ getPriorityText(scope.row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column prop="completedAt" label="完成时间" width="180">
          <template #default="scope">
            <span>{{ scope.row.completedAt || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="viewTask(scope.row.id)">查看</el-button>
            <el-button size="small" type="primary" @click="handleTaskAction(scope.row)" v-if="canTakeAction(scope.row.status)">
              {{ getActionText(scope.row.status) }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { taskAPI, api } from '@/utils/api'

const router = useRouter()
const loading = ref(false)
const tasks = ref([])
const total = ref(0)
const page = ref(0)
const size = ref(20)

const searchForm = reactive({
  content: '',
  elderName: '',
  status: ''
})

const getTasks = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
      content: searchForm.content,
      elderName: searchForm.elderName,
      status: searchForm.status
    }
    
    const response = await taskAPI.getList(params)
    tasks.value = response.data.content || []
    total.value = response.data.totalElements || 0
  } catch (error) {
    // 错误已在API拦截器中处理
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 0
  getTasks()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  page.value = 0
  getTasks()
}

const handleSizeChange = (newSize) => {
  size.value = newSize
  getTasks()
}

const handleCurrentChange = (newPage) => {
  page.value = newPage
  getTasks()
}

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

const getPriorityType = (priority) => {
  const priorityMap = {
    LOW: 'info',
    MEDIUM: 'warning',
    HIGH: 'danger'
  }
  return priorityMap[priority] || 'info'
}

const getPriorityText = (priority) => {
  const priorityMap = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高'
  }
  return priorityMap[priority] || priority
}

const canTakeAction = (status) => {
  return ['PENDING', 'CLAIMED', 'IN_PROGRESS'].includes(status)
}

const getActionText = (status) => {
  const actionMap = {
    PENDING: '分配',
    CLAIMED: '开始',
    IN_PROGRESS: '完成'
  }
  return actionMap[status] || '操作'
}

const handleTaskAction = async (task) => {
  try {
    let successMessage
    
    switch (task.status) {
      case 'PENDING':
        // 分配任务 - 这里简化处理，实际应该弹出分配对话框
        ElMessage.info('任务分配功能开发中')
        return
      case 'CLAIMED':
        // 开始任务
        await api.patch(`/api/admin/tasks/${task.id}/start`)
        successMessage = '任务已开始'
        break
      case 'IN_PROGRESS':
        // 完成任务
        await api.patch(`/api/admin/tasks/${task.id}/complete`)
        successMessage = '任务已完成'
        break
      default:
        return
    }
    
    ElMessage.success(successMessage)
    getTasks()
  } catch (error) {
    // 错误已在API拦截器中处理
  }
}

const viewTask = (id) => {
  router.push(`/manager/tasks/${id}`)
}

onMounted(() => {
  getTasks()
})
</script>

<style scoped>
.task-list-container {
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