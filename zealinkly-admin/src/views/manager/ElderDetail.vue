<template>
  <div class="elder-detail-container">
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <span>老人详情</span>
          <div>
            <el-button size="small" @click="handleEdit">编辑</el-button>
            <el-button size="small" @click="$router.back()">返回</el-button>
          </div>
        </div>
      </template>
      
      <div v-loading="loading" class="detail-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form label-width="120px" :model="elder" class="detail-form">
              <el-form-item label="老人ID">
                <span>{{ elder.id }}</span>
              </el-form-item>
              <el-form-item label="用户名">
                <span>{{ elder.username }}</span>
              </el-form-item>
              <el-form-item label="真实姓名">
                <span>{{ elder.realName }}</span>
              </el-form-item>
              <el-form-item label="手机号">
                <span>{{ elder.phone }}</span>
              </el-form-item>
              <el-form-item label="地址">
                <span>{{ elder.address }}</span>
              </el-form-item>
              <el-form-item label="身份证号">
                <span>{{ elder.idCardNumber }}</span>
              </el-form-item>
              <el-form-item label="社区卡号">
                <span>{{ elder.communityCardNumber }}</span>
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :span="12">
            <el-form label-width="120px" :model="elder" class="detail-form">
              <el-form-item label="纬度">
                <span>{{ elder.lat }}</span>
              </el-form-item>
              <el-form-item label="经度">
                <span>{{ elder.lng }}</span>
              </el-form-item>
              <el-form-item label="积分">
                <span>{{ elder.points }}</span>
              </el-form-item>
              <el-form-item label="状态">
                <el-tag :type="elder.enabled ? 'success' : 'danger'">
                  {{ elder.enabled ? '启用' : '禁用' }}
                </el-tag>
              </el-form-item>
              <el-form-item label="创建时间">
                <span>{{ elder.createdAt }}</span>
              </el-form-item>
              <el-form-item label="更新时间">
                <span>{{ elder.updatedAt }}</span>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
        
        <!-- 紧急联系人 -->
        <el-card class="contact-card" v-if="emergencyContacts.length > 0">
          <template #header>
            <span>紧急联系人</span>
            <el-button type="primary" size="small" @click="handleAddContact">添加</el-button>
          </template>
          <el-table :data="emergencyContacts" style="width: 100%">
            <el-table-column prop="name" label="姓名" width="120" />
            <el-table-column prop="relation" label="关系" width="120" />
            <el-table-column prop="phone" label="手机号" width="150" />
            <el-table-column prop="priority" label="优先级" width="100" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button size="small" @click="handleEditContact(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDeleteContact(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
        
        <!-- 发放积分 -->
        <el-card class="points-card">
          <template #header>
            <span>发放积分</span>
          </template>
          <div class="points-form">
            <el-form :model="pointsForm" label-width="120px" class="demo-ruleForm">
              <el-form-item label="积分数量" prop="amount">
                <el-input v-model.number="pointsForm.amount" placeholder="请输入积分数量" />
              </el-form-item>
              <el-form-item label="发放原因" prop="reason">
                <el-input v-model="pointsForm.reason" type="textarea" placeholder="请输入发放原因" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleGrantPoints">确认发放</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { elderAPI, emergencyContactAPI } from '@/utils/api'

const router = useRouter()
const route = useRoute()
const loading = ref(true)

const elderId = computed(() => route.params.id)

const elder = reactive({
  id: '',
  username: '',
  realName: '',
  phone: '',
  address: '',
  idCardNumber: '',
  communityCardNumber: '',
  lat: null,
  lng: null,
  points: 0,
  enabled: true,
  createdAt: '',
  updatedAt: ''
})

const emergencyContacts = ref([])
const pointsForm = reactive({
  amount: 0,
  reason: ''
})

const getElderDetail = async () => {
  if (!elderId.value) {
    ElMessage.error('老人ID不存在')
    router.push('/manager/elders')
    return
  }
  
  try {
    loading.value = true
    const response = await elderAPI.getDetail(elderId.value)
    if (response.code === 200) {
      console.log('老人详情数据:', response.data)
      // 确保所有必要字段都存在，即使后端没有返回
      const elderData = {
        id: response.data.id || '',
        username: response.data.username || '',
        realName: response.data.realName || '',
        phone: response.data.phone || '',
        address: response.data.address || '',
        idCardNumber: response.data.idCardNumber || '',
        communityCardNumber: response.data.communityCardNumber || '',
        lat: response.data.lat || null,
        lng: response.data.lng || null,
        points: response.data.points || 0,
        enabled: response.data.enabled !== false,
        createdAt: response.data.createdAt || '',
        updatedAt: response.data.updatedAt || ''
      }
      Object.assign(elder, elderData)
    }
  } catch (error) {
    // 错误已在API拦截器中处理
    console.error('获取老人详情失败:', error)
    router.push('/manager/elders')
  } finally {
    loading.value = false
  }
}

const getEmergencyContacts = async () => {
  if (!elderId.value) return
  
  try {
    const response = await emergencyContactAPI.getList(elderId.value)
    if (response.code === 200) {
      emergencyContacts.value = response.data || []
    }
  } catch (error) {
    // 错误已在API拦截器中处理
  }
}

const handleEdit = () => {
  router.push(`/manager/elders/${elderId.value}/edit`)
}

const handleAddContact = () => {
  router.push(`/manager/elders/${elderId.value}/emergency-contacts/add`)
}

const handleEditContact = (contact) => {
  router.push(`/manager/elders/${elderId.value}/emergency-contacts/${contact.id}/edit`)
}

const handleDeleteContact = async (contactId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个紧急联系人吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await emergencyContactAPI.delete(elderId.value, contactId)
    ElMessage.success('紧急联系人已删除')
    getEmergencyContacts()
  } catch (error) {
    if (error !== 'cancel') {
      // 错误已在API拦截器中处理
    }
  }
}

const handleGrantPoints = async () => {
  if (pointsForm.amount <= 0) {
    ElMessage.warning('请输入有效的积分数量')
    return
  }
  
  try {
    await elderAPI.grantPoints(elderId.value, {
      amount: pointsForm.amount,
      reason: pointsForm.reason
    })
    ElMessage.success('积分发放成功')
    // 重置表单
    pointsForm.amount = 0
    pointsForm.reason = ''
    // 重新获取老人详情
    getElderDetail()
  } catch (error) {
    // 错误已在API拦截器中处理
  }
}

onMounted(() => {
  getElderDetail()
  getEmergencyContacts()
})
</script>

<style scoped>
.elder-detail-container {
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

.contact-card {
  margin-top: 30px;
}

.points-card {
  margin-top: 30px;
}

.points-form {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}
</style>