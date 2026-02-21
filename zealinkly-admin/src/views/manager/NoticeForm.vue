<template>
  <div class="notice-form-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑通知' : '新增通知' }}</span>
          <div>
            <el-button size="small" @click="$router.back()">返回</el-button>
          </div>
        </div>
      </template>
      
      <el-form
        :model="noticeForm"
        :rules="rules"
        ref="noticeFormRef"
        label-width="120px"
        class="demo-ruleForm"
      >
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="noticeForm.title" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="通知类型" prop="type">
          <el-select v-model="noticeForm.type" placeholder="请选择通知类型">
            <el-option label="系统通知" :value="'SYSTEM'" />
            <el-option label="任务通知" :value="'TASK'" />
            <el-option label="积分通知" :value="'POINT'" />
            <el-option label="活动通知" :value="'ACTIVITY'" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标类型" prop="targetType">
          <el-select v-model="noticeForm.targetType" placeholder="请选择目标类型">
            <el-option label="全体用户" :value="'ALL'" />
            <el-option label="老人用户" :value="'ELDER'" />
            <el-option label="志愿者用户" :value="'VOLUNTEER'" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知内容" prop="content">
          <el-input
            v-model="noticeForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入通知内容"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">
            {{ isEdit ? '保存修改' : '创建通知' }}
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const noticeFormRef = ref(null)

const noticeId = computed(() => route.params.id)
const isEdit = computed(() => !!noticeId.value)

const noticeForm = reactive({
  title: '',
  type: 'SYSTEM',
  targetType: 'ALL',
  content: ''
})

const rules = {
  title: [
    { required: true, message: '请输入通知标题', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择通知类型', trigger: 'change' }
  ],
  targetType: [
    { required: true, message: '请选择目标类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入通知内容', trigger: 'blur' },
    { min: 1, max: 1000, message: '长度在 1 到 1000 个字符', trigger: 'blur' }
  ]
}

const getNoticeDetail = async () => {
  if (!noticeId.value) return
  
  try {
    loading.value = true
    const response = await fetch(`/api/admin/notices/${noticeId.value}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
      Object.assign(noticeForm, data.data)
    } else {
      ElMessage.error(data.message || '获取通知信息失败')
      router.push('/manager/notices')
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
    router.push('/manager/notices')
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  if (!noticeFormRef.value) return
  
  try {
    await noticeFormRef.value.validate()
    loading.value = true
    
    let url, method, successMessage
    if (isEdit.value) {
      url = `/api/admin/notices/${noticeId.value}`
      method = 'PUT'
      successMessage = '通知已更新'
    } else {
      url = '/api/admin/notices'
      method = 'POST'
      successMessage = '通知已创建'
    }
    
    const response = await fetch(url, {
      method: method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(noticeForm)
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success(successMessage)
      router.push('/manager/notices')
    } else {
      ElMessage.error(data.message || '操作失败')
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
  if (noticeFormRef.value) {
    noticeFormRef.value.resetFields()
  }
}

onMounted(() => {
  if (isEdit.value) {
    getNoticeDetail()
  }
})
</script>

<style scoped>
.notice-form-container {
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