<template>
  <div class="task-detail-container">
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <span>任务详情</span>
          <div>
            <el-button size="small" @click="$router.back()">返回</el-button>
          </div>
        </div>
      </template>
      
      <div v-loading="loading" class="detail-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form label-width="120px" :model="task" class="detail-form">
              <el-form-item label="任务ID">
                <span>{{ task.id }}</span>
              </el-form-item>
              <el-form-item label="任务内容">
                <span>{{ task.content }}</span>
              </el-form-item>
              <el-form-item label="老人姓名">
                <span>{{ task.elderName }}</span>
              </el-form-item>
              <el-form-item label="老人电话">
                <span>{{ task.elderPhone }}</span>
              </el-form-item>
              <el-form-item label="老人地址">
                <span>{{ task.elderAddress }}</span>
              </el-form-item>
              <el-form-item label="志愿者姓名">
                <span>{{ task.volunteerName || '-' }}</span>
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :span="12">
            <el-form label-width="120px" :model="task" class="detail-form">
              <el-form-item label="志愿者电话">
                <span>{{ task.volunteerPhone || '-' }}</span>
              </el-form-item>
              <el-form-item label="状态">
                <el-tag :type="getStatusType(task.status)">
                  {{ getStatusText(task.status) }}
                </el-tag>
              </el-form-item>
              <el-form-item label="优先级">
                <el-tag :type="getPriorityType(task.priority)">
                  {{ getPriorityText(task.priority) }}
                </el-tag>
              </el-form-item>
              <el-form-item label="创建时间">
                <span>{{ task.createdAt }}</span>
              </el-form-item>
              <el-form-item label="接单时间">
                <span>{{ task.claimedAt || '-' }}</span>
              </el-form-item>
              <el-form-item label="开始时间">
                <span>{{ task.startedAt || '-' }}</span>
              </el-form-item>
              <el-form-item label="完成时间">
                <span>{{ task.completedAt || '-' }}</span>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
        
        <!-- 任务操作 -->
        <el-card class="action-card" v-if="canTakeAction(task.status)">
          <template #header>
            <span>任务操作</span>
          </template>
          <div class="action-buttons">
            <el-button type="primary" @click="handleTaskAction" :loading="actionLoading">
              {{ getActionText(task.status) }}
            </el-button>
            <el-button type="danger" @click="handleCancelTask" v-if="task.status !== 'CANCELLED'">
              取消任务
            </el-button>
          </div>
        </el-card>
        
        <!-- 任务备注 -->
        <el-card class="note-card" v-if="task.notes">
          <template #header>
            <span>任务备注</span>
          </template>
          <div class="note-content">
            <p>{{ task.notes }}</p>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
  
  <!-- 分配任务对话框 -->
  <el-dialog
    v-model="showAssignDialog"
    title="分配任务"
    width="500px"
    :close-on-click-modal="false"
  >
    <div class="assign-dialog-content">
      <p class="task-info">任务：{{ task.content }}</p>
      <p class="task-info">老人：{{ task.elderName }} {{ task.elderPhone }}</p>
      <p class="task-info">积分：{{ task.pointsReward }}</p>
      
      <el-form label-width="80px" class="mt-4">
        <el-form-item label="选择志愿者">
          <el-select
            v-model="selectedVolunteerId"
            placeholder="请选择志愿者"
            class="w-full"
          >
            <el-option
              v-for="volunteer in volunteers"
              :key="volunteer.id"
              :label="volunteer.realName + ' ' + volunteer.phone"
              :value="volunteer.id"
            >
              <div class="volunteer-option">
                <span>{{ volunteer.realName }}</span>
                <span class="phone">{{ volunteer.phone }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="cancelAssign">取消</el-button>
        <el-button type="primary" @click="assignTask" :loading="actionLoading">
          确认分配
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { taskAPI, volunteerAPI, api } from '@/utils/api'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const actionLoading = ref(false)

const taskId = computed(() => {
  // 确保获取的是数字类型的任务ID，并且只取数字部分
  const id = route.params.id
  if (typeof id === 'string') {
    // 只取数字部分，忽略后面的非数字字符
    const numericId = id.replace(/[^0-9]/g, '')
    return parseInt(numericId)
  }
  return id
})

const task = reactive({
  id: '',
  content: '',
  elderName: '',
  elderPhone: '',
  elderAddress: '',
  volunteerName: '',
  volunteerPhone: '',
  status: '',
  priority: '',
  notes: '',
  createdAt: '',
  claimedAt: '',
  startedAt: '',
  completedAt: ''
})

const getTaskDetail = async () => {
  if (!taskId.value) {
    ElMessage.error('任务ID不存在')
    router.push('/manager/tasks')
    return
  }
  
  try {
    loading.value = true
    const response = await taskAPI.getDetail(taskId.value)
    if (response.code === 200) {
      // 注意：后端返回的是TaskDetailAdminResponse对象，包含task属性
      Object.assign(task, response.data.task)
    }
  } catch (error) {
    // 错误已在API拦截器中处理
    router.push('/manager/tasks')
  } finally {
    loading.value = false
  }
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
    PENDING: '分配任务',
    CLAIMED: '开始任务',
    IN_PROGRESS: '完成任务'
  }
  return actionMap[status] || '操作'
}

