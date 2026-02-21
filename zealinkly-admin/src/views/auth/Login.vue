<template>
  <div class="login-container">
    <div class="login-form">
      <h2>管理员登录</h2>
      <n-form 
        :model="loginForm" 
        :rules="loginRules" 
        ref="loginFormRef" 
        label-placement="left"
        label-width="80px"
      >
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="loginForm.username" placeholder="请输入用户名" />
        </n-form-item>
        <n-form-item label="密码" path="password">
          <n-input 
            v-model:value="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password 
          />
        </n-form-item>
        <n-form-item label="用户类型" path="userType">
          <n-select v-model:value="loginForm.userType" placeholder="请选择用户类型">
            <option label="管理员" value="ADMIN" />
          </n-select>
        </n-form-item>
        <n-form-item>
          <n-button 
            type="primary" 
            @click="handleLogin" 
            :loading="loading"
            class="login-button"
          >
            登录
          </n-button>
          <n-button @click="resetForm" class="reset-button">重置</n-button>
          <n-button @click="$router.push('/register')" class="register-button">注册</n-button>
        </n-form-item>
      </n-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { NForm, NFormItem, NInput, NSelect, NButton, useMessage } from 'naive-ui'
import { authAPI } from '@/utils/api'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)
const message = useMessage()

const loginForm = reactive({
  username: '',
  password: '',
  userType: 'ADMIN'
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  const valid = await loginFormRef.value.validate()
  if (valid) {
    loading.value = true
    try {
      const response = await authAPI.login(loginForm)
      
      if (response.code === 200) {
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('userType', response.data.userType)
        localStorage.setItem('userId', response.data.userId)
        localStorage.setItem('username', response.data.username)
        
        message.success('登录成功')
        router.push('/home')
      }
    } catch (error) {
      // 错误已在API拦截器中处理
    } finally {
      loading.value = false
    }
  }
}

const resetForm = () => {
  loginFormRef.value?.resetFields()
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.login-form {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  width: 400px;
}

.login-form h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.n-form-item {
  margin-bottom: 20px;
}

.login-button {
  margin-right: 12px;
}

.reset-button {
  margin-right: 12px;
}
</style>