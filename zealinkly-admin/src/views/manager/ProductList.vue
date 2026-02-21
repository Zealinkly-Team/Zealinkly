<template>
  <div class="product-list-container">
    <div class="page-header">
      <h3>商品管理</h3>
      <el-button type="primary" @click="$router.push('/manager/products/create')">新增商品</el-button>
    </div>
    
    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类">
            <el-option label="生活用品" :value="'LIFESTYLE'" />
            <el-option label="食品饮料" :value="'FOOD'" />
            <el-option label="医疗健康" :value="'MEDICAL'" />
            <el-option label="其他" :value="'OTHER'" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态">
            <el-option label="上架" :value="'ACTIVE'" />
            <el-option label="下架" :value="'INACTIVE'" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 商品列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>商品列表</span>
          <el-button type="danger" @click="handleBatchDelete" :disabled="selectedProductIds.length === 0">
            批量删除
          </el-button>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="products"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="商品ID" width="80" />
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="scope">
            <span>{{ getCategoryText(scope.row.category) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="积分价格" width="100" />
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" type="primary" @click="editProduct(scope.row.id)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteProduct(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const products = ref([])
const total = ref(0)
const page = ref(0)
const size = ref(20)
const selectedProductIds = ref([])

const searchForm = reactive({
  name: '',
  category: '',
  status: ''
})

const getProducts = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    params.append('page', page.value)
    params.append('size', size.value)
    if (searchForm.name) params.append('name', searchForm.name)
    if (searchForm.category) params.append('category', searchForm.category)
    if (searchForm.status) params.append('status', searchForm.status)
    
    const response = await fetch(`/api/admin/products?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
      products.value = data.data.content || []
      total.value = data.data.totalElements || 0
    } else {
      ElMessage.error(data.message || '获取商品列表失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 0
  getProducts()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  page.value = 0
  getProducts()
}

const handleSizeChange = (newSize) => {
  size.value = newSize
  getProducts()
}

const handleCurrentChange = (newPage) => {
  page.value = newPage
  getProducts()
}

const handleSelectionChange = (selection) => {
  selectedProductIds.value = selection.map(item => item.id)
}

const getCategoryText = (category) => {
  const categoryMap = {
    LIFESTYLE: '生活用品',
    FOOD: '食品饮料',
    MEDICAL: '医疗健康',
    OTHER: '其他'
  }
  return categoryMap[category] || category
}

const getStatusType = (status) => {
  const statusMap = {
    ACTIVE: 'success',
    INACTIVE: 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    ACTIVE: '上架',
    INACTIVE: '下架'
  }
  return statusMap[status] || status
}

const viewProduct = (id) => {
  router.push(`/manager/products/${id}`)
}

const editProduct = (id) => {
  router.push(`/manager/products/${id}/edit`)
}

const deleteProduct = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await fetch(`/api/admin/products/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('商品已删除')
      getProducts()
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedProductIds.value.length === 0) {
    ElMessage.warning('请选择要删除的商品')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedProductIds.value.length} 个商品吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await fetch('/api/admin/products/batch', {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(selectedProductIds.value)
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('商品已批量删除')
      selectedProductIds.value = []
      getProducts()
    } else {
      ElMessage.error(data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试')
    }
  }
}

onMounted(() => {
  getProducts()
})
</script>

<style scoped>
.product-list-container {
  padding: 20px 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.list-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}
</style>