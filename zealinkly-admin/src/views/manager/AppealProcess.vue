<template>
  <div class="appeal-process-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>处理申诉</span>
          <div>
            <el-button size="small" @click="$router.back()">返回</el-button>
          </div>
        </div>
      </template>
      
      <el-form
        :model="processForm"
        :rules="rules"
        ref="processFormRef"
        label-width="120px"
        class="demo-ruleForm"
      >
        <el-form-item label="处理说明" prop="adminNote">
          <el-input
            v-model="processForm.adminNote"
            type="textarea"
            :rows="4"
            placeholder="请输入处理说明"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">
            提交处理结果
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
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const processFormRef = ref(null)

const appealId = computed(() => route.params.id)

const processForm = reactive({
  adminNote: ''
})

const rules = {
  adminNote: [
    { required: true, message: '请输入处理说明', trigger: 'blur' },
    { min: 5, max: 1000, message: '处理说明长度在 5 到 1000 个字符', trigger: 'blur' }
  ]
}

const submitForm = async () => {
  if (!processFormRef.value) return
  
  try {
    await processFormRef.value.validate()
    loading.value = true
    
    const response = await fetch(`/api/admin/appeals/${appealId.value}/resolve`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        adminNote: processForm.adminNote
      })
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('处理成功')
      router.push(`/manager/appeals/${appealId.value}`)
    } else {
      ElMessage.error(data.message || '处理失败')
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
  if (processFormRef.value) {
    processFormRef.value.resetFields()
  }
}

onMounted(() => {
})
</script>

<style scoped>
.appeal-process-container {
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