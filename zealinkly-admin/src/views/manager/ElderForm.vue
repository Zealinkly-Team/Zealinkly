<template>
  <div class="elder-form-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑老人' : '新增老人' }}</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>
      
      <el-form :model="elderForm" :rules="elderRules" ref="elderFormRef" label-width="120px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="elderForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="elderForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="elderForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="elderForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="elderForm.address" placeholder="请输入地址" type="textarea" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCardNumber">
          <el-input v-model="elderForm.idCardNumber" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="社区卡号" prop="communityCardNumber">
          <el-input v-model="elderForm.communityCardNumber" placeholder="请输入社区卡号" />
        </el-form-item>
        <el-form-item label="纬度" prop="lat">
          <el-input v-model.number="elderForm.lat" placeholder="请输入纬度" type="number" />
        </el-form-item>
        <el-form-item label="经度" prop="lng">
          <el-input v-model.number="elderForm.lng" placeholder="请输入经度" type="number" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="积分" prop="points">
          <el-input v-model.number="elderForm.points" placeholder="请输入积分" type="number" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="状态">
          <el-switch v-model="elderForm.enabled" active-color="#13ce66" inactive-color="#ff4949" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">保存</el-button>
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
import { elderAPI } from '@/utils/api'

const router = useRouter()
const route = useRoute()
const elderFormRef = ref(null)
const loading = ref(false)

const elderId = computed(() => route.params.id)
const isEdit = computed(() => !!elderId.value)

const elderForm = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  address: '',
  idCardNumber: '',
  communityCardNumber: '',
  lat: null,
  lng: null,
  points: 0,
  enabled: true
})

const elderRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码长度至少6位', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  idCardNumber: [{ required: true, message: '请输入身份证号', trigger: 'blur' }, { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[0-9Xx]$/, message: '身份证号格式不正确', trigger: 'blur' }],
  communityCardNumber: [{ required: true, message: '请输入社区卡号', trigger: 'blur' }],
  lat: [{ required: true, message: '请输入纬度', trigger: 'blur' }],
  lng: [{ required: true, message: '请输入经度', trigger: 'blur' }]
}

const getElderDetail = async () => {
  if (!isEdit.value) return
  
  try {
    const response = await elderAPI.getDetail(elderId.value)
    if (response.code === 200) {
      Object.assign(elderForm, response.data)
    }
  } catch (error) {
    // 错误已在API拦截器中处理
    router.push('/manager/elders')
  }
}

const handleSubmit = async () => {
  if (!elderFormRef.value) return
  
  await elderFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        let response
        
        if (isEdit.value) {
          // 编辑老人
          response = await elderAPI.update(elderId.value, elderForm)
        } else {
          // 新增老人
          response = await elderAPI.create(elderForm)
        }
        
        if (response.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
          router.push('/manager/elders')
        }
      } catch (error) {
        // 错误已在API拦截器中处理
      } finally {
        loading.value = false
      }
    }
  })
}

const resetForm = () => {
  elderFormRef.value?.resetFields()
}

onMounted(() => {
  getElderDetail()
})
</script>

<style scoped>
.elder-form-container {
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

.el-form {
  max-width: 600px;
}

.el-form-item {
  margin-bottom: 20px;
}
</style>