<template>
  <div class="exchange-list-container">
    <div class="page-header">
      <h3>兑换管理</h3>
    </div>
    
    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="老人姓名">
          <el-input v-model="searchForm.elderName" placeholder="请输入老人姓名" />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态">
            <el-option label="待审核" :value="'PENDING'" />
            <el-option label="已通过" :value="'APPROVED'" />
            <el-option label="已拒绝" :value="'REJECTED'" />
            <el-option label="已完成" :value="'COMPLETED'" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 兑换列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>兑换记录</span>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="exchanges"
        style="width: 100%"
      >
        <el-table-column prop="id" label="兑换ID" width="80" />
        <el-table-column prop="elderName" label="老人姓名" width="120" />
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="quantity" label="兑换数量" width="100" />
        <el-table-column prop="totalPoints" label="总积分" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180" />
        <el-table-column prop="processedAt" label="处理时间" width="180">
          <template #default="scope">
            <span>{{ scope.row.processedAt || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="viewExchange(scope.row.id)">查看</el-button>
            <template v-if="scope.row.status === 'PENDING'">
              <el-button size="small" type="primary" @click="approveExchange(scope.row.id)">通过</el-button>
              <el-button size="small" type="danger" @click="rejectExchange(scope.row.id)">拒绝</el-button>
            </template>
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
const exchanges = ref([])
const total = ref(0)
const page = ref(0)
const size = ref(20)

const searchForm = reactive({
  elderName: '',
  productName: '',
  status: ''
})

const getExchanges = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    params.append('page', page.value)
    params.append('size', size.value)
    if (searchForm.elderName) params.append('elderName', searchForm.elderName)
    if (searchForm.productName) params.append('productName', searchForm.productName)
    if (searchForm.status) params.append('status', searchForm.status)
    
    const response = await fetch(`/api/admin/exchanges?${params.toString()}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
      exchanges.value = data.data.content || []
      total.value = data.data.totalElements || 0
    } else {
      ElMessage.error(data.message || '获取兑换记录失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 0
  getExchanges()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  page.value = 0
  getExchanges()
}

const handleSizeChange = (newSize) => {
  size.value = newSize
  getExchanges()
}

const handleCurrentChange = (newPage) => {
  page.value = newPage
  getExchanges()
}

const getStatusType = (status) => {
  const statusMap = {
    PENDING: 'warning',
    APPROVED: 'primary',
    REJECTED: 'danger',
    COMPLETED: 'success'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    COMPLETED: '已完成'
  }
  return statusMap[status] || status
}

const viewExchange = (id) => {
  router.push(`/manager/exchanges/${id}`)
}

const approveExchange = async (id) => {
  try {
    await ElMessageBox.confirm('确定要通过这个兑换申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    const response = await fetch(`/api/admin/exchanges/${id}/approve`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('兑换申请已通过')
      getExchanges()
    } else {
      ElMessage.error(data.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试')
    }
  }
}

const rejectExchange = async (id) => {
  try {
    await ElMessageBox.confirm('确定要拒绝这个兑换申请吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await fetch(`/api/admin/exchanges/${id}/reject`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success('兑换申请已拒绝')
      getExchanges()
    } else {
      ElMessage.error(data.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试')
    }
  }
}

onMounted(() => {
  getExchanges()
})
</script>

<style scoped>
.exchange-list-container {
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