<template>
  <div class="exchange-form-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>商品兑换</span>
          <div>
            <el-button size="small" @click="$router.back()">返回</el-button>
          </div>
        </div>
      </template>
      
      <el-form
        :model="exchangeForm"
        :rules="rules"
        ref="exchangeFormRef"
        label-width="120px"
        class="demo-ruleForm"
      >
        <el-form-item label="志愿者选择" prop="volunteerId">
          <el-select v-model="exchangeForm.volunteerId" placeholder="请选择志愿者">
            <el-option
              v-for="volunteer in volunteers"
              :key="volunteer.id"
              :label="volunteer.realName"
              :value="volunteer.id"
            >
              <div class="option-content">
                <span>{{ volunteer.realName }}</span>
                <span class="option-subtitle">{{ volunteer.username }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="商品选择" prop="productId">
          <el-select v-model="exchangeForm.productId" placeholder="请选择商品">
            <el-option
              v-for="product in products"
              :key="product.id"
              :label="product.name"
              :value="product.id"
            >
              <div class="option-content">
                <span>{{ product.name }}</span>
                <span class="option-subtitle">{{ product.pointsPrice }} 积分</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="兑换数量" prop="quantity">
          <el-input-number 
            v-model="exchangeForm.quantity" 
            :min="1" 
            :step="1" 
            placeholder="请输入兑换数量"
          />
        </el-form-item>
        
        <el-form-item label="积分消耗" prop="totalPoints">
          <el-input 
            v-model="exchangeForm.totalPoints" 
            placeholder="总积分" 
            disabled
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">
            确认兑换
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { exchangeAPI, volunteerAPI, productAPI } from '@/utils/api'

const router = useRouter()
const loading = ref(false)
const exchangeFormRef = ref(null)
const volunteers = ref([])
const products = ref([])

const exchangeForm = reactive({
  volunteerId: '',
  productId: '',
  quantity: 1,
  totalPoints: 0
})

const rules = {
  volunteerId: [
    { required: true, message: '请选择志愿者', trigger: 'change' }
  ],
  productId: [
    { required: true, message: '请选择商品', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入兑换数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '兑换数量必须大于 0', trigger: 'blur' }
  ]
}

// 计算总积分
const calculateTotalPoints = () => {
  if (!exchangeForm.productId || !exchangeForm.quantity) {
    exchangeForm.totalPoints = 0
    return
  }
  
  const product = products.value.find(p => p.id === exchangeForm.productId)
  if (product) {
    exchangeForm.totalPoints = product.pointsPrice * exchangeForm.quantity
  } else {
    exchangeForm.totalPoints = 0
  }
}

// 监听商品和数量变化，计算总积分
watch(() => exchangeForm.productId, () => {
  calculateTotalPoints()
})

watch(() => exchangeForm.quantity, () => {
  calculateTotalPoints()
})

// 获取志愿者列表
const getVolunteers = async () => {
  try {
    const response = await volunteerAPI.getList({ enabled: true })
    if (response.code === 200) {
      volunteers.value = response.data.content
    }
  } catch (error) {
    ElMessage.error('获取志愿者列表失败')
  }
}

// 获取商品列表（已启用）
const getProducts = async () => {
  try {
    const response = await productAPI.getEnabledList()
    if (response.code === 200) {
      products.value = response.data.content
    }
  } catch (error) {
    ElMessage.error('获取商品列表失败')
  }
}

const submitForm = async () => {
  if (!exchangeFormRef.value) return
  
  try {
    await exchangeFormRef.value.validate()
    loading.value = true
    
    const response = await exchangeAPI.exchange({
      volunteerId: exchangeForm.volunteerId,
      productId: exchangeForm.productId,
      quantity: exchangeForm.quantity
    })
    
    if (response.code === 200) {
      ElMessage.success('兑换成功')
      router.push('/manager/exchanges')
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
  if (exchangeFormRef.value) {
    exchangeFormRef.value.resetFields()
  }
  exchangeForm.totalPoints = 0
}

onMounted(() => {
  getVolunteers()
  getProducts()
})
</script>

<style scoped>
.exchange-form-container {
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

.option-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.option-subtitle {
  font-size: 12px;
  color: #999;
}
</style>