const volunteers = ref([])
const showAssignDialog = ref(false)
const selectedVolunteerId = ref(null)

const getVolunteers = async () => {
  try {
    const response = await volunteerAPI.getList({ page: 0, size: 100 })
    if (response.code === 200) {
      volunteers.value = response.data.content || []
    }
  } catch (error) {
    // 错误已在API拦截器中处理
  }
}

const handleTaskAction = async () => {
  try {
    actionLoading.value = true
    let successMessage
    
    switch (task.status) {
      case 'PENDING':
        // 分配任务
        await getVolunteers()
        showAssignDialog.value = true
        actionLoading.value = false
        return
      case 'CLAIMED':
        // 开始任务
        await api.patch(`/api/admin/tasks/${taskId.value}/start`)
        successMessage = '任务已开始'
        break
      case 'IN_PROGRESS':
        // 完成任务
        await api.patch(`/api/admin/tasks/${taskId.value}/complete`)
        successMessage = '任务已完成'
        break
      default:
        actionLoading.value = false
        return
    }
    
    ElMessage.success(successMessage)
    getTaskDetail()
  } catch (error) {
    // 错误已在API拦截器中处理
  } finally {
    actionLoading.value = false
  }
}

const assignTask = async () => {
  if (!selectedVolunteerId.value) {
    ElMessage.warning('请选择志愿者')
    return
  }
  
  try {
    actionLoading.value = true
    // 调用API分配任务
    await taskAPI.update(taskId.value, {
      volunteerId: selectedVolunteerId.value,
      status: 'CLAIMED'
    })
    ElMessage.success('任务分配成功')
    showAssignDialog.value = false
    selectedVolunteerId.value = null
    getTaskDetail()
  } catch (error) {
    // 错误已在API拦截器中处理
  } finally {
    actionLoading.value = false
  }
}

const cancelAssign = () => {
  showAssignDialog.value = false
  selectedVolunteerId.value = null
}

const handleCancelTask = async () => {
  try {
    await ElMessageBox.confirm('确定要取消这个任务吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.patch(`/api/admin/tasks/${taskId.value}/cancel`)
    ElMessage.success('任务已取消')
    getTaskDetail()
  } catch (error) {
    if (error !== 'cancel') {
      // 错误已在API拦截器中处理
    }
  }
}

onMounted(() => {
  getTaskDetail()
})
</script>

<style scoped>
.task-detail-container {
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

/* 分配任务对话框样式 */
.assign-dialog-content {
  padding: 10px 0;
}

.task-info {
  margin-bottom: 10px;
  line-height: 1.5;
}

.volunteer-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.volunteer-option .phone {
  color: #606266;
  font-size: 14px;
}

.mt-4 {
  margin-top: 16px;
}
</style>