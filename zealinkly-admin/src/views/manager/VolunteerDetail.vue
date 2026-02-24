<template>
  <div class="volunteer-detail-container">
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <span>志愿者详情</span>
          <div>
            <el-button size="small" @click="handleEdit">编辑</el-button>
            <el-button size="small" @click="$router.back()">返回</el-button>
          </div>
        </div>
      </template>
      
      <div v-loading="loading" class="detail-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form label-width="120px" :model="volunteer" class="detail-form">
              <el-form-item label="用户名">
                <span>{{ volunteer.username }}</span>
              </el-form-item>
              <el-form-item label="真实姓名">
                <span>{{ volunteer.realName }}</span>
              </el-form-item>
              <el-form-item label="手机号">
                <span>{{ volunteer.phone }}</span>
              </el-form-item>
              <el-form-item label="地址">
                <span>{{ volunteer.address }}</span>
              </el-form-item>
              <el-form-item label="身份证号">
                <span>{{ volunteer.idCardNumber }}</span>
              </el-form-item>
              <el-form-item label="社区卡号">
                <span>{{ volunteer.communityCardNumber }}</span>
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :span="12">
            <el-form label-width="120px" :model="volunteer" class="detail-form">
              <el-form-item label="纬度">
                <span>{{ volunteer.lat }}</span>
              </el-form-item>
              <el-form-item label="经度">
                <span>{{ volunteer.lng }}</span>
              </el-form-item>
              <el-form-item label="积分">
                <span>{{ volunteer.points }}</span>
              </el-form-item>
              <el-form-item label="状态">
                <el-tag :type="volunteer.enabled ? 'success' : 'danger'">
                  {{ volunteer.enabled ? '启用' : '禁用' }}
                </el-tag>
              </el-form-item>
              <el-form-item label="创建时间">
                <span>{{ volunteer.createdAt }}</span>
              </el-form-item>
              <el-form-item label="更新时间">
                <span>{{ volunteer.updatedAt }}</span>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
        
        <!-- 地图展示 -->
        <el-card class="map-card" v-if="volunteer.lat && volunteer.lng">
          <template #header>
            <span>位置信息</span>
          </template>
          <div class="map-container">
            <p>纬度: {{ volunteer.lat }}</p>
            <p>经度: {{ volunteer.lng }}</p>
            <div class="map-placeholder">
              <el-icon class="map-icon"><Location /></el-icon>
              <p>地图功能开发中...</p>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const loading = ref(true)

const volunteerId = computed(() => route.params.id)

const volunteer = reactive({
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

const getVolunteerDetail = async () => {
  if (!volunteerId.value) {
    ElMessage.error('志愿者ID不存在')
    router.push('/manager/volunteers')
    return
  }
  
  try {
    loading.value = true
    const response = await fetch(`/api/admin/volunteers/${volunteerId.value}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
      console.log('志愿者详情数据:', data.data)
      // 确保所有必要字段都存在，即使后端没有返回
      const volunteerData = {
        id: data.data.id || '',
        username: data.data.username || '',
        realName: data.data.realName || '',
        phone: data.data.phone || '',
        address: data.data.address || '',
        idCardNumber: data.data.idCardNumber || '',
        communityCardNumber: data.data.communityCardNumber || '',
        lat: data.data.lat || null,
        lng: data.data.lng || null,
        points: data.data.points || 0,
        enabled: data.data.enabled !== false,
        createdAt: data.data.createdAt || '',
        updatedAt: data.data.updatedAt || ''
      }
      Object.assign(volunteer, volunteerData)
    } else {
      ElMessage.error(data.message || '获取志愿者信息失败')
      router.push('/manager/volunteers')
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
    router.push('/manager/volunteers')
  } finally {
    loading.value = false
  }
}

const handleEdit = () => {
  router.push(`/manager/volunteers/${volunteerId.value}/edit`)
}

onMounted(() => {
  getVolunteerDetail()
})
</script>

<style scoped>
.volunteer-detail-container {
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

.map-card {
  margin-top: 30px;
}

.map-container {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  text-align: center;
}

.map-placeholder {
  margin-top: 20px;
  padding: 40px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.map-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 10px;
}

.map-placeholder p {
  color: #606266;
  margin: 0;
}
</style>