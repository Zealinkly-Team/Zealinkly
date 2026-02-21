<template>
  <div class="notification-form-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>通知管理</span>
          <div>
            <el-button size="small" @click="$router.back()">返回</el-button>
          </div>
        </div>
      </template>
      
      <el-form
        :model="notificationForm"
        :rules="rules"
        ref="notificationFormRef"
        label-width="120px"
        class="demo-ruleForm"
      >
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="notificationForm.title" placeholder="请输入通知标题" />
        </el-form-item>
        
        <el-form-item label="通知内容" prop="content">
          <el-input
            v-model="notificationForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入通知内容"
          />
        </el-form-item>
        
        <el-form-item label="接收对象" prop="targetUserType">
          <el-select v-model="notificationForm.targetUserType" placeholder="请选择接收对象">
            <el-option label="所有用户" :value="'ALL'" />
            <el-option label="仅老人" :value="'ELDER'" />
            <el-option label="仅志愿者" :value="'VOLUNTEER'" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="通知类型" prop="type">
          <el-select v-model="notificationForm.type" placeholder="请选择通知类型">
            <el-option label="系统通知" :value="'SYSTEM'" />
            <el-option label="活动通知" :value="'ACTIVITY'" />
            <el-option label="积分通知" :value="'POINTS'" />
            <el-option label="任务通知" :value="'TASK'" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">
            发送通知
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { notificationAPI } from '@/utils/api'

const router = useRouter()
const loading = ref(false)
const notificationFormRef = ref(null)

const notificationForm = reactive({
  title: '',
  content: '',
  targetUserType: 'ALL',
  type: 'SYSTEM'
})

const rules = {
  title: [
    { required: true, message: '请输入通知标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入通知内容', trigger: 'blur' },
    { min: 1, max: 1000, message: '内容长度在 1 到 1000 个字符', trigger: 'blur' }
  ],
  targetUserType: [
    { required: true, message: '请选择接收对象', trigger: 'change' }
  ],
  type: [
    { required: true, message: '请选择通知类型', trigger: 'change' }
  ]
}

const submitForm = async () => {
  if (!notificationFormRef.value) return
  
  try {
    await notificationFormRef.value.validate()
    loading.value = true
    
    // 转换数据格式以匹配后端API
    const broadcastData = {
      targetType: notificationForm.targetUserType === 'ALL' ? 'ALL_ELDERS' : 
                  notificationForm.targetUserType === 'ELDER' ? 'ALL_ELDERS' : 'ALL_VOLUNTEERS',
      title: notificationForm.title,
      message: notificationForm.content
    }
    
    const response = await notificationAPI.broadcast(broadcastData)
    
    if (response.code === 200) {
      ElMessage.success('通知发送成功')
      resetForm()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  if (notificationFormRef.value) {
    notificationFormRef.value.resetFields()
  }
}

onMounted(() => {
})
</script>

<style scoped>
.notification-form-container {
  padding: 20px 0;
}

.form-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.demo-ruleForm {
  padding: 20px 0;
}

.demo-ruleForm .el-form-item {
  margin-bottom: 20px;
}
</style>