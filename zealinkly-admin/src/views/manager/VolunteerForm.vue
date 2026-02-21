<template>
  <div class="volunteer-form-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑志愿者' : '新增志愿者' }}</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>
      
      <el-form :model="volunteerForm" :rules="volunteerRules" ref="volunteerFormRef" label-width="120px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="volunteerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="volunteerForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="volunteerForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="volunteerForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="volunteerForm.address" placeholder="请输入地址" type="textarea" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCardNumber">
          <el-input v-model="volunteerForm.idCardNumber" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="社区卡号" prop="communityCardNumber">
          <el-input v-model="volunteerForm.communityCardNumber" placeholder="请输入社区卡号" />
        </el-form-item>
        <el-form-item label="纬度" prop="lat">
          <el-input v-model.number="volunteerForm.lat" placeholder="请输入纬度" type="number" />
        </el-form-item>
        <el-form-item label="经度" prop="lng">
          <el-input v-model.number="volunteerForm.lng" placeholder="请输入经度" type="number" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="积分" prop="points">
          <el-input v-model.number="volunteerForm.points" placeholder="请输入积分" type="number" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="状态">
          <el-switch v-model="volunteerForm.enabled" active-color="#13ce66" inactive-color="#ff4949" />
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
import { volunteerAPI } from '@/utils/api'

const router = useRouter()
const route = useRoute()
const volunteerFormRef = ref(null)
const loading = ref(false)

const volunteerId = computed(() => route.params.id)
const isEdit = computed(() => !!volunteerId.value)

const volunteerForm = reactive({
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

const volunteerRules = {
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

const getVolunteerDetail = async () => {
  if (!isEdit.value) return
  
  try {
    const response = await volunteerAPI.getDetail(volunteerId.value)
    if (response.code === 200) {
      Object.assign(volunteerForm, response.data)
    }
  } catch (error) {
    // 错误已在API拦截器中处理
    router.push('/manager/volunteers')
  }
}

const handleSubmit = async () => {
  if (!volunteerFormRef.value) return
  
  await volunteerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        let response
        
        if (isEdit.value) {
          // 编辑志愿者
          response = await volunteerAPI.update(volunteerId.value, volunteerForm)
        } else {
          // 新增志愿者
          response = await volunteerAPI.create(volunteerForm)
        }
        
        if (response.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
          router.push('/manager/volunteers')
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
  volunteerFormRef.value?.resetFields()
}

onMounted(() => {
  getVolunteerDetail()
})
</script>

<style scoped>
.volunteer-form-container {
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