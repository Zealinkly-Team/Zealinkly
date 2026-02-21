<template>
  <div class="product-form-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑商品' : '新增商品' }}</span>
          <div>
            <el-button size="small" @click="$router.back()">返回</el-button>
          </div>
        </div>
      </template>
      
      <el-form
        :model="productForm"
        :rules="rules"
        ref="productFormRef"
        label-width="120px"
        class="demo-ruleForm"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="category">
          <el-select v-model="productForm.category" placeholder="请选择商品分类">
            <el-option label="生活用品" :value="'LIFESTYLE'" />
            <el-option label="食品饮料" :value="'FOOD'" />
            <el-option label="医疗健康" :value="'MEDICAL'" />
            <el-option label="其他" :value="'OTHER'" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分价格" prop="price">
          <el-input-number v-model="productForm.price" :min="1" :step="1" placeholder="请输入积分价格" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="productForm.stock" :min="0" :step="1" placeholder="请输入库存" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        <el-form-item label="商品图片" prop="imageUrl">
          <el-input v-model="productForm.imageUrl" placeholder="请输入商品图片URL" />
        </el-form-item>
        <el-form-item label="商品状态" prop="status">
          <el-select v-model="productForm.status" placeholder="请选择商品状态">
            <el-option label="上架" :value="'ACTIVE'" />
            <el-option label="下架" :value="'INACTIVE'" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">
            {{ isEdit ? '保存修改' : '创建商品' }}
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
const productFormRef = ref(null)

const productId = computed(() => route.params.id)
const isEdit = computed(() => !!productId.value)

const productForm = reactive({
  name: '',
  category: 'LIFESTYLE',
  price: 100,
  stock: 0,
  description: '',
  imageUrl: '',
  status: 'ACTIVE'
})

const rules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入积分价格', trigger: 'blur' },
    { type: 'number', min: 1, message: '积分价格必须大于 0', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存不能小于 0', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' },
    { min: 1, max: 500, message: '长度在 1 到 500 个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择商品状态', trigger: 'change' }
  ]
}

const getProductDetail = async () => {
  if (!productId.value) return
  
  try {
    loading.value = true
    const response = await fetch(`/api/admin/products/${productId.value}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
      // 转换后端返回的数据格式以匹配前端表单
      const productData = {
        name: data.data.name,
        category: data.data.category,
        price: data.data.pointsPrice,
        stock: data.data.stock,
        description: data.data.description,
        imageUrl: data.data.imageUrl,
        status: data.data.enabled ? 'ACTIVE' : 'INACTIVE'
      }
      Object.assign(productForm, productData)
    } else {
      ElMessage.error(data.message || '获取商品信息失败')
      router.push('/manager/products')
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
    router.push('/manager/products')
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  if (!productFormRef.value) return
  
  try {
    await productFormRef.value.validate()
    loading.value = true
    
    let url, method, successMessage
    if (isEdit.value) {
      url = `/api/admin/products/${productId.value}`
      method = 'PUT'
      successMessage = '商品已更新'
    } else {
      url = '/api/admin/products'
      method = 'POST'
      successMessage = '商品已创建'
    }
    
    // 转换数据格式以匹配后端API
    const requestData = {
      name: productForm.name,
      category: productForm.category,
      pointsPrice: productForm.price,
      stock: productForm.stock,
      description: productForm.description,
      imageUrl: productForm.imageUrl,
      enabled: productForm.status === 'ACTIVE'
    }
    
    const response = await fetch(url, {
      method: method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(requestData)
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success(successMessage)
      router.push('/manager/products')
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
  if (productFormRef.value) {
    productFormRef.value.resetFields()
  }
}

onMounted(() => {
  if (isEdit.value) {
    getProductDetail()
  }
})
</script>

<style scoped>
.product-form-container {
